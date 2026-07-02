package com.company.ams.mapper;

import com.company.ams.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuditLogMapper {

    void insert(AuditLog auditLog);

    List<AuditLog> search(@Param("userId") Integer userId,
                           @Param("action") String action,
                           @Param("from") String from,
                           @Param("to") String to,
                           @Param("offset") int offset,
                           @Param("limit") int limit);

    long countSearch(@Param("userId") Integer userId,
                      @Param("action") String action,
                      @Param("from") String from,
                      @Param("to") String to);

    void nullifyUser(@Param("userId") Integer userId);
}
