package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Chapter;
import com.onlinetrain.entity.Course;
import com.onlinetrain.entity.Question;
import com.onlinetrain.entity.QuestionOption;
import com.onlinetrain.service.ChapterService;
import com.onlinetrain.service.CourseService;
import com.onlinetrain.service.QuestionOptionService;
import com.onlinetrain.service.QuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @Autowired
    private CourseService courseService;

    @Autowired
    private ChapterService chapterService;

    @GetMapping("/questions")
    @ApiOperation("题目列表")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Question> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Question> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (courseId != null) wrapper.eq(Question::getCourseId, courseId);
        if (chapterId != null) wrapper.eq(Question::getChapterId, chapterId);
        if (type != null && !type.isEmpty()) wrapper.eq(Question::getType, type);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Question::getContent, keyword);
        wrapper.orderByDesc(Question::getCreateTime);

        Page<Question> questionPage = questionService.page(pageParam, wrapper);

        // 填充课程名称和章节名称
        List<Map<String, Object>> enrichedRecords = new ArrayList<>();
        for (Question q : questionPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", q.getId());
            item.put("courseId", q.getCourseId());
            item.put("chapterId", q.getChapterId());
            item.put("type", q.getType());
            item.put("content", q.getContent());
            item.put("score", q.getScore());
            item.put("status", q.getStatus());
            item.put("createTime", q.getCreateTime());

            // 课程名称
            Course course = courseService.getById(q.getCourseId());
            item.put("courseName", course != null ? course.getTitle() : "未知课程");

            // 章节名称
            Chapter chapter = chapterService.getById(q.getChapterId());
            item.put("chapterName", chapter != null ? chapter.getTitle() : "未知章节");

            enrichedRecords.add(item);
        }

        return Result.ok(PageResult.of(enrichedRecords, questionPage.getTotal(), questionPage.getCurrent(), questionPage.getSize()));
    }

    @GetMapping("/questions/{id}")
    @ApiOperation("题目详情")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Question q = questionService.getById(id);
        if (q == null) return Result.notFound("题目不存在");
        
        // 加载选项
        List<QuestionOption> options = questionOptionService.lambdaQuery()
                .eq(QuestionOption::getQuestionId, id)
                .orderByAsc(QuestionOption::getOptionLabel)
                .list();
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", q.getId());
        result.put("courseId", q.getCourseId());
        result.put("chapterId", q.getChapterId());
        result.put("type", q.getType());
        result.put("content", q.getContent());
        result.put("answer", q.getAnswer());
        result.put("analysis", q.getAnalysis());
        result.put("score", q.getScore());
        result.put("status", q.getStatus());
        result.put("options", options.stream().map(o -> {
            Map<String, Object> opt = new HashMap<>();
            opt.put("optionLabel", o.getOptionLabel());
            opt.put("content", o.getContent());
            opt.put("isCorrect", o.getIsCorrect());
            return opt;
        }).collect(Collectors.toList()));
        
        return Result.ok(result);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/questions")
    @ApiOperation("创建题目")
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> params) {
        Question question = new Question();
        if (params.get("courseId") != null) question.setCourseId(Long.valueOf(params.get("courseId").toString()));
        if (params.get("chapterId") != null) question.setChapterId(Long.valueOf(params.get("chapterId").toString()));
        if (params.get("type") != null) question.setType(params.get("type").toString());
        if (params.get("content") != null) question.setContent(params.get("content").toString());
        if (params.get("analysis") != null) question.setAnalysis(params.get("analysis").toString());
        if (params.get("score") != null) question.setScore(Integer.parseInt(params.get("score").toString()));
        if (params.get("answer") != null) question.setAnswer(params.get("answer").toString());
        question.setStatus(1);
        questionService.save(question);

        // 保存选项
        if (params.get("options") instanceof List) {
            List<Map<String, Object>> options = (List<Map<String, Object>>) params.get("options");
            for (Map<String, Object> optMap : options) {
                if (optMap.get("content") != null && !optMap.get("content").toString().isEmpty()) {
                    QuestionOption option = new QuestionOption();
                    option.setQuestionId(question.getId());
                    option.setOptionLabel(optMap.get("optionLabel") != null ? optMap.get("optionLabel").toString() : "");
                    option.setContent(optMap.get("content").toString());
                    option.setIsCorrect(optMap.get("isCorrect") != null && (Boolean.parseBoolean(optMap.get("isCorrect").toString()) || "1".equals(optMap.get("isCorrect").toString())) ? 1 : 0);
                    questionOptionService.save(option);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", question.getId());
        return Result.ok(result);
    }

    @SuppressWarnings("unchecked")
    @PutMapping("/questions/{id}")
    @ApiOperation("更新题目")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Question question = questionService.getById(id);
        if (question == null) return Result.notFound("题目不存在");

        if (params.get("courseId") != null) question.setCourseId(Long.valueOf(params.get("courseId").toString()));
        if (params.get("chapterId") != null) question.setChapterId(Long.valueOf(params.get("chapterId").toString()));
        if (params.get("type") != null) question.setType(params.get("type").toString());
        if (params.get("content") != null) question.setContent(params.get("content").toString());
        if (params.get("analysis") != null) question.setAnalysis(params.get("analysis").toString());
        if (params.get("score") != null) question.setScore(Integer.parseInt(params.get("score").toString()));
        if (params.get("answer") != null) question.setAnswer(params.get("answer").toString());
        questionService.updateById(question);

        // 更新选项：先删后建
        if (params.containsKey("options")) {
            questionOptionService.lambdaUpdate().eq(QuestionOption::getQuestionId, id).remove();
            if (params.get("options") instanceof List) {
                List<Map<String, Object>> options = (List<Map<String, Object>>) params.get("options");
                for (Map<String, Object> optMap : options) {
                    if (optMap.get("content") != null && !optMap.get("content").toString().isEmpty()) {
                        QuestionOption option = new QuestionOption();
                        option.setQuestionId(id);
                        option.setOptionLabel(optMap.get("optionLabel") != null ? optMap.get("optionLabel").toString() : "");
                        option.setContent(optMap.get("content").toString());
                        option.setIsCorrect(optMap.get("isCorrect") != null && (Boolean.parseBoolean(optMap.get("isCorrect").toString()) || "1".equals(optMap.get("isCorrect").toString())) ? 1 : 0);
                        questionOptionService.save(option);
                    }
                }
            }
        }

        return Result.ok();
    }

    @DeleteMapping("/questions/{id}")
    @ApiOperation("删除题目")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.removeById(id);
        questionOptionService.lambdaUpdate().eq(QuestionOption::getQuestionId, id).remove();
        return Result.ok();
    }

    @SuppressWarnings("unchecked")
    @DeleteMapping("/questions/batch")
    @ApiOperation("批量删除题目")
    public Result<Integer> batchDelete(@RequestBody Map<String, Object> params) {
        List<Integer> idsRaw = (List<Integer>) params.get("ids");
        if (idsRaw == null || idsRaw.isEmpty()) {
            return Result.error("请选择要删除的题目");
        }
        List<Long> ids = idsRaw.stream().map(Integer::longValue).collect(Collectors.toList());
        // 先删除选项
        for (Long id : ids) {
            questionOptionService.lambdaUpdate().eq(QuestionOption::getQuestionId, id).remove();
        }
        questionService.removeByIds(ids);
        return Result.ok("成功删除" + ids.size() + "道题目", ids.size());
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
                q.setAnalysis(qMap.get("analysis") != null ? qMap.get("analysis").toString() : null);
                q.setDifficulty(qMap.get("difficulty") != null ? qMap.get("difficulty").toString() : null);
                q.setScore(qMap.get("score") != null ? Integer.parseInt(qMap.get("score").toString()) : 1);
                q.setStatus(1);
                questionService.save(q);

                if (qMap.get("options") instanceof List) {
                    List<?> options = (List<?>) qMap.get("options");
                    if (!options.isEmpty()) {
                        // 判断是结构化Map选项还是纯字符串选项
                        Object first = options.get(0);
                        if (first instanceof Map) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> structOpts = (List<Map<String, Object>>) options;
                            for (Map<String, Object> optMap : structOpts) {
                                String label = optMap.get("optionLabel") != null ? optMap.get("optionLabel").toString() : "";
                                String content = optMap.get("content") != null ? optMap.get("content").toString() : "";
                                if (!content.isEmpty()) {
                                    boolean correct = optMap.get("isCorrect") != null
                                            && (Boolean.parseBoolean(optMap.get("isCorrect").toString())
                                            || "1".equals(optMap.get("isCorrect").toString())
                                            || "true".equalsIgnoreCase(optMap.get("isCorrect").toString()));
                                    saveOption(q.getId(), label, content, correct);
                                }
                            }
                        } else {
                            // 纯字符串列表（旧Excel模式）
                            @SuppressWarnings("unchecked")
                            List<String> strOpts = (List<String>) options;
                            String[] labels = {"A", "B", "C", "D", "E", "F"};
                            for (int i = 0; i < strOpts.size() && i < labels.length; i++) {
                                saveOption(q.getId(), labels[i], strOpts.get(i), labels[i].equals(q.getAnswer()));
                            }
                        }
                    }
                }
                importedCount++;
            } catch (Exception ignored) {}
        }
        return Result.ok("成功导入" + importedCount + "道题目", importedCount);
    }

    /**
     * Word(.docx)批量导入
     * 格式示例:
     * 10. (知识点) 题目内容？（ ）
     * A. 选项A
     * B. 选项B
     * 答案：ABC
     * 解析：解析内容
     * 难度：困难
     */
    @PostMapping("/questions/import-word")
    @ApiOperation("Word批量导入")
    public Result<Map<String, Object>> importWord(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(required = false) Long courseId,
                                                   @RequestParam(required = false) Long chapterId,
                                                   @RequestParam(defaultValue = "false") Boolean previewOnly) {
        try (InputStream is = file.getInputStream();
             XWPFDocument doc = new XWPFDocument(is)) {

            // 提取所有段落文本
            List<String> lines = new ArrayList<>();
            for (XWPFParagraph para : doc.getParagraphs()) {
                String text = para.getText().trim();
                if (!text.isEmpty()) {
                    lines.add(text);
                } else if (!lines.isEmpty() && !lines.get(lines.size() - 1).isEmpty()) {
                    lines.add("");  // 保留空行作为分隔
                }
            }

            // 按空行分割成题目块
            List<List<String>> blocks = new ArrayList<>();
            List<String> currentBlock = new ArrayList<>();
            for (String line : lines) {
                if (line.isEmpty() && !currentBlock.isEmpty()) {
                    blocks.add(new ArrayList<>(currentBlock));
                    currentBlock.clear();
                } else if (!line.isEmpty()) {
                    currentBlock.add(line);
                }
            }
            if (!currentBlock.isEmpty()) {
                blocks.add(currentBlock);
            }

            List<Map<String, Object>> parsedQuestions = new ArrayList<>();
            int errors = 0;

            for (List<String> block : blocks) {
                try {
                    Map<String, Object> parsed = parseQuestionBlock(block);
                    if (parsed != null) {
                        parsedQuestions.add(parsed);
                    } else {
                        errors++;
                    }
                } catch (Exception e) {
                    errors++;
                }
            }

            // 如果是预览模式，直接返回解析结果
            if (previewOnly) {
                Map<String, Object> result = new HashMap<>();
                result.put("total", parsedQuestions.size());
                result.put("errors", errors);
                result.put("preview", parsedQuestions);
                return Result.ok("解析成功，共" + parsedQuestions.size() + "道题目", result);
            }

            // 导入题目
            int importedCount = 0;
            for (Map<String, Object> qMap : parsedQuestions) {
                try {
                    Question q = new Question();
                    q.setCourseId(courseId);
                    q.setChapterId(chapterId);
                    q.setContent(qMap.get("content") != null ? qMap.get("content").toString() : "");
                    q.setType(qMap.get("type") != null ? qMap.get("type").toString() : "SINGLE");
                    q.setAnswer(qMap.get("answer") != null ? qMap.get("answer").toString() : null);
                    q.setAnalysis(qMap.get("analysis") != null ? qMap.get("analysis").toString() : null);
                    q.setDifficulty(qMap.get("difficulty") != null ? qMap.get("difficulty").toString() : null);
                    q.setScore(qMap.get("score") != null ? Integer.parseInt(qMap.get("score").toString()) : 5);
                    q.setStatus(1);
                    questionService.save(q);

                    // 保存选项
                    if (qMap.get("options") instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> options = (List<Map<String, Object>>) qMap.get("options");
                        String answer = (String) qMap.get("answer");
                        for (Map<String, Object> opt : options) {
                            String label = (String) opt.get("label");
                            String content = (String) opt.get("content");
                            if (content != null && !content.isEmpty()) {
                                boolean correct = answer != null && answer.contains(label);
                                saveOption(q.getId(), label, content, correct);
                            }
                        }
                    }
                    importedCount++;
                } catch (Exception ignored) {}
            }

            Map<String, Object> result = new HashMap<>();
            result.put("imported", importedCount);
            result.put("total", parsedQuestions.size());
            result.put("errors", errors);
            result.put("preview", parsedQuestions);
            return Result.ok("成功导入" + importedCount + "道题目", result);
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 解析单个题目块
     */
    private Map<String, Object> parseQuestionBlock(List<String> lines) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> options = new ArrayList<>();

        String questionLine = null;
        String answer = null;
        String analysis = null;
        String difficulty = null;
        int optionStartIdx = -1;

        // Pattern for matching option lines: "A. xxx" or "A．xxx" or "A、xxx"
        Pattern optionPattern = Pattern.compile("^([A-H])\\s*[.．、]\\s*(.+)");
        Pattern answerPattern = Pattern.compile("^答案[：:]\\s*(.+)");
        Pattern analysisPattern = Pattern.compile("^解析[：:]\\s*(.+)");
        Pattern difficultyPattern = Pattern.compile("^难度[：:]\\s*(.+)");

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            Matcher am = answerPattern.matcher(line);
            if (am.find()) {
                answer = am.group(1).trim();
                continue;
            }

            Matcher anm = analysisPattern.matcher(line);
            if (anm.find()) {
                analysis = anm.group(1).trim();
                continue;
            }

            Matcher dm = difficultyPattern.matcher(line);
            if (dm.find()) {
                difficulty = dm.group(1).trim();
                continue;
            }

            Matcher om = optionPattern.matcher(line);
            if (om.find()) {
                if (optionStartIdx < 0) optionStartIdx = i;
                Map<String, Object> opt = new HashMap<>();
                opt.put("label", om.group(1));
                opt.put("content", om.group(2).trim());
                options.add(opt);
                continue;
            }

            // 如果不是以上任何特殊行，且还没找到选项，那就是题目行
            if (optionStartIdx < 0) {
                questionLine = line;
            }
        }

        if (questionLine == null || questionLine.isEmpty()) return null;

        // 清理题目行：去掉前面的题号
        String content = questionLine.replaceFirst("^\\d+\\s*[.．、]?\\s*", "");
        // 去掉开头可能的知识点括号（支持全角/半角，兼容前导空格）
        content = content.replaceFirst("^\\s*[（(][^）)]*[）)]\\s*", "");
        // 去掉末尾的（ ）
        content = content.replaceAll("[（(]\\s*[）)]$", "").trim();

        if (content.isEmpty()) return null;

        // 判断题型：去掉分隔符后，答案包含多个字母为多选
        // 支持 "ABC"、"A,B,C"、"A、B、C"、"A B C" 等格式
        String cleanedAnswer = answer != null ? answer.replaceAll("[\\s,，、;；]+", "") : "";

        // 先检测判断题
        boolean isJudge = false;
        if (answer != null) {
            String upperAnswer = answer.toUpperCase().trim();
            // 答案直接是判断词（"正确"/"错误" 或单字"对/错/√/×/是/否/T/F"）
            if (answer.matches("^(正确|错误|对|错|√|×|是|否)$") || upperAnswer.matches("^[TF]$")) {
                isJudge = true;
            }
        }
        // 选项只有2个且内容包含判断题典型表述（如 A.正确 B.错误），也识别为判断题
        if (!isJudge && options.size() == 2 && cleanedAnswer.length() == 1 && cleanedAnswer.matches("[A-H]")) {
            String optAContent = "", optBContent = "";
            for (Map<String, Object> opt : options) {
                String label = (String) opt.get("label");
                String optContent = (String) opt.get("content");
                if ("A".equals(label)) { optAContent = optContent; }
                if ("B".equals(label)) { optBContent = optContent; }
            }
            if (optAContent != null && optBContent != null) {
                String aUp = optAContent.trim().toUpperCase();
                String bUp = optBContent.trim().toUpperCase();
                // 选项内容是典型判断题表述
                boolean aIsTrue = aUp.contains("正确") || aUp.contains("对") || aUp.contains("√") || aUp.equals("T") || aUp.equals("TRUE") || aUp.contains("是");
                boolean bIsFalse = bUp.contains("错误") || bUp.contains("错") || bUp.contains("×") || bUp.equals("F") || bUp.equals("FALSE") || bUp.contains("否");
                if (aIsTrue && bIsFalse) {
                    isJudge = true;
                }
            }
        }

        String type;
        if (isJudge) {
            type = "JUDGE";
            // 判断题答案标准化为 T/F
            if (answer != null) {
                String upperAnswer = answer.toUpperCase().trim();
                if (answer.matches("^[正确对√是]$") || upperAnswer.equals("T") || upperAnswer.equals("TRUE") || upperAnswer.equals("A")) {
                    answer = "T";
                } else if (answer.matches("^[错误错×否]$") || upperAnswer.equals("F") || upperAnswer.equals("FALSE") || upperAnswer.equals("B")) {
                    answer = "F";
                }
            }
        } else if (cleanedAnswer.length() > 1 && cleanedAnswer.matches("[A-H]+")) {
            type = "MULTIPLE";
        } else {
            type = "SINGLE";
        }

        result.put("content", content);
        result.put("type", type);
        result.put("answer", answer);
        result.put("analysis", analysis);
        result.put("difficulty", difficulty);
        result.put("options", options);
        result.put("score", 5);

        return result;
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
