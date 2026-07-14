package com.company.ams.mapper;

import com.company.ams.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User findByEmployeeNo(@Param("employeeNo") String employeeNo);

    User findById(@Param("userId") Integer userId);

    List<User> findAll(@Param("deptId") Integer deptId, @Param("keyword") String keyword);

    boolean existsByEmployeeNo(@Param("employeeNo") String employeeNo);

    void insert(User user);

    void update(User user);

    void updateActive(@Param("userId") Integer userId, @Param("isActive") boolean isActive);

    void updatePassword(@Param("userId") Integer userId, @Param("password") String password,
                         @Param("mustChangePassword") boolean mustChangePassword);

    void deleteById(@Param("userId") Integer userId);

    void updateLastLogin(@Param("userId") Integer userId);

    int countActiveAdmins();

    List<Integer> findApprovalRecipientUserIds(@Param("deptId") Integer deptId, @Param("excludeUserId") Integer excludeUserId);
}
