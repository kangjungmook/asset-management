package com.company.ams.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMapper {

    List<String> findRoleCodesByUserId(@Param("userId") Integer userId);

    boolean exists(@Param("userId") Integer userId, @Param("role") String role);

    void insert(@Param("userId") Integer userId, @Param("role") String role, @Param("grantedBy") Integer grantedBy);

    void delete(@Param("userId") Integer userId, @Param("role") String role);

    void deleteAllByUserId(@Param("userId") Integer userId);

    boolean existsGrantedBy(@Param("userId") Integer userId);
}
