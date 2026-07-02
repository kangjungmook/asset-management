package com.company.ams.mapper;

import com.company.ams.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    List<Department> findAll();

    Department findById(@Param("deptId") Integer deptId);

    void insert(Department department);

    void update(Department department);

    void deleteById(@Param("deptId") Integer deptId);

    int countUsersByDeptId(@Param("deptId") Integer deptId);
}
