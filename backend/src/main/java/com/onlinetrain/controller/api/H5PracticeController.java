package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Question;
import com.onlinetrain.entity.WrongQuestion;
import com.onlinetrain.service.QuestionService;
import com.onlinetrain.service.WrongQuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * H5练习控制器
 */
@RestController
@RequestMapping("/api/practice")
@Api(tags = "H5-练习接口")
public class H5PracticeController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private WrongQuestionService wrongQuestionService;

    /**
     * 提交练习答案
     */
    @PostMapping("/submit/{chapterId}")
    @ApiOperation("提交练习答案")
    public Result<Map<String, Object>> submitPractice(
            @PathVariable Long chapterId,
            @RequestBody List<Map<String, Object>> answers,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        List<Question> questions = questionService.lambdaQuery()
                .eq(Question::getChapterId, chapterId)
                .eq(Question::getStatus, 1)
                .list();

        int totalScore = 0;
        int rightCount = 0;
        int totalCount = questions.size();

        for (Question question : questions) {
            // 查找用户对此题的答案
            String userAnswer = null;
            for (Map<String, Object> ans : answers) {
                Object qidObj = ans.get("questionId");
                if (qidObj != null) {
                    Long qid = Long.valueOf(qidObj.toString());
                    if (qid.equals(question.getId())) {
                        Object answerObj = ans.get("answer");
                        userAnswer = answerObj != null ? answerObj.toString() : "";
                        break;
                    }
                }
            }

            if (userAnswer != null && userAnswer.equals(question.getAnswer())) {
                rightCount++;
                totalScore += (question.getScore() != null ? question.getScore() : 1);
            } else {
                // 记录错题
                WrongQuestion existing = wrongQuestionService.lambdaQuery()
                        .eq(WrongQuestion::getUserId, userId)
                        .eq(WrongQuestion::getQuestionId, question.getId())
                        .one();
                if (existing != null) {
                    existing.setWrongCount(existing.getWrongCount() + 1);
                    existing.setLastWrongTime(LocalDateTime.now());
                    wrongQuestionService.updateById(existing);
                } else {
                    WrongQuestion wq = new WrongQuestion();
                    wq.setUserId(userId);
                    wq.setQuestionId(question.getId());
                    wq.setWrongCount(1);
                    wq.setLastWrongTime(LocalDateTime.now());
                    wrongQuestionService.save(wq);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("rightCount", rightCount);
        result.put("totalScore", totalScore);
        result.put("accuracy", totalCount > 0 ? (double) rightCount / totalCount * 100 : 0);

        return Result.ok(result);
    }

    /**
     * 练习历史
     */
    @GetMapping("/records/{chapterId}")
    @ApiOperation("章节练习历史")
    public Result<List<Question>> practiceRecords(@PathVariable Long chapterId) {
        List<Question> questions = questionService.lambdaQuery()
                .eq(Question::getChapterId, chapterId)
                .list();
        questions.forEach(q -> q.setAnswer(null));
        return Result.ok(questions);
    }
}
