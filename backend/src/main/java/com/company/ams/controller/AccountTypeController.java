package com.company.ams.controller;

import com.company.ams.common.ApiResponse;
import com.company.ams.dto.AccountTypeRequest;
import com.company.ams.entity.AccountType;
import com.company.ams.security.AuthPrincipal;
import com.company.ams.security.Authz;
import com.company.ams.security.CurrentUser;
import com.company.ams.service.AccountTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account-types")
@RequiredArgsConstructor
public class AccountTypeController {

    private final AccountTypeService accountTypeService;
    private final CurrentUser currentUser;
    private final Authz authz;

    @GetMapping
    public ApiResponse<List<AccountType>> findAll() {
        return ApiResponse.success(accountTypeService.findAll());
    }

    @PostMapping
    public ApiResponse<AccountType> create(@Valid @RequestBody AccountTypeRequest request) {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        return ApiResponse.success(accountTypeService.create(request, me), "계정유형이 생성되었습니다.");
    }

    @PutMapping("/{id}")
    public ApiResponse<AccountType> update(@PathVariable Integer id, @Valid @RequestBody AccountTypeRequest request) {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        return ApiResponse.success(accountTypeService.update(id, request, me), "계정유형이 수정되었습니다.");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        AuthPrincipal me = currentUser.get();
        authz.requireAdmin(me);
        accountTypeService.delete(id, me);
        return ApiResponse.success(null, "계정유형이 삭제되었습니다.");
    }
}
