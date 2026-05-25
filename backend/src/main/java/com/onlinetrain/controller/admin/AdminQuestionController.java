package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Question;
import com.onlinetrain.entity.QuestionOption;
import com.onlinetrain.service.QuestionOptionService;
import com.onlinetrain.service.QuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 管理端-题库管理控制器
 */
@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-题库管理")
public class AdminQuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionOptionService questionOptionService;

    @GetMapping("/questions")
    @ApiOperation("题目列表")
    public Result<PageResult<Question>> list(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Question> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Question> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (courseId != null) wrapper.eq(Question::getCourseId, courseId);
        if (chapterId != null) wrapper.eq(Question::getChapterId, chapterId);
        if (type != null && !type.isEmpty()) wrapper.eq(Question::getType, type);
        wrapper.orderByDesc(Question::getCreateTime);

        return Result.ok(PageResult.of(questionService.page(pageParam, wrapper)));
    }

    @GetMapping("/questions/{id}")
    @ApiOperation("题目详情")
    public Result<Question> detail(@PathVariable Long id) {
        Question q = questionService.getById(id);
        if (q == null) return Result.notFound("题目不存在");
        return Result.ok(q);
    }

    @PostMapping("/questions")
    @ApiOperation("创建题目")
    public Result<Question> create(@RequestBody Question question) {
        questionService.save(question);
        return Result.ok(question);
    }

    @PutMapping("/questions/{id}")
    @ApiOperation("更新题目")
    public Result<Question> update(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        questionService.updateById(question);
        return Result.ok(question);
    }

    @DeleteMapping("/questions/{id}")
    @ApiOperation("删除题目")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.removeById(id);
        questionOptionService.lambdaUpdate().eq(QuestionOption::getQuestionId, id).remove();
        return Result.ok();
    }

    @PutMapping("/questions/{id}/status")
    @ApiOperation("切换状态 - body: {status: 0|1}")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Question q = questionService.getById(id);
        if (q != null && params.get("status") != null) {
            q.setStatus(Integer.valueOf(params.get("status").toString()));
            questionService.updateById(q);
        }
        return Result.ok();
    }

    /**
     * Excel批量导入
     */
    @PostMapping("/questions/import")
    @ApiOperation("Excel批量导入")
    public Result<Integer> importExcel(@RequestParam("file") MultipartFile file,
                                        @RequestParam(required = false) Long courseId,
                                        @RequestParam(required = false) Long chapterId) {
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int importedCount = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String type = getCellValue(row, 0);
                String content = getCellValue(row, 1);
                String optionA = getCellValue(row, 2);
                String optionB = getCellValue(row, 3);
                String optionC = getCellValue(row, 4);
                String optionD = getCellValue(row, 5);
                String answer = getCellValue(row, 6);
                String analysis = getCellValue(row, 7);
                String scoreStr = getCellValue(row, 8);

                if (content == null || content.isEmpty()) continue;

                Question q = new Question();
                q.setCourseId(courseId);
                q.setChapterId(chapterId);
                q.setType(type != null ? type : "SINGLE");
                q.setContent(content);
                q.setAnswer(answer);
                q.setAnalysis(analysis);
                q.setScore(scoreStr != null && !scoreStr.isEmpty() ? Integer.parseInt(scoreStr) : 1);
                q.setStatus(1);
                questionService.save(q);

                if (optionA != null && !optionA.isEmpty()) saveOption(q.getId(), "A", optionA, "A".equals(answer));
                if (optionB != null && !optionB.isEmpty()) saveOption(q.getId(), "B", optionB, "B".equals(answer));
                if (optionC != null && !optionC.isEmpty()) saveOption(q.getId(), "C", optionC, "C".equals(answer));
                if (optionD != null && !optionD.isEmpty()) saveOption(q.getId(), "D", optionD, "D".equals(answer));

                importedCount++;
            }
            return Result.ok("成功导入" + importedCount + "道题目", importedCount);
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * JSON批量导入 - body: {questions: [{...}, ...]}
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/questions/batch")
    @ApiOperation("JSON批量导入")
    public Result<Integer> batchImport(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> questions = (List<Map<String, Object>>) params.get("questions");
        if (questions == null || questions.isEmpty()) {
            return Result.error("题目列表不能为空");
        }

        int importedCount = 0;
        for (Map<String, Object> qMap : questions) {
            try {
                Question q = new Question();
                if (qMap.get("courseId") != null) q.setCourseId(Long.valueOf(qMap.get("courseId").toString()));
                if (qMap.get("chapterId") != null) q.setChapterId(Long.valueOf(qMap.get("chapterId").toString()));
                q.setContent(qMap.get("content") != null ? qMap.get("content").toString() : "");
                q.setType(qMap.get("type") != null ? qMap.get("type").toString() : "SINGLE");
                q.setAnswer(qMap.get("answer") != null ? qMap.get("answer").toString() : null);
                q.setScore(qMap.get("score") != null ? Integer.parseInt(qMap.get("score").toString()) : 1);
                q.setStatus(1);
                questionService.save(q);

                if (qMap.get("options") instanceof List) {
                    List<String> options = (List<String>) qMap.get("options");
                    String[] labels = {"A", "B", "C", "D", "E", "F"};
                    for (int i = 0; i < options.size() && i < labels.length; i++) {
                        saveOption(q.getId(), labels[i], options.get(i), labels[i].equals(qMap.get("answer")));
                    }
                }
                importedCount++;
            } catch (Exception ignored) {}
        }
        return Result.ok("成功导入" + importedCount + "道题目", importedCount);
    }

    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }

    private void saveOption(Long questionId, String label, String content, boolean isCorrect) {
        QuestionOption option = new QuestionOption();
        option.setQuestionId(questionId);
        option.setOptionLabel(label);
        option.setContent(content);
        option.setIsCorrect(isCorrect ? 1 : 0);
        questionOptionService.save(option);
    }
}
