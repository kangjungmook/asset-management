package com.company.ams.controller;

import com.company.ams.common.ApiResponse;
import com.company.ams.common.exception.NotFoundException;
import com.company.ams.dto.PageResponse;
import com.company.ams.dto.PasswordRejectRequest;
import com.company.ams.dto.PasswordRequest;
import com.company.ams.entity.PasswordEntry;
import com.company.ams.entity.User;
import com.company.ams.mapper.UserMapper;
import com.company.ams.security.AuthPrincipal;
import com.company.ams.security.Authz;
import com.company.ams.security.CurrentUser;
import com.company.ams.service.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passwords")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;
    private final UserMapper userMapper;
    private final CurrentUser currentUser;
    private final Authz authz;

    @GetMapping
    public ApiResponse<PageResponse<PasswordEntry>> findAll(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer typeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        AuthPrincipal me = currentUser.get();
        if (me.isAdmin()) {
            return ApiResponse.success(passwordService.findAll(null, userId, keyword, typeId, status, page, size));
        }
        if (me.hasRole("DEPT_MANAGER")) {
            return ApiResponse.success(passwordService.findAll(me.getDeptId(), userId, keyword, typeId, status, page, size));
        }
        return ApiResponse.success(passwordService.findAll(null, me.getUserId(), keyword, typeId, status, page, size));
    }

    @GetMapping("/expiring")
    public ApiResponse<List<PasswordEntry>> findExpiring() {
        AuthPrincipal me = currentUser.get();
        if (me.isAdmin()) {
            return ApiResponse.success(passwordService.findExpiring(null, null));
        }
        if (me.hasRole("DEPT_MANAGER")) {
            return ApiResponse.success(passwordService.findExpiring(me.getDeptId(), null));
        }
        return ApiResponse.success(passwordService.findExpiring(null, me.getUserId()));
    }

    @GetMapping("/summary")
    public ApiResponse<com.company.ams.dto.PasswordSummaryResponse> summary() {
        AuthPrincipal me = currentUser.get();
        if (me.isAdmin()) {
            return ApiResponse.success(passwordService.getSummary(null, null));
        }
        if (me.hasRole("DEPT_MANAGER")) {
            return ApiResponse.success(passwordService.getSummary(me.getDeptId(), null));
        }
        return ApiResponse.success(passwordService.getSummary(null, me.getUserId()));
    }

    @PostMapping
    public ApiResponse<PasswordEntry> create(@  Valid @RequestBody PasswordRequest request) {
        AuthPrincipal me = currentUser.get();
        User target = findUser(request.getUserId());
        authz.requirePasswordAccess(me, target.getUserId(), target.getDeptId());
        return ApiResponse.success(passwordService.create(request, me), "패스워드 관리대장이 등록되었습니다.");
    }

    @PutMapping("/{id}")
    public ApiResponse<PasswordEntry> update(@PathVariable Integer id, @Valid @RequestBody PasswordRequest request) {
        AuthPrincipal me = currentUser.get();
        PasswordEntry entry = passwordService.findById(id);
        authz.requirePasswordAccess(me, entry.getUserId(), entry.getUserDeptId());
        User target = findUser(request.getUserId());
        authz.requirePasswordAccess(me, target.getUserId(), target.getDeptId());
        return ApiResponse.success(passwordService.update(id, request, me), "패스워드 관리대장이 수정되었습니다.");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        AuthPrincipal me = currentUser.get();
        PasswordEntry entry = passwordService.findById(id);
        authz.requirePasswordAccess(me, entry.getUserId(), entry.getUserDeptId());
        passwordService.delete(id, me);
        return ApiResponse.success(null, "패스워드 관리대장이 삭제되었습니다.");
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<PasswordEntry> approve(@PathVariable Integer id) {
        AuthPrincipal me = currentUser.get();
        PasswordEntry entry = passwordService.findById(id);
        authz.requirePasswordApprove(me, entry.getUserDeptId());
        return ApiResponse.success(passwordService.approve(id, me), "승인되었습니다.");
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<PasswordEntry> reject(@PathVariable Integer id, @Valid @RequestBody PasswordRejectRequest request) {
        AuthPrincipal me = currentUser.get();
        PasswordEntry entry = passwordService.findById(id);
        authz.requirePasswordApprove(me, entry.getUserDeptId());
        return ApiResponse.success(passwordService.reject(id, request, me), "반려되었습니다.");
    }

    private User findUser(Integer userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException("대상 사용자를 찾을 수 없습니다.");
        }
        return user;
    }
}
