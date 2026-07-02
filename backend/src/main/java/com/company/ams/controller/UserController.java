package com.company.ams.controller;

import com.company.ams.common.ApiResponse;
import com.company.ams.common.exception.ForbiddenException;
import com.company.ams.common.exception.NotFoundException;
import com.company.ams.dto.PermissionGrantRequest;
import com.company.ams.dto.RoleGrantRequest;
import com.company.ams.dto.TempPasswordResponse;
import com.company.ams.dto.UserCreateRequest;
import com.company.ams.dto.UserDetailResponse;
import com.company.ams.dto.UserUpdateRequest;
import com.company.ams.entity.User;
import com.company.ams.mapper.UserMapper;
import com.company.ams.security.AuthPrincipal;
import com.company.ams.security.Authz;
import com.company.ams.security.CurrentUser;
import com.company.ams.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final CurrentUser currentUser;
    private final Authz authz;

    @GetMapping
    public ApiResponse<List<User>> findAll(@RequestParam(required = false) String keyword) {
        AuthPrincipal me = currentUser.get();
        if (me.isAdmin()) {
            return ApiResponse.success(userService.findAll(null, keyword));
        }
        if (me.hasRole("DEPT_MANAGER")) {
            return ApiResponse.success(userService.findAll(me.getDeptId(), keyword));
        }
        throw new ForbiddenException();
    }

    @GetMapping("/me")
    public ApiResponse<UserDetailResponse> me() {
        AuthPrincipal me = currentUser.get();
        return ApiResponse.success(userService.findDetail(me.getUserId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDetailResponse> findById(@PathVariable Integer id) {
        AuthPrincipal me = currentUser.get();
        User target = userMapper.findById(id);
        if (target == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        authz.requireAdminOrManagerOrSelf(me, target.getUserId(), target.getDeptId());
        return ApiResponse.success(userService.findDetail(id));
    }

    @PostMapping
    public ApiResponse<User> create(@Valid @RequestBody UserCreateRequest request) {
        AuthPrincipal me = currentUser.get();
        if (me.isAdmin()) {
            // 통과
        } else if (me.hasRole("DEPT_MANAGER")) {
            if (request.getDeptId() == null || !request.getDeptId().equals(me.getDeptId())) {
                throw new ForbiddenException();
            }
        } else {
            throw new ForbiddenException();
        }
        return ApiResponse.success(userService.create(request, me), "사용자가 생성되었습니다.");
    }

    @PutMapping("/{id}")
    public ApiResponse<User> update(@PathVariable Integer id, @Valid @RequestBody UserUpdateRequest request) {
        AuthPrincipal me = currentUser.get();
        User target = userMapper.findById(id);
        if (target == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        authz.requireAdminOrManagerOfDept(me, target.getDeptId());
        return ApiResponse.success(userService.update(id, request, me), "사용자가 수정되었습니다.");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        AuthPrincipal me = currentUser.get();
        User target = userMapper.findById(id);
        if (target == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        authz.requireAdminOrManagerOfDept(me, target.getDeptId());
        userService.delete(id, me);
        return ApiResponse.success(null, "사용자가 삭제되었습니다.");
    }

    @PutMapping("/{id}/deactivate")
    public ApiResponse<Void> deactivate(@PathVariable Integer id) {
        AuthPrincipal me = currentUser.get();
        User target = userMapper.findById(id);
        if (target == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        authz.requireAdminOrManagerOfDept(me, target.getDeptId());
        userService.deactivate(id, me);
        return ApiResponse.success(null, "사용자가 비활성화되었습니다.");
    }

    @PutMapping("/{id}/activate")
    public ApiResponse<Void> activate(@PathVariable Integer id) {
        AuthPrincipal me = currentUser.get();
        User target = userMapper.findById(id);
        if (target == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        authz.requireAdminOrManagerOfDept(me, target.getDeptId());
        userService.activate(id, me);
        return ApiResponse.success(null, "사용자가 활성화되었습니다.");
    }

    @PostMapping("/{id}/roles")
    public ApiResponse<Void> grantRole(@PathVariable Integer id, @Valid @RequestBody RoleGrantRequest request) {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        userService.grantRole(id, request, me);
        return ApiResponse.success(null, "역할이 부여되었습니다.");
    }

    @DeleteMapping("/{id}/roles/{role}")
    public ApiResponse<Void> revokeRole(@PathVariable Integer id, @PathVariable String role) {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        userService.revokeRole(id, role, me);
        return ApiResponse.success(null, "역할이 해제되었습니다.");
    }

    @PostMapping("/{id}/permissions")
    public ApiResponse<Void> grantPermission(@PathVariable Integer id, @Valid @RequestBody PermissionGrantRequest request) {
        AuthPrincipal me = currentUser.get();
        User target = userMapper.findById(id);
        if (target == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        authz.requireAdminOrManagerOfDept(me, target.getDeptId());
        userService.grantPermission(id, request, me);
        return ApiResponse.success(null, "권한이 부여되었습니다.");
    }

    @DeleteMapping("/{id}/permissions/{permId}")
    public ApiResponse<Void> revokePermission(@PathVariable Integer id, @PathVariable Integer permId) {
        AuthPrincipal me = currentUser.get();
        User target = userMapper.findById(id);
        if (target == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        authz.requireAdminOrManagerOfDept(me, target.getDeptId());
        userService.revokePermission(id, permId, me);
        return ApiResponse.success(null, "권한이 해제되었습니다.");
    }
}
