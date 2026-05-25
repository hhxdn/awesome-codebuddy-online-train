package com.onlinetrain.controller.admin;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.*;
import com.onlinetrain.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理端-统计控制器
 */
@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-数据统计")
public class AdminStatisticsController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private LearningRecordService learningRecordService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private WrongQuestionService wrongQuestionService;
    @Autowired
    private ExamRecordService examRecordService;
    @Autowired
    private ExamPaperService examPaperService;

    /**
     * Dashboard 仪表盘
     */
    @GetMapping("/statistics/dashboard")
    @ApiOperation("仪表盘")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> result = new HashMap<>();

        // 基本统计
        long totalCourses = courseService.count();
        long totalUsers = orderService.lambdaQuery().list().stream().map(Order::getUserId).distinct().count();
        long totalQuestions = questionService.count();

        // 营收统计
        List<Order> paidOrders = orderService.lambdaQuery().eq(Order::getStatus, "PAID").list();
        double totalRevenue = paidOrders.stream()
                .filter(o -> o.getAmount() != null)
                .mapToDouble(o -> o.getAmount().doubleValue()).sum();

        result.put("totalCourses", totalCourses);
        result.put("totalUsers", totalUsers);
        result.put("totalQuestions", totalQuestions);
        result.put("totalRevenue", totalRevenue);
        result.put("totalOrders", paidOrders.size());

        return Result.ok(result);
    }

    @GetMapping("/statistics/revenue")
    @ApiOperation("营收统计")
    public Result<Map<String, Object>> revenue() {
        Map<String, Object> result = new HashMap<>();
        List<Order> paidOrders = orderService.lambdaQuery().eq(Order::getStatus, "PAID").list();

        double totalRevenue = paidOrders.stream()
                .filter(o -> o.getAmount() != null)
                .mapToDouble(o -> o.getAmount().doubleValue()).sum();
        result.put("totalRevenue", totalRevenue);
        result.put("totalOrders", paidOrders.size());

        Map<String, Double> revenueByPayMethod = paidOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getPayMethod() != null ? o.getPayMethod() : "UNKNOWN",
                        Collectors.summingDouble(o -> o.getAmount() != null ? o.getAmount().doubleValue() : 0)));
        result.put("revenueByPayMethod", revenueByPayMethod);

        Map<Long, Double> courseRevenueMap = paidOrders.stream()
                .collect(Collectors.groupingBy(Order::getCourseId,
                        Collectors.summingDouble(o -> o.getAmount() != null ? o.getAmount().doubleValue() : 0)));

        List<Map<String, Object>> courseRanking = new ArrayList<>();
        courseRevenueMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed()).limit(10)
                .forEach(entry -> {
                    Course course = courseService.getById(entry.getKey());
                    Map<String, Object> item = new HashMap<>();
                    item.put("courseId", entry.getKey());
                    item.put("courseTitle", course != null ? course.getTitle() : "未知课程");
                    item.put("revenue", entry.getValue());
                    courseRanking.add(item);
                });
        result.put("courseRevenueRanking", courseRanking);

        Map<String, Double> dailyRevenue = new LinkedHashMap<>();
        paidOrders.stream().filter(o -> o.getPayTime() != null).forEach(o -> {
            String day = o.getPayTime().toLocalDate().toString();
            dailyRevenue.merge(day, o.getAmount() != null ? o.getAmount().doubleValue() : 0, Double::sum);
        });
        result.put("dailyRevenueTrend", dailyRevenue);

        return Result.ok(result);
    }

    @GetMapping("/statistics/learning")
    @ApiOperation("学习统计")
    public Result<Map<String, Object>> learning() {
        Map<String, Object> result = new HashMap<>();
        List<Course> courses = courseService.list();
        List<Map<String, Object>> courseStats = new ArrayList<>();
        for (Course course : courses) {
            long learnerCount = learningRecordService.lambdaQuery()
                    .eq(LearningRecord::getCourseId, course.getId()).count();
            long finishedCount = learningRecordService.lambdaQuery()
                    .eq(LearningRecord::getCourseId, course.getId())
                    .eq(LearningRecord::getIsFinished, 1).count();
            Map<String, Object> item = new HashMap<>();
            item.put("courseId", course.getId());
            item.put("courseTitle", course.getTitle());
            item.put("learnerCount", learnerCount);
            item.put("finishedCount", finishedCount);
            courseStats.add(item);
        }
        result.put("courseLearningStats", courseStats);

        long totalQuestions = questionService.count();
        Map<String, Object> questionStats = new HashMap<>();
        questionStats.put("total", totalQuestions);
        questionStats.put("singleCount", questionService.lambdaQuery().eq(Question::getType, "SINGLE").count());
        questionStats.put("multiCount", questionService.lambdaQuery().eq(Question::getType, "MULTIPLE").count());
        questionStats.put("judgeCount", questionService.lambdaQuery().eq(Question::getType, "JUDGE").count());
        questionStats.put("essayCount", questionService.lambdaQuery().eq(Question::getType, "ESSAY").count());
        result.put("questionBankStats", questionStats);

        List<WrongQuestion> allWrong = wrongQuestionService.list();
        Map<Long, Integer> wrongCountMap = new HashMap<>();
        for (WrongQuestion wq : allWrong) wrongCountMap.merge(wq.getQuestionId(), wq.getWrongCount(), Integer::sum);
        List<Map<String, Object>> topWrong = wrongCountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed()).limit(10)
                .map(entry -> {
                    Question q = questionService.getById(entry.getKey());
                    Map<String, Object> item = new HashMap<>();
                    item.put("questionId", entry.getKey());
                    item.put("content", q != null ? q.getContent() : "");
                    item.put("wrongCount", entry.getValue());
                    return item;
                }).collect(Collectors.toList());
        result.put("topWrongQuestions", topWrong);

        return Result.ok(result);
    }

    @GetMapping("/statistics/exam")
    @ApiOperation("考试统计")
    public Result<Map<String, Object>> exam() {
        Map<String, Object> result = new HashMap<>();
        List<ExamRecord> records = examRecordService.lambdaQuery()
                .eq(ExamRecord::getStatus, "SUBMITTED").list();

        result.put("totalExams", records.size());
        double avgScore = records.stream().filter(r -> r.getScore() != null)
                .mapToDouble(r -> r.getScore().doubleValue()).average().orElse(0);
        result.put("averageScore", Math.round(avgScore * 10.0) / 10.0);

        long passCount = records.stream().filter(r -> r.getIsPass() == 1).count();
        double passRate = records.size() > 0 ? (double) passCount / records.size() * 100 : 0;
        result.put("passCount", passCount);
        result.put("passRate", Math.round(passRate * 10.0) / 10.0);

        Map<String, Long> scoreDistribution = new LinkedHashMap<>();
        scoreDistribution.put("0-59", records.stream().filter(r -> r.getScore() != null && r.getScore().doubleValue() < 60).count());
        scoreDistribution.put("60-69", records.stream().filter(r -> r.getScore() != null && r.getScore().doubleValue() >= 60 && r.getScore().doubleValue() < 70).count());
        scoreDistribution.put("70-79", records.stream().filter(r -> r.getScore() != null && r.getScore().doubleValue() >= 70 && r.getScore().doubleValue() < 80).count());
        scoreDistribution.put("80-89", records.stream().filter(r -> r.getScore() != null && r.getScore().doubleValue() >= 80 && r.getScore().doubleValue() < 90).count());
        scoreDistribution.put("90-100", records.stream().filter(r -> r.getScore() != null && r.getScore().doubleValue() >= 90).count());
        result.put("scoreDistribution", scoreDistribution);

        List<ExamPaper> papers = examPaperService.list();
        List<Map<String, Object>> paperStats = new ArrayList<>();
        for (ExamPaper paper : papers) {
            List<ExamRecord> paperRecords = records.stream()
                    .filter(r -> r.getExamPaperId().equals(paper.getId())).collect(Collectors.toList());
            double paperAvgScore = paperRecords.stream().filter(r -> r.getScore() != null)
                    .mapToDouble(r -> r.getScore().doubleValue()).average().orElse(0);
            long paperPassCount = paperRecords.stream().filter(r -> r.getIsPass() == 1).count();
            Map<String, Object> item = new HashMap<>();
            item.put("paperId", paper.getId());
            item.put("paperTitle", paper.getTitle());
            item.put("totalAttempts", paperRecords.size());
            item.put("passCount", paperPassCount);
            item.put("averageScore", Math.round(paperAvgScore * 10.0) / 10.0);
            paperStats.add(item);
        }
        result.put("paperStats", paperStats);

        return Result.ok(result);
    }
}
