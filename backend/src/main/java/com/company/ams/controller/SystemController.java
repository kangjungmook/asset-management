package com.company.ams.controller;

import com.company.ams.common.ApiResponse;
import com.company.ams.dto.SystemResetRequest;
import com.company.ams.security.AuthPrincipal;
import com.company.ams.security.Authz;
import com.company.ams.security.CurrentUser;
import com.company.ams.service.SystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;
    private final CurrentUser currentUser;
    private final Authz authz;

    @PostMapping("/reset")
    public ApiResponse<Void> reset(@Valid @RequestBody SystemResetRequest request) {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        systemService.reset(request.getConfirm(), me);
        return ApiResponse.success(null, "시스템 데이터가 초기화되었습니다.");
    }

    @PostMapping("/reset/users")
    public ApiResponse<Void> resetUsers() {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        systemService.resetUsers(me);
        return ApiResponse.success(null, "사용자가 초기화되었습니다.");
    }

    @PostMapping("/reset/departments")
    public ApiResponse<Void> resetDepartments() {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        systemService.resetDepartments(me);
        return ApiResponse.success(null, "부서가 초기화되었습니다.");
    }

    @PostMapping("/reset/permissions")
    public ApiResponse<Void> resetPermissions() {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        systemService.resetPermissions(me);
        return ApiResponse.success(null, "권한이 초기화되었습니다.");
    }

    @PostMapping("/reset/account-types")
    public ApiResponse<Void> resetAccountTypes() {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        systemService.resetAccountTypes(me);
        return ApiResponse.success(null, "계정유형이 초기화되었습니다.");
    }

    @PostMapping("/reset/passwords")
    public ApiResponse<Void> resetPasswords() {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        systemService.resetPasswords(me);
        return ApiResponse.success(null, "패스워드 관리대장이 초기화되었습니다.");
    }

    @PostMapping("/reset/audit-logs")
    public ApiResponse<Void> resetAuditLogs() {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        systemService.resetAuditLogs(me);
        return ApiResponse.success(null, "감사 로그가 초기화되었습니다.");
    }
}
