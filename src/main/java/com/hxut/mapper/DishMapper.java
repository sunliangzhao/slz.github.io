package com.hxut.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxut.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
