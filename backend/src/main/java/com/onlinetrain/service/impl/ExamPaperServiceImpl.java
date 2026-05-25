package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.ExamPaper;
import com.onlinetrain.mapper.ExamPaperMapper;
import com.onlinetrain.service.ExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExamPaperServiceImpl extends ServiceImpl<ExamPaperMapper, ExamPaper> implements ExamPaperService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void savePaperQuestions(Long paperId, List<Long> questionIds) {
        // 删除旧的关联
        jdbcTemplate.update("DELETE FROM exam_paper_question WHERE exam_paper_id = ?", paperId);

        // 插入新的关联
        int order = 0;
        for (Long qid : questionIds) {
            jdbcTemplate.update(
                    "INSERT INTO exam_paper_question (exam_paper_id, question_id, sort_order) VALUES (?, ?, ?)",
                    paperId, qid, order++);
        }
    }
}
