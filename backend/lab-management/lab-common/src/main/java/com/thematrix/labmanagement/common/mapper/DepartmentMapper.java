package com.thematrix.labmanagement.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thematrix.labmanagement.common.entity.Department;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
