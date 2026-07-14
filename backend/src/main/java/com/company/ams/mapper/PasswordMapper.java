package com.company.ams.mapper;

import com.company.ams.entity.PasswordEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PasswordMapper {

    List<PasswordEntry> findAll(@Param("deptId") Integer deptId, @Param("userId") Integer userId,
                                 @Param("keyword") String keyword,
                                 @Param("typeId") Integer typeId, @Param("status") String status,
                                 @Param("offset") int offset, @Param("limit") int limit);

    long countAll(@Param("deptId") Integer deptId, @Param("userId") Integer userId, @Param("keyword") String keyword,
                  @Param("typeId") Integer typeId, @Param("status") String status);

    Map<String, Object> countByExpiryStatus(@Param("deptId") Integer deptId, @Param("userId") Integer userId);

    PasswordEntry findById(@Param("pwId") Integer pwId);

    List<PasswordEntry> findExpiring(@Param("deptId") Integer deptId, @Param("userId") Integer userId,
                                      @Param("withinDays") int withinDays);

    void insert(PasswordEntry entry);

    void update(PasswordEntry entry);

    void deleteById(@Param("pwId") Integer pwId);

    boolean existsByUserId(@Param("userId") Integer userId);

    void approve(@Param("pwId") Integer pwId, @Param("approverName") String approverName,
                 @Param("approvedAt") java.time.LocalDateTime approvedAt);

    void reject(@Param("pwId") Integer pwId, @Param("rejectedBy") String rejectedBy,
                @Param("rejectedAt") java.time.LocalDateTime rejectedAt, @Param("rejectReason") String rejectReason);

    void resetToPending(@Param("pwId") Integer pwId);
}
