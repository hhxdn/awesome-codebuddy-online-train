package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.ExamPaper;
import com.onlinetrain.entity.ExamReservation;
import com.onlinetrain.entity.User;
import com.onlinetrain.service.ExamPaperService;
import com.onlinetrain.service.ExamReservationService;
import com.onlinetrain.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理端-线下考试预约管理
 */
@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-线下考试预约管理")
public class AdminExamReservationController {

    @Autowired
    private ExamReservationService examReservationService;

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private UserService userService;

    /**
     * 预约列表
     */
    @GetMapping("/reservations")
    @ApiOperation("预约列表")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) Long examPaperId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<ExamReservation> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamReservation> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (examPaperId != null) wrapper.eq(ExamReservation::getExamPaperId, examPaperId);
        if (status != null && !status.isEmpty()) wrapper.eq(ExamReservation::getStatus, status);
        wrapper.orderByDesc(ExamReservation::getCreateTime);

        Page<ExamReservation> result = examReservationService.page(pageParam, wrapper);
        List<Map<String, Object>> records = result.getRecords().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("examPaperId", r.getExamPaperId());
            map.put("userId", r.getUserId());
            map.put("reservationTime", r.getReservationTime());
            map.put("status", r.getStatus());
            map.put("remark", r.getRemark());
            map.put("createTime", r.getCreateTime());

            ExamPaper paper = examPaperService.getById(r.getExamPaperId());
            map.put("examTitle", paper != null ? paper.getTitle() : "");

            User user = userService.getById(r.getUserId());
            map.put("userName", user != null ? (user.getRealName() != null ? user.getRealName() : user.getNickname()) : "");
            map.put("userPhone", user != null ? user.getPhone() : "");
            return map;
        }).collect(Collectors.toList());

        PageResult<Map<String, Object>> pageResult = PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
        return Result.ok(pageResult);
    }

    /**
     * 确认预约
     */
    @PutMapping("/reservations/{id}/confirm")
    @ApiOperation("确认预约")
    public Result<Void> confirm(@PathVariable Long id) {
        ExamReservation reservation = examReservationService.getById(id);
        if (reservation == null) return Result.notFound("预约不存在");
        if (!"PENDING".equals(reservation.getStatus())) {
            return Result.error("当前状态不允许确认");
        }
        reservation.setStatus("CONFIRMED");
        examReservationService.updateById(reservation);
        return Result.ok();
    }

    /**
     * 取消预约（管理员操作）
     */
    @PutMapping("/reservations/{id}/cancel")
    @ApiOperation("取消预约")
    public Result<Void> cancel(@PathVariable Long id) {
        ExamReservation reservation = examReservationService.getById(id);
        if (reservation == null) return Result.notFound("预约不存在");
        reservation.setStatus("CANCELLED");
        examReservationService.updateById(reservation);
        return Result.ok();
    }

    /**
     * 标记为已完成（考试已结束）
     */
    @PutMapping("/reservations/{id}/complete")
    @ApiOperation("标记已完成")
    public Result<Void> complete(@PathVariable Long id) {
        ExamReservation reservation = examReservationService.getById(id);
        if (reservation == null) return Result.notFound("预约不存在");
        reservation.setStatus("COMPLETED");
        examReservationService.updateById(reservation);
        return Result.ok();
    }
}
