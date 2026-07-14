package com.company.ams.service;

import com.company.ams.entity.AuditLog;
import com.company.ams.mapper.AuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;

    @Transactional
    public void log(Integer userId, String action, String target, String detail) {
        doLog(userId, action, target, detail, "성공");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Integer userId, String action, String target, String detail, String result) {
        doLog(userId, action, target, detail, result);
    }

    private void doLog(Integer userId, String action, String target, String detail, String result) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setAction(action);
            auditLog.setTarget(target);
            auditLog.setDetail(detail);
            auditLog.setIpAddress(currentRequestIp());
            auditLog.setResult(result);
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("감사 로그 기록 실패: action={}, target={}", action, target, e);
        }
    }

    private String currentRequestIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
