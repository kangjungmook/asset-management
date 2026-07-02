package com.company.ams.security;

import com.company.ams.common.exception.ForbiddenException;
import org.springframework.stereotype.Component;

@Component
public class Authz {

    /** ADMIN 이거나, DEPT_MANAGER 이면서 같은 부서일 때만 통과 */
    public void requireAdminOrManagerOfDept(AuthPrincipal principal, Integer deptId) {
        if (principal.isAdmin()) {
            return;
        }
        if (principal.hasRole("DEPT_MANAGER") && deptId != null && deptId.equals(principal.getDeptId())) {
            return;
        }
        throw new ForbiddenException();
    }

    /** ADMIN 만 통과 */
    public void requireAdmin(AuthPrincipal principal) {
        if (!principal.isAdmin()) {
            throw new ForbiddenException();
        }
    }

    /** ADMIN, 같은 부서 매니저, 본인 중 하나면 통과 */
    public void requireAdminOrManagerOrSelf(AuthPrincipal principal, Integer targetUserId, Integer targetDeptId) {
        if (principal.isAdmin()) {
            return;
        }
        if (principal.hasRole("DEPT_MANAGER") && targetDeptId != null && targetDeptId.equals(principal.getDeptId())) {
            return;
        }
        if (principal.getUserId().equals(targetUserId)) {
            return;
        }
        throw new ForbiddenException();
    }

    /** ADMIN 이거나 특정 권한코드를 보유해야 통과 */
    public void requireAdminOrPermission(AuthPrincipal principal, String permCode) {
        if (principal.isAdmin()) {
            return;
        }
        if (principal.hasPermission(permCode)) {
            return;
        }
        throw new ForbiddenException();
    }

    /** 패스워드 대장 접근: ADMIN, 같은 부서 매니저, 또는 PASSWORD.EDIT 권한을 가진 본인 중 하나면 통과 */
    public void requirePasswordAccess(AuthPrincipal principal, Integer targetUserId, Integer targetDeptId) {
        if (principal.isAdmin()) {
            return;
        }
        if (principal.hasRole("DEPT_MANAGER") && targetDeptId != null && targetDeptId.equals(principal.getDeptId())) {
            return;
        }
        if (principal.getUserId().equals(targetUserId) && principal.hasPermission("PASSWORD.EDIT")) {
            return;
        }
        throw new ForbiddenException();
    }
}
