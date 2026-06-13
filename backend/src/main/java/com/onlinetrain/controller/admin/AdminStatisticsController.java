package com.onlinetrain.controller.admin;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.*;
import com.onlinetrain.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    @Autowired
    private UserService userService;

    /**
     * Dashboard 仪表盘
     */
    @GetMapping("/statistics/dashboard")
    @ApiOperation("仪表盘")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> result = new HashMap<>();

        // 基本统计
        long totalCourses = courseService.count();
        long totalStudents = userService.lambdaQuery().eq(User::getRole, "STUDENT").count();
        long totalQuestions = questionService.count();

        // 营收统计
        List<Order> paidOrders = orderService.lambdaQuery().eq(Order::getStatus, "PAID").list();
        double totalRevenue = paidOrders.stream()
                .filter(o -> o.getAmount() != null)
                .mapToDouble(o -> o.getAmount().doubleValue()).sum();

        // 今日营收
        LocalDate today = LocalDate.now();
        double todayRevenue = paidOrders.stream()
                .filter(o -> o.getPayTime() != null && o.getPayTime().toLocalDate().equals(today))
                .filter(o -> o.getAmount() != null)
                .mapToDouble(o -> o.getAmount().doubleValue()).sum();

        // 本月新增学员
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        long monthlyNewStudents = userService.lambdaQuery()
                .eq(User::getRole, "STUDENT")
                .ge(User::getRegisterTime, firstDayOfMonth.atStartOfDay())
                .count();

        // 近30天营收趋势
        List<Map<String, Object>> revenueTrend = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            double dayRevenue = paidOrders.stream()
                    .filter(o -> o.getPayTime() != null && o.getPayTime().toLocalDate().equals(date))
                    .filter(o -> o.getAmount() != null)
                    .mapToDouble(o -> o.getAmount().doubleValue()).sum();
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("amount", dayRevenue);
            revenueTrend.add(item);
        }

        // 近7天新增学员趋势
        List<Map<String, Object>> studentTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long newStudents = userService.lambdaQuery()
                    .eq(User::getRole, "STUDENT")
                    .ge(User::getRegisterTime, date.atStartOfDay())
                    .lt(User::getRegisterTime, date.plusDays(1).atStartOfDay())
                    .count();
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", newStudents);
            studentTrend.add(item);
        }

        result.put("totalStudents", totalStudents);
        result.put("totalCourses", totalCourses);
        result.put("totalQuestions", totalQuestions);
        result.put("totalRevenue", totalRevenue);
        result.put("totalOrders", paidOrders.size());
        result.put("todayRevenue", todayRevenue);
        result.put("monthlyNewStudents", monthlyNewStudents);
        result.put("revenueTrend", revenueTrend);
        result.put("studentTrend", studentTrend);

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
        long totalOrders = paidOrders.size();
        result.put("totalRevenue", totalRevenue);
        result.put("totalOrders", totalOrders);
        result.put("avgOrderAmount", totalOrders > 0 ? Math.round(totalRevenue / totalOrders * 100.0) / 100.0 : 0);

        // 支付方式分布
        Map<String, Long> payMethodCount = paidOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getPayMethod() != null ? o.getPayMethod() : "UNKNOWN",
                        Collectors.counting()));
        List<Map<String, Object>> payMethodDistribution = new ArrayList<>();
        payMethodCount.forEach((method, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("method", method);
            item.put("count", count);
            payMethodDistribution.add(item);
        });
        result.put("payMethodDistribution", payMethodDistribution);

        // 课程营收排行
        Map<Long, Map<String, Object>> courseRevenueMap = new LinkedHashMap<>();
        for (Order o : paidOrders) {
            courseRevenueMap.computeIfAbsent(o.getCourseId(), k -> {
                Map<String, Object> item = new HashMap<>();
                item.put("revenue", 0.0);
                item.put("orderCount", 0L);
                return item;
            });
            Map<String, Object> item = courseRevenueMap.get(o.getCourseId());
            item.put("revenue", (Double) item.get("revenue") + (o.getAmount() != null ? o.getAmount().doubleValue() : 0));
            item.put("orderCount", (Long) item.get("orderCount") + 1);
        }

        List<Map<String, Object>> courseRanking = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Object>> entry : courseRevenueMap.entrySet()) {
            Course course = courseService.getById(entry.getKey());
            Map<String, Object> item = new HashMap<>(entry.getValue());
            item.put("courseId", entry.getKey());
            item.put("courseName", course != null ? course.getTitle() : "未知课程");
            courseRanking.add(item);
        }
        courseRanking.sort((a, b) -> Double.compare((Double) b.get("revenue"), (Double) a.get("revenue")));
        if (courseRanking.size() > 10) courseRanking = courseRanking.subList(0, 10);
        result.put("courseRanking", courseRanking);

        // 近30天日营收趋势
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> dailyRevenue = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            double dayRevenue = paidOrders.stream()
                    .filter(o -> o.getPayTime() != null && o.getPayTime().toLocalDate().equals(date))
                    .filter(o -> o.getAmount() != null)
                    .mapToDouble(o -> o.getAmount().doubleValue()).sum();
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("amount", dayRevenue);
            dailyRevenue.add(item);
        }
        result.put("dailyRevenue", dailyRevenue);

        // 近12个月月营收趋势
        List<Map<String, Object>> monthlyRevenue = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1);
            double monthRevenue = paidOrders.stream()
                    .filter(o -> o.getPayTime() != null
                            && !o.getPayTime().toLocalDate().isBefore(monthStart)
                            && o.getPayTime().toLocalDate().isBefore(monthEnd))
                    .filter(o -> o.getAmount() != null)
                    .mapToDouble(o -> o.getAmount().doubleValue()).sum();
            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStart.toString().substring(0, 7));
            item.put("amount", monthRevenue);
            monthlyRevenue.add(item);
        }
        result.put("monthlyRevenue", monthlyRevenue);

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
                    .eq(LearningRecord::getCourseId, course.getId())
                    .list().stream().map(LearningRecord::getUserId).distinct().count();
            long finishedCount = learningRecordService.lambdaQuery()
                    .eq(LearningRecord::getCourseId, course.getId())
                    .eq(LearningRecord::getIsFinished, 1).count();
            Map<String, Object> item = new HashMap<>();
            item.put("courseId", course.getId());
            item.put("courseName", course.getTitle());
            item.put("studentCount", learnerCount);
            item.put("completionCount", finishedCount);
            item.put("avgCompletionRate", learnerCount > 0 ? (int)((double)finishedCount / learnerCount * 100) : 0);
            item.put("avgWatchDuration", 0);
            courseStats.add(item);
        }
        result.put("courseStats", courseStats);

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
        int totalWrongSum = 0;
        for (WrongQuestion wq : allWrong) {
            wrongCountMap.merge(wq.getQuestionId(), wq.getWrongCount(), Integer::sum);
            totalWrongSum += wq.getWrongCount() != null ? wq.getWrongCount() : 0;
        }
        final int finalTotalWrong = totalWrongSum;
        List<Map<String, Object>> topWrong = wrongCountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed()).limit(10)
                .map(entry -> {
                    Question q = questionService.getById(entry.getKey());
                    Map<String, Object> item = new HashMap<>();
                    item.put("questionId", entry.getKey());
                    item.put("content", q != null ? q.getContent() : "");
                    item.put("wrongCount", entry.getValue());
                    item.put("wrongRate", finalTotalWrong > 0 ? (double) entry.getValue() / finalTotalWrong : 0);
                    return item;
                }).collect(Collectors.toList());
        result.put("topWrongQuestions", topWrong);

        return Result.ok(result);
    }

    @GetMapping("/statistics/exam")
    @ApiOperation("考试统计")
    public Result<Map<String, Object>> exam(@RequestParam(required = false) Long examPaperId) {
        Map<String, Object> result = new HashMap<>();
        
        // 所有已提交的考试记录
        List<ExamRecord> allRecords = examRecordService.lambdaQuery()
                .eq(ExamRecord::getStatus, "SUBMITTED").list();
        
        // 选择的考试记录
        List<ExamRecord> records = examPaperId != null 
                ? allRecords.stream().filter(r -> r.getExamPaperId().equals(examPaperId)).collect(Collectors.toList())
                : allRecords;
        
        // 统计指标 - 针对选择的考试或全部
        double avgScore = records.stream().filter(r -> r.getScore() != null)
                .mapToDouble(r -> r.getScore().doubleValue()).average().orElse(0);
        result.put("avgScore", Math.round(avgScore * 10.0) / 10.0);
        
        long passCount = records.stream().filter(r -> r.getIsPass() == 1).count();
        double passRate = records.size() > 0 ? (double) passCount / records.size() * 100 : 0;
        result.put("passCount", passCount);
        result.put("passRate", Math.round(passRate * 10.0) / 10.0);
        
        // 参与人数
        result.put("totalParticipants", records.size());

        // 成绩分布 - 针对选择的考试
        long[] scoreDist = new long[5];
        scoreDist[0] = records.stream().filter(r -> r.getScore() != null && r.getScore().doubleValue() < 60).count();
        scoreDist[1] = records.stream().filter(r -> r.getScore() != null && r.getScore().doubleValue() >= 60 && r.getScore().doubleValue() < 70).count();
        scoreDist[2] = records.stream().filter(r -> r.getScore() != null && r.getScore().doubleValue() >= 70 && r.getScore().doubleValue() < 80).count();
        scoreDist[3] = records.stream().filter(r -> r.getScore() != null && r.getScore().doubleValue() >= 80 && r.getScore().doubleValue() < 90).count();
        scoreDist[4] = records.stream().filter(r -> r.getScore() != null && r.getScore().doubleValue() >= 90).count();
        result.put("scoreDistribution", scoreDist);
        
        // 选择的考试信息
        if (examPaperId != null) {
            ExamPaper selectedPaper = examPaperService.getById(examPaperId);
            if (selectedPaper != null) {
                result.put("selectedExamTitle", selectedPaper.getTitle());
                result.put("selectedExamTotalScore", selectedPaper.getTotalScore());
                result.put("selectedExamPassScore", selectedPaper.getPassScore());
            }
        }

        // 全部试卷统计概览
        List<ExamPaper> papers = examPaperService.list();
        List<Map<String, Object>> examPaperStats = new ArrayList<>();
        for (ExamPaper paper : papers) {
            List<ExamRecord> paperRecords = allRecords.stream()
                    .filter(r -> r.getExamPaperId().equals(paper.getId())).collect(Collectors.toList());
            double paperAvgScore = paperRecords.stream().filter(r -> r.getScore() != null)
                    .mapToDouble(r -> r.getScore().doubleValue()).average().orElse(0);
            long paperPassCount = paperRecords.stream().filter(r -> r.getIsPass() == 1).count();
            double paperPassRate = paperRecords.size() > 0 ? (double) paperPassCount / paperRecords.size() * 100 : 0;
            Map<String, Object> item = new HashMap<>();
            item.put("paperId", paper.getId());
            item.put("examTitle", paper.getTitle());
            item.put("totalParticipants", paperRecords.size());
            item.put("passCount", paperPassCount);
            item.put("avgScore", Math.round(paperAvgScore * 10.0) / 10.0);
            item.put("passRate", Math.round(paperPassRate * 10.0) / 10.0);
            examPaperStats.add(item);
        }
        result.put("examPaperStats", examPaperStats);
        
        // 所有试卷列表（供前端下拉选择）
        List<Map<String, Object>> examList = papers.stream().map(p -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("title", p.getTitle());
            item.put("examType", p.getExamType() != null ? p.getExamType() : "ONLINE");
            return item;
        }).collect(Collectors.toList());
        result.put("examPapers", examList);

        return Result.ok(result);
    }
}
