package com.company.ams.service;

import com.company.ams.common.exception.UnauthorizedException;
import com.company.ams.dto.LoginRequest;
import com.company.ams.dto.LoginResponse;
import com.company.ams.dto.TokenResponse;
import com.company.ams.entity.RefreshToken;
import com.company.ams.entity.User;
import com.company.ams.mapper.RefreshTokenMapper;
import com.company.ams.mapper.UserMapper;
import com.company.ams.mapper.UserPermissionMapper;
import com.company.ams.mapper.UserRoleMapper;
import com.company.ams.security.Authz;
import com.company.ams.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String RAW_PASSWORD = "correct-password";

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private UserPermissionMapper userPermissionMapper;
    @Mock
    private RefreshTokenMapper refreshTokenMapper;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private UserService userService;
    @Mock
    private Authz authz;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtProvider jwtProvider = new JwtProvider(
            "test-secret-key-for-unit-tests-must-be-long-enough-256bits", 900000L, 604800000L);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userMapper, userRoleMapper, userPermissionMapper, refreshTokenMapper,
                passwordEncoder, jwtProvider, auditLogService, userService, authz);
    }

    private User activeUser() {
        User user = new User();
        user.setUserId(1);
        user.setEmployeeNo("emp001");
        user.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        user.setName("홍길동");
        user.setDeptId(1);
        user.setDeptName("개발부");
        user.setIsAdmin(false);
        user.setIsActive(true);
        return user;
    }

    private RefreshToken activeStoredToken(String jti) {
        RefreshToken stored = new RefreshToken();
        stored.setTokenId(jti);
        stored.setUserId(1);
        stored.setRevoked(false);
        stored.setExpiresAt(LocalDateTime.now().plusDays(7));
        return stored;
    }

    @Test
    void 올바른_사번과_비밀번호로_로그인하면_토큰이_발급된다() {
        User user = activeUser();
        when(userMapper.findByEmployeeNo("emp001")).thenReturn(user);
        when(userRoleMapper.findRoleCodesByUserId(1)).thenReturn(List.of("USER"));
        when(userPermissionMapper.findPermCodesByUserId(1)).thenReturn(List.of());

        LoginRequest request = new LoginRequest();
        request.setEmployeeNo("emp001");
        request.setPassword(RAW_PASSWORD);

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(response.getUserId()).isEqualTo(1);
        assertThat(response.getRoles()).containsExactly("USER");
    }

    @Test
    void 비밀번호가_틀리면_401이_난다() {
        when(userMapper.findByEmployeeNo("emp001")).thenReturn(activeUser());

        LoginRequest request = new LoginRequest();
        request.setEmployeeNo("emp001");
        request.setPassword("wrong-password");

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void 존재하지_않는_사번이면_401이_난다() {
        when(userMapper.findByEmployeeNo("nobody")).thenReturn(null);

        LoginRequest request = new LoginRequest();
        request.setEmployeeNo("nobody");
        request.setPassword(RAW_PASSWORD);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void 비활성화된_계정은_비밀번호가_맞아도_401이_난다() {
        User inactive = activeUser();
        inactive.setIsActive(false);
        when(userMapper.findByEmployeeNo("emp001")).thenReturn(inactive);

        LoginRequest request = new LoginRequest();
        request.setEmployeeNo("emp001");
        request.setPassword(RAW_PASSWORD);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void 유효한_리프레시_토큰으로_액세스_토큰을_재발급한다() {
        String jti = "test-jti-1";
        User user = activeUser();
        when(userMapper.findById(1)).thenReturn(user);
        when(userRoleMapper.findRoleCodesByUserId(1)).thenReturn(List.of("USER"));
        when(userPermissionMapper.findPermCodesByUserId(1)).thenReturn(List.of());
        when(refreshTokenMapper.findById(jti)).thenReturn(activeStoredToken(jti));

        String refreshToken = jwtProvider.generateRefreshToken(1, jti);
        TokenResponse response = authService.refresh(refreshToken);

        assertThat(response.getAccessToken()).isNotBlank();
    }

    @Test
    void 로그아웃한_뒤에는_리프레시_토큰이_거부된다() {
        String jti = "test-jti-2";
        RefreshToken revoked = activeStoredToken(jti);
        revoked.setRevoked(true);
        when(refreshTokenMapper.findById(jti)).thenReturn(revoked);

        String refreshToken = jwtProvider.generateRefreshToken(1, jti);

        assertThrows(UnauthorizedException.class, () -> authService.refresh(refreshToken));
    }

    @Test
    void DB에_없는_리프레시_토큰은_거부된다() {
        String jti = "unknown-jti";
        when(refreshTokenMapper.findById(jti)).thenReturn(null);

        String refreshToken = jwtProvider.generateRefreshToken(1, jti);

        assertThrows(UnauthorizedException.class, () -> authService.refresh(refreshToken));
    }

    @Test
    void 로그아웃하면_해당_사용자의_모든_리프레시_토큰이_무효화된다() {
        authService.logout(1);

        org.mockito.Mockito.verify(refreshTokenMapper).revokeAllByUserId(1);
    }

    @Test
    void 액세스_토큰을_리프레시용으로_쓰면_401이_난다() {
        String accessToken = jwtProvider.generateAccessToken(
                new com.company.ams.security.AuthPrincipal(1, "emp001", "홍길동", false, 1, "개발부", List.of(), List.of()));

        assertThrows(UnauthorizedException.class, () -> authService.refresh(accessToken));
    }

    @Test
    void 유효하지_않은_토큰_형식이면_401이_난다() {
        assertThrows(UnauthorizedException.class, () -> authService.refresh("not-a-valid-jwt"));
    }

    @Test
    void 리프레시_시점에_계정이_비활성화면_401이_난다() {
        String jti = "test-jti-3";
        User inactive = activeUser();
        inactive.setIsActive(false);
        when(userMapper.findById(1)).thenReturn(inactive);
        when(refreshTokenMapper.findById(jti)).thenReturn(activeStoredToken(jti));

        String refreshToken = jwtProvider.generateRefreshToken(1, jti);

        assertThrows(UnauthorizedException.class, () -> authService.refresh(refreshToken));
    }
}
