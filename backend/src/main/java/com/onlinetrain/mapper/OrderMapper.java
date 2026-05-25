package com.onlinetrain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlinetrain.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
