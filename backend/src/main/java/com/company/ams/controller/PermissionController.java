package com.company.ams.controller;

import com.company.ams.common.ApiResponse;
import com.company.ams.dto.PermissionRequest;
import com.company.ams.entity.Permission;
import com.company.ams.security.AuthPrincipal;
import com.company.ams.security.Authz;
import com.company.ams.security.CurrentUser;
import com.company.ams.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final CurrentUser currentUser;
    private final Authz authz;

    @GetMapping
    public ApiResponse<List<Permission>> findAll() {
        return ApiResponse.success(permissionService.findAll());
    }

    @PostMapping
    public ApiResponse<Permission> create(@Valid @RequestBody PermissionRequest request) {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        return ApiResponse.success(permissionService.create(request, me), "권한이 생성되었습니다.");
    }

    @PutMapping("/{id}")
    public ApiResponse<Permission> update(@PathVariable Integer id, @Valid @RequestBody PermissionRequest request) {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        return ApiResponse.success(permissionService.update(id, request, me), "권한이 수정되었습니다.");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        permissionService.delete(id, me);
        return ApiResponse.success(null, "권한이 삭제되었습니다.");
    }
}
