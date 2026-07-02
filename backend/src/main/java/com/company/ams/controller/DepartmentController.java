package com.company.ams.controller;

import com.company.ams.common.ApiResponse;
import com.company.ams.dto.DepartmentRequest;
import com.company.ams.entity.Department;
import com.company.ams.security.AuthPrincipal;
import com.company.ams.security.Authz;
import com.company.ams.security.CurrentUser;
import com.company.ams.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final CurrentUser currentUser;
    private final Authz authz;

    @GetMapping
    public ApiResponse<List<Department>> findAll() {
        return ApiResponse.success(departmentService.findAll());
    }

    @PostMapping
    public ApiResponse<Department> create(@Valid @RequestBody DepartmentRequest request) {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        return ApiResponse.success(departmentService.create(request, me), "부서가 생성되었습니다.");
    }

    @PutMapping("/{id}")
    public ApiResponse<Department> update(@PathVariable Integer id, @Valid @RequestBody DepartmentRequest request) {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        return ApiResponse.success(departmentService.update(id, request, me), "부서가 수정되었습니다.");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        departmentService.delete(id, me);
        return ApiResponse.success(null, "부서가 삭제되었습니다.");
    }
}
