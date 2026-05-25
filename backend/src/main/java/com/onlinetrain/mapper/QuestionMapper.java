package com.onlinetrain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlinetrain.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
