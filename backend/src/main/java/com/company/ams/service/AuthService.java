package com.company.ams.service;

import com.company.ams.common.exception.NotFoundException;
import com.company.ams.common.exception.UnauthorizedException;
import com.company.ams.dto.LoginRequest;
import com.company.ams.dto.LoginResponse;
import com.company.ams.dto.TempPasswordResponse;
import com.company.ams.dto.TokenResponse;
import com.company.ams.entity.RefreshToken;
import com.company.ams.entity.User;
import com.company.ams.mapper.RefreshTokenMapper;
import com.company.ams.mapper.UserMapper;
import com.company.ams.mapper.UserPermissionMapper;
import com.company.ams.mapper.UserRoleMapper;
import com.company.ams.security.AuthPrincipal;
import com.company.ams.security.Authz;
import com.company.ams.security.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserPermissionMapper userPermissionMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuditLogService auditLogService;
    private final UserService userService;
    private final Authz authz;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByEmployeeNo(request.getEmployeeNo());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("사번 또는 비밀번호가 올바르지 않습니다.");
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException("비활성화된 계정입니다. 관리자에게 문의하세요.");
        }

        AuthPrincipal principal = buildPrincipal(user);
        String accessToken = jwtProvider.generateAccessToken(principal);
        String refreshToken = issueRefreshToken(user.getUserId());

        auditLogService.log(user.getUserId(), "LOGIN", "user:" + user.getUserId(),
                "로그인: " + user.getEmployeeNo());

        return new LoginResponse(
                accessToken, refreshToken,
                user.getUserId(), user.getEmployeeNo(), user.getName(),
                user.getIsAdmin(), user.getDeptId(), user.getDeptName(),
                principal.getRoles(), principal.getPermissions(),
                Boolean.TRUE.equals(user.getMustChangePassword())
        );
    }

    @Transactional
    public void logout(Integer userId) {
        refreshTokenMapper.revokeAllByUserId(userId);
        auditLogService.log(userId, "LOGOUT", "user:" + userId, "로그아웃");
    }

    @Transactional
    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        userService.changeOwnPassword(userId, currentPassword, newPassword);
        refreshTokenMapper.revokeAllByUserId(userId);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        if (!jwtProvider.isValid(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }
        Claims claims = jwtProvider.parseClaims(refreshToken);
        if (!jwtProvider.isRefreshToken(claims)) {
            throw new UnauthorizedException("리프레시 토큰이 아닙니다.");
        }

        RefreshToken stored = refreshTokenMapper.findById(claims.getId());
        if (stored == null || Boolean.TRUE.equals(stored.getRevoked())
                || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("만료되었거나 로그아웃된 토큰입니다. 다시 로그인해주세요.");
        }

        Integer userId = Integer.valueOf(claims.getSubject());
        User user = userMapper.findById(userId);
        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException("계정을 사용할 수 없습니다.");
        }
        AuthPrincipal principal = buildPrincipal(user);
        String newAccessToken = jwtProvider.generateAccessToken(principal);

        refreshTokenMapper.revoke(claims.getId());
        String newRefreshToken = issueRefreshToken(userId);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    private String issueRefreshToken(Integer userId) {
        String jti = UUID.randomUUID().toString();
        String refreshToken = jwtProvider.generateRefreshToken(userId, jti);

        RefreshToken record = new RefreshToken();
        record.setTokenId(jti);
        record.setUserId(userId);
        record.setExpiresAt(LocalDateTime.now().plus(
                java.time.Duration.ofMillis(jwtProvider.getRefreshExpirationMillis())));
        record.setRevoked(false);
        refreshTokenMapper.insert(record);

        return refreshToken;
    }

    @Transactional
    public TempPasswordResponse resetPassword(Integer userId, AuthPrincipal actor) {
        User target = userMapper.findById(userId);
        if (target == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        authz.requireAdminOrManagerOfDept(actor, target.getDeptId());
        return userService.resetPassword(userId, actor);
    }

    private AuthPrincipal buildPrincipal(User user) {
        return new AuthPrincipal(
                user.getUserId(),
                user.getEmployeeNo(),
                user.getName(),
                Boolean.TRUE.equals(user.getIsAdmin()),
                user.getDeptId(),
                user.getDeptName(),
                userRoleMapper.findRoleCodesByUserId(user.getUserId()),
                userPermissionMapper.findPermCodesByUserId(user.getUserId())
        );
    }
}
