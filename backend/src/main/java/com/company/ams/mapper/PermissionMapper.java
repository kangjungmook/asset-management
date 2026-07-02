package com.company.ams.mapper;

import com.company.ams.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper {

    List<Permission> findAll();

    Permission findById(@Param("permId") Integer permId);

    void insert(Permission permission);

    void update(Permission permission);

    void deleteById(@Param("permId") Integer permId);
}
