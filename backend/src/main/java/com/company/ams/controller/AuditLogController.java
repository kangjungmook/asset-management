package com.company.ams.controller;

import com.company.ams.common.ApiResponse;
import com.company.ams.dto.PageResponse;
import com.company.ams.entity.AuditLog;
import com.company.ams.mapper.AuditLogMapper;
import com.company.ams.security.AuthPrincipal;
import com.company.ams.security.Authz;
import com.company.ams.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogMapper auditLogMapper;
    private final CurrentUser currentUser;
    private final Authz authz;

    @GetMapping
    public ApiResponse<PageResponse<AuditLog>> search(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);

        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;

        List<AuditLog> content = auditLogMapper.search(userId, action, from, to, offset, safeSize);
        long total = auditLogMapper.countSearch(userId, action, from, to);

        return ApiResponse.success(new PageResponse<>(content, safePage, safeSize, total));
    }
}
