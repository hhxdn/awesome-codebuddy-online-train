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

/**
 * 管理端-题库管理控制器
 */
@RestController
@RequestMapping("/api/admin/questions")
@Api(tags = "管理端-题库管理")
public class AdminQuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionOptionService questionOptionService;

    /**
     * 题目列表
     */
    @GetMapping
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

    /**
     * 创建题目
     */
    @PostMapping
    @ApiOperation("创建题目")
    public Result<Question> create(@RequestBody Question question) {
        questionService.save(question);
        return Result.ok(question);
    }

    /**
     * 更新题目
     */
    @PutMapping("/{id}")
    @ApiOperation("更新题目")
    public Result<Question> update(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        questionService.updateById(question);
        return Result.ok(question);
    }

    /**
     * 删除题目（同时删除选项）
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除题目")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.removeById(id);
        questionOptionService.lambdaUpdate().eq(QuestionOption::getQuestionId, id).remove();
        return Result.ok();
    }

    /**
     * 切换题目状态
     */
    @PutMapping("/{id}/status")
    @ApiOperation("切换状态")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        Question q = questionService.getById(id);
        if (q != null) {
            q.setStatus(q.getStatus() == 1 ? 0 : 1);
            questionService.updateById(q);
        }
        return Result.ok();
    }

    /**
     * Excel批量导入题目
     */
    @PostMapping("/import")
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
