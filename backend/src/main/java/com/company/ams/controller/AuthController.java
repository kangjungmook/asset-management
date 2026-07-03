package com.company.ams.controller;

import com.company.ams.common.ApiResponse;
import com.company.ams.dto.ChangePasswordRequest;
import com.company.ams.dto.LoginRequest;
import com.company.ams.dto.LoginResponse;
import com.company.ams.dto.RefreshRequest;
import com.company.ams.dto.TempPasswordResponse;
import com.company.ams.dto.TokenResponse;
import com.company.ams.security.AuthPrincipal;
import com.company.ams.security.CurrentUser;
import com.company.ams.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request), "로그인되었습니다.");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        AuthPrincipal me = currentUser.get();
        authService.logout(me.getUserId());
        return ApiResponse.success(null, "로그아웃되었습니다.");
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/reset-password/{id}")
    public ApiResponse<TempPasswordResponse> resetPassword(@PathVariable Integer id) {
        AuthPrincipal me = currentUser.get();
        return ApiResponse.success(authService.resetPassword(id, me), "임시 비밀번호가 발급되었습니다.");
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        AuthPrincipal me = currentUser.get();
        authService.changePassword(me.getUserId(), request.getCurrentPassword(), request.getNewPassword());
        return ApiResponse.success(null, "비밀번호가 변경되었습니다.");
    }
}
