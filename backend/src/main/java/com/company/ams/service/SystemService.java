package com.company.ams.service;

import com.company.ams.common.exception.BusinessException;
import com.company.ams.mapper.SystemMapper;
import com.company.ams.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemService {

    public static final String RESET_CONFIRM_PHRASE = "시스템 초기화";

    private final SystemMapper systemMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public void reset(String confirm, AuthPrincipal actor) {
        if (!RESET_CONFIRM_PHRASE.equals(confirm)) {
            throw new BusinessException("확인 문구가 일치하지 않습니다. '" + RESET_CONFIRM_PHRASE + "'를 정확히 입력해주세요.");
        }

        systemMapper.deleteAllAuditLogs();
        systemMapper.deleteAllPasswords();
        systemMapper.deleteAllUserPermissions();
        systemMapper.deleteAllUserRoles();
        systemMapper.deleteAllRefreshTokens();

        systemMapper.clearAdminDeptId();
        systemMapper.deleteNonAdminUsers();

        systemMapper.deleteAllDepartments();
        systemMapper.seedDepartments();

        systemMapper.deleteAllAccountTypes();
        systemMapper.seedAccountTypes();

        systemMapper.deleteAllPermissions();
        systemMapper.seedPermissions();

        auditLogService.log(actor.getUserId(), "SYSTEM_RESET", "system",
                "시스템 데이터 전체 초기화 실행 (사용자·부서·권한·계정유형·패스워드대장·감사로그 초기화)");
    }

    @Transactional
    public void resetUsers(AuthPrincipal actor) {
        systemMapper.deletePasswordsForNonAdminUsers();
        systemMapper.deleteRefreshTokensForNonAdmins();
        systemMapper.deleteUserPermissionsForNonAdmins();
        systemMapper.deleteUserRolesForNonAdmins();
        systemMapper.nullifyAuditLogForNonAdmins();
        systemMapper.deleteNonAdminUsers();

        auditLogService.log(actor.getUserId(), "SYSTEM_RESET_USERS", "system",
                "사용자 초기화 실행 (관리자 제외 전체 사용자 및 관련 패스워드대장·권한·역할 삭제)");
    }

    @Transactional
    public void resetDepartments(AuthPrincipal actor) {
        systemMapper.clearDeptIdForAllUsers();
        systemMapper.deleteAllDepartments();
        systemMapper.seedDepartments();

        auditLogService.log(actor.getUserId(), "SYSTEM_RESET_DEPARTMENTS", "system",
                "부서 초기화 실행 (기본 부서로 재생성, 사용자 부서 배정 해제)");
    }

    @Transactional
    public void resetPermissions(AuthPrincipal actor) {
        systemMapper.deleteAllUserPermissions();
        systemMapper.deleteAllPermissions();
        systemMapper.seedPermissions();

        auditLogService.log(actor.getUserId(), "SYSTEM_RESET_PERMISSIONS", "system",
                "권한 초기화 실행 (기본 권한으로 재생성, 부여 이력 전체 삭제)");
    }

    @Transactional
    public void resetAccountTypes(AuthPrincipal actor) {
        systemMapper.deleteAllPasswords();
        systemMapper.deleteAllAccountTypes();
        systemMapper.seedAccountTypes();

        auditLogService.log(actor.getUserId(), "SYSTEM_RESET_ACCOUNT_TYPES", "system",
                "계정유형 초기화 실행 (기본 유형으로 재생성, 참조하던 패스워드 관리대장 전체 삭제)");
    }

    @Transactional
    public void resetPasswords(AuthPrincipal actor) {
        systemMapper.deleteAllPasswords();

        auditLogService.log(actor.getUserId(), "SYSTEM_RESET_PASSWORDS", "system",
                "패스워드 관리대장 초기화 실행");
    }

    @Transactional
    public void resetAuditLogs(AuthPrincipal actor) {
        systemMapper.deleteAllAuditLogs();

        auditLogService.log(actor.getUserId(), "SYSTEM_RESET_AUDIT_LOGS", "system",
                "감사 로그 초기화 실행");
    }
}
