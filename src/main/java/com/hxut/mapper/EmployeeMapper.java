package com.hxut.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxut.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
