package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.Question;
import com.onlinetrain.entity.QuestionOption;
import com.onlinetrain.mapper.QuestionMapper;
import com.onlinetrain.service.QuestionOptionService;
import com.onlinetrain.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private QuestionOptionService questionOptionService;

    @Override
    public List<Question> getExamPaperQuestions(Long paperId) {
        List<Long> questionIds = jdbcTemplate.queryForList(
                "SELECT question_id FROM exam_paper_question WHERE exam_paper_id = ? ORDER BY sort_order",
                Long.class, paperId);

        List<Question> questions = new ArrayList<>();
        for (Long qid : questionIds) {
            Question q = getById(qid);
            if (q != null) {
                questions.add(q);
            }
        }
        return questions;
    }

    @Override
    public void enrichForDisplay(List<Question> questions) {
        if (questions == null || questions.isEmpty()) return;
        for (Question q : questions) {
            // 加载选项：仅单选/多选题需要从DB加载选项
            if ("SINGLE".equals(q.getType()) || "MULTIPLE".equals(q.getType())) {
                List<QuestionOption> opts = questionOptionService.lambdaQuery()
                        .eq(QuestionOption::getQuestionId, q.getId())
                        .orderByAsc(QuestionOption::getOptionLabel)
                        .list();
                q.setOptions(opts.stream().map(QuestionOption::getContent).collect(Collectors.toList()));
            }
            // 转换答案为索引格式
            q.setAnswer(toIndexAnswer(q));
        }
    }

    @Override
    public String toIndexAnswer(Question question) {
        if (question.getAnswer() == null) return null;
        String type = question.getType();
        String answer = question.getAnswer().trim();

        if ("JUDGE".equals(type)) {
            return "T".equalsIgnoreCase(answer) ? "0" : "1";
        }
        if ("ESSAY".equals(type)) {
            return answer;
        }
        if ("SINGLE".equals(type)) {
            // A->0, B->1, C->2, D->3 ...
            return String.valueOf(answer.toUpperCase().charAt(0) - 'A');
        }
        if ("MULTIPLE".equals(type)) {
            // ABD -> "0,1,3"
            return answer.toUpperCase().chars()
                    .mapToObj(c -> String.valueOf((char) c - 'A'))
                    .collect(Collectors.joining(","));
        }
        return answer;
    }
}
