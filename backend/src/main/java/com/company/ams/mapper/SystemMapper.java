package com.company.ams.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemMapper {

    void deleteAllAuditLogs();

    void deleteAllPasswords();

    void deleteAllUserPermissions();

    void deleteAllUserRoles();

    void deleteAllRefreshTokens();

    void clearAdminDeptId();

    void deleteNonAdminUsers();

    void deleteAllDepartments();

    void seedDepartments();

    void deleteAllAccountTypes();

    void seedAccountTypes();

    void deleteAllPermissions();

    void seedPermissions();

    void deletePasswordsForNonAdminUsers();

    void deleteRefreshTokensForNonAdmins();

    void deleteUserPermissionsForNonAdmins();

    void deleteUserRolesForNonAdmins();

    void nullifyAuditLogForNonAdmins();

    void clearDeptIdForAllUsers();
}
