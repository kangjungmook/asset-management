package com.company.ams.security;

import com.company.ams.common.exception.ForbiddenException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthzTest {

    private final Authz authz = new Authz();

    private AuthPrincipal admin() {
        return new AuthPrincipal(1, "admin", "관리자", true, null, null, List.of(), List.of());
    }

    private AuthPrincipal manager(Integer deptId) {
        return new AuthPrincipal(2, "mgr", "매니저", false, deptId, "개발부", List.of("DEPT_MANAGER"), List.of());
    }

    private AuthPrincipal user(Integer userId, Integer deptId, List<String> permissions) {
        return new AuthPrincipal(userId, "u" + userId, "사용자" + userId, false, deptId, "개발부", List.of("USER"), permissions);
    }

    // ---------- requireAdminOrManagerOfDept ----------

    @Test
    void 관리자는_아무_부서나_통과한다() {
        assertDoesNotThrow(() -> authz.requireAdminOrManagerOfDept(admin(), 999));
    }

    @Test
    void 부서관리자는_같은_부서면_통과한다() {
        assertDoesNotThrow(() -> authz.requireAdminOrManagerOfDept(manager(1), 1));
    }

    @Test
    void 부서관리자는_다른_부서면_403이_난다() {
        assertThrows(ForbiddenException.class, () -> authz.requireAdminOrManagerOfDept(manager(1), 2));
    }

    @Test
    void 일반사용자는_자기_부서여도_403이_난다() {
        assertThrows(ForbiddenException.class, () -> authz.requireAdminOrManagerOfDept(user(3, 1, List.of()), 1));
    }

    @Test
    void deptId가_null이면_부서관리자도_403이_난다() {
        assertThrows(ForbiddenException.class, () -> authz.requireAdminOrManagerOfDept(manager(1), null));
    }

    // ---------- requireAdmin ----------

    @Test
    void requireAdmin은_관리자만_통과한다() {
        assertDoesNotThrow(() -> authz.requireAdmin(admin()));
        assertThrows(ForbiddenException.class, () -> authz.requireAdmin(manager(1)));
    }

    // ---------- requireAdminOrManagerOrSelf ----------

    @Test
    void 본인이면_통과한다() {
        AuthPrincipal self = user(5, 1, List.of());
        assertDoesNotThrow(() -> authz.requireAdminOrManagerOrSelf(self, 5, 1));
    }

    @Test
    void 본인이_아니고_관리자도_매니저도_아니면_403이_난다() {
        AuthPrincipal other = user(5, 1, List.of());
        assertThrows(ForbiddenException.class, () -> authz.requireAdminOrManagerOrSelf(other, 6, 1));
    }

    @Test
    void 같은_부서_매니저는_본인이_아니어도_통과한다() {
        assertDoesNotThrow(() -> authz.requireAdminOrManagerOrSelf(manager(1), 999, 1));
    }

    // ---------- requireAdminOrPermission ----------

    @Test
    void 권한을_보유하면_통과한다() {
        AuthPrincipal withPerm = user(7, 1, List.of("PASSWORD.EDIT"));
        assertDoesNotThrow(() -> authz.requireAdminOrPermission(withPerm, "PASSWORD.EDIT"));
    }

    @Test
    void 권한이_없으면_403이_난다() {
        AuthPrincipal withoutPerm = user(7, 1, List.of());
        assertThrows(ForbiddenException.class, () -> authz.requireAdminOrPermission(withoutPerm, "PASSWORD.EDIT"));
    }

    // ---------- requirePasswordAccess ----------

    @Test
    void 본인이면서_PASSWORD_EDIT_권한이_있으면_통과한다() {
        AuthPrincipal self = user(8, 1, List.of("PASSWORD.EDIT"));
        assertDoesNotThrow(() -> authz.requirePasswordAccess(self, 8, 1));
    }

    @Test
    void 본인이어도_PASSWORD_EDIT_권한이_없으면_403이_난다() {
        AuthPrincipal self = user(8, 1, List.of());
        assertThrows(ForbiddenException.class, () -> authz.requirePasswordAccess(self, 8, 1));
    }

    @Test
    void 다른_부서_대상의_패스워드는_같은_부서_매니저가_아니면_403이_난다() {
        assertThrows(ForbiddenException.class, () -> authz.requirePasswordAccess(manager(1), 999, 2));
    }

    @Test
    void 같은_부서_매니저는_대상의_PASSWORD_EDIT_권한과_무관하게_통과한다() {
        assertDoesNotThrow(() -> authz.requirePasswordAccess(manager(1), 999, 1));
    }
}
