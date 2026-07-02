package com.company.ams.mapper;

import com.company.ams.entity.UserPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPermissionMapper {

    List<UserPermission> findByUserId(@Param("userId") Integer userId);

    List<String> findPermCodesByUserId(@Param("userId") Integer userId);

    boolean exists(@Param("userId") Integer userId, @Param("permId") Integer permId);

    void insert(@Param("userId") Integer userId, @Param("permId") Integer permId, @Param("grantedBy") Integer grantedBy);

    void delete(@Param("userId") Integer userId, @Param("permId") Integer permId);

    void deleteAllByUserId(@Param("userId") Integer userId);

    boolean existsGrantedBy(@Param("userId") Integer userId);
}
