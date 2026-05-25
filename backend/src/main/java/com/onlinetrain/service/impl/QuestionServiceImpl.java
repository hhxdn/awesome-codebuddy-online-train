package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.Question;
import com.onlinetrain.mapper.QuestionMapper;
import com.onlinetrain.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Question> getExamPaperQuestions(Long paperId) {
        List<Long> questionIds = jdbcTemplate.queryForList(
                "SELECT question_id FROM exam_paper_question WHERE exam_paper_id = ? ORDER BY sort_order",
                Long.class, paperId);

        List<Question> questions = new ArrayList<>();
        for (Long qid : questionIds) {
            Question q = getById(qid);
            if (q != null && q.getDeleted() == 0) {
                questions.add(q);
            }
        }
        return questions;
    }
}
