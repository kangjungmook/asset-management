package com.company.ams.service;

import com.company.ams.entity.AuditLog;
import com.company.ams.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Integer userId, String action, String target, String detail) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setAction(action);
            auditLog.setTarget(target);
            auditLog.setDetail(detail);
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("감사 로그 기록 실패: action={}, target={}", action, target, e);
        }
    }
}
