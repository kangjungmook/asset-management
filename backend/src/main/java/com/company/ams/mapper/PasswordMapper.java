package com.company.ams.mapper;

import com.company.ams.entity.PasswordEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PasswordMapper {

    List<PasswordEntry> findAll(@Param("deptId") Integer deptId, @Param("userId") Integer userId,
                                 @Param("keyword") String keyword,
                                 @Param("offset") int offset, @Param("limit") int limit);

    long countAll(@Param("deptId") Integer deptId, @Param("userId") Integer userId, @Param("keyword") String keyword);

    PasswordEntry findById(@Param("pwId") Integer pwId);

    List<PasswordEntry> findExpiring(@Param("deptId") Integer deptId, @Param("userId") Integer userId,
                                      @Param("withinDays") int withinDays);

    void insert(PasswordEntry entry);

    void update(PasswordEntry entry);

    void deleteById(@Param("pwId") Integer pwId);

    boolean existsByUserId(@Param("userId") Integer userId);

    void nullifyRequester(@Param("userId") Integer userId);

    void nullifyApprover(@Param("userId") Integer userId);

    void nullifyConfirmedBy(@Param("userId") Integer userId);
}
