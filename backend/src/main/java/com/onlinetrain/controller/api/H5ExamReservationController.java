package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.ExamPaper;
import com.onlinetrain.entity.ExamReservation;
import com.onlinetrain.service.ExamPaperService;
import com.onlinetrain.service.ExamReservationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * H5-线下考试预约控制器
 */
@RestController
@RequestMapping("/api")
@Api(tags = "H5-线下考试预约")
public class H5ExamReservationController {

    @Autowired
    private ExamReservationService examReservationService;

    @Autowired
    private ExamPaperService examPaperService;

    /**
     * 获取可预约的线下考试列表
     */
    @GetMapping("/exam/reservations/available")
    @ApiOperation("可预约的线下考试列表")
    public Result<List<Map<String, Object>>> available(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        // 获取所有已发布的线下考试
        List<ExamPaper> offlinePapers = examPaperService.lambdaQuery()
                .eq(ExamPaper::getExamType, "OFFLINE")
                .eq(ExamPaper::getStatus, "PUBLISHED")
                .list();

        // 获取该用户已有预约
        List<ExamReservation> myReservations = examReservationService.lambdaQuery()
                .eq(ExamReservation::getUserId, userId)
                .list();
        Set<Long> reservedExamIds = myReservations.stream()
                .map(ExamReservation::getExamPaperId)
                .collect(Collectors.toSet());
        Map<Long, ExamReservation> reservationMap = myReservations.stream()
                .collect(Collectors.toMap(ExamReservation::getExamPaperId, r -> r, (a, b) -> a));

        List<Map<String, Object>> result = offlinePapers.stream().map(paper -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", paper.getId());
            map.put("title", paper.getTitle());
            map.put("durationMinutes", paper.getDurationMinutes());
            map.put("totalScore", paper.getTotalScore());
            map.put("passScore", paper.getPassScore());
            map.put("courseId", paper.getCourseId());
            map.put("hasReserved", reservedExamIds.contains(paper.getId()));

            ExamReservation myRes = reservationMap.get(paper.getId());
            if (myRes != null) {
                map.put("reservationId", myRes.getId());
                map.put("reservationTime", myRes.getReservationTime());
                map.put("reservationStatus", myRes.getStatus());
            }
            return map;
        }).collect(Collectors.toList());

        return Result.ok(result);
    }

    /**
     * 预约线下考试
     * body: { examPaperId, reservationTime (可选) }
     */
    @PostMapping("/exam/reservations")
    @ApiOperation("预约线下考试")
    public Result<Map<String, Object>> reserve(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long examPaperId = Long.valueOf(params.get("examPaperId").toString());

        ExamPaper paper = examPaperService.getById(examPaperId);
        if (paper == null) return Result.notFound("考试不存在");
        if (!"OFFLINE".equals(paper.getExamType())) {
            return Result.error("该考试为线上考试，无需预约");
        }
        if (!"PUBLISHED".equals(paper.getStatus())) {
            return Result.error("该考试暂未开放预约");
        }

        // 检查是否已预约
        ExamReservation existing = examReservationService.lambdaQuery()
                .eq(ExamReservation::getUserId, userId)
                .eq(ExamReservation::getExamPaperId, examPaperId)
                .one();
        if (existing != null) {
            if ("CANCELLED".equals(existing.getStatus())) {
                // 已取消的可以重新预约
                existing.setStatus("PENDING");
                existing.setRemark(null);
                if (params.get("reservationTime") != null) {
                    existing.setReservationTime(java.time.LocalDateTime.parse(params.get("reservationTime").toString()));
                }
                examReservationService.updateById(existing);
                Map<String, Object> result = new HashMap<>();
                result.put("id", existing.getId());
                result.put("status", "PENDING");
                result.put("message", "重新预约成功，等待管理员确认");
                return Result.ok(result);
            }
            return Result.error("您已预约该考试，状态：" + existing.getStatus());
        }

        ExamReservation reservation = new ExamReservation();
        reservation.setUserId(userId);
        reservation.setExamPaperId(examPaperId);
        reservation.setStatus("PENDING");
        if (params.get("reservationTime") != null) {
            reservation.setReservationTime(java.time.LocalDateTime.parse(params.get("reservationTime").toString()));
        }
        if (params.get("remark") != null) {
            reservation.setRemark(params.get("remark").toString());
        }
        examReservationService.save(reservation);

        Map<String, Object> result = new HashMap<>();
        result.put("id", reservation.getId());
        result.put("status", "PENDING");
        result.put("message", "预约成功，等待管理员确认");
        return Result.ok(result);
    }

    /**
     * 取消预约
     */
    @PutMapping("/exam/reservations/{id}/cancel")
    @ApiOperation("取消预约")
    public Result<Void> cancelReservation(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        ExamReservation reservation = examReservationService.getById(id);
        if (reservation == null || !reservation.getUserId().equals(userId)) {
            return Result.notFound("预约不存在");
        }
        if (!"PENDING".equals(reservation.getStatus())) {
            return Result.error("当前状态不允许取消");
        }
        reservation.setStatus("CANCELLED");
        examReservationService.updateById(reservation);
        return Result.ok();
    }

    /**
     * 我的预约列表
     */
    @GetMapping("/exam/reservations/my")
    @ApiOperation("我的预约列表")
    public Result<List<Map<String, Object>>> myReservations(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        List<ExamReservation> reservations = examReservationService.lambdaQuery()
                .eq(ExamReservation::getUserId, userId)
                .orderByDesc(ExamReservation::getCreateTime)
                .list();

        List<Map<String, Object>> result = reservations.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("examPaperId", r.getExamPaperId());
            map.put("status", r.getStatus());
            map.put("reservationTime", r.getReservationTime());
            map.put("remark", r.getRemark());
            map.put("createTime", r.getCreateTime());

            ExamPaper paper = examPaperService.getById(r.getExamPaperId());
            map.put("examTitle", paper != null ? paper.getTitle() : "");
            map.put("totalScore", paper != null ? paper.getTotalScore() : 0);
            map.put("passScore", paper != null ? paper.getPassScore() : 0);
            return map;
        }).collect(Collectors.toList());

        return Result.ok(result);
    }
}
