package com.company.ams.service;

import com.company.ams.common.exception.BusinessException;
import com.company.ams.common.exception.NotFoundException;
import com.company.ams.dto.PermissionRequest;
import com.company.ams.entity.Permission;
import com.company.ams.mapper.PermissionMapper;
import com.company.ams.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PermissionService {

    /** 코드에서 실제로 접근 제어에 사용하는 권한 코드. 이름 변경·삭제 시 기능이 조용히 망가지므로 보호한다. */
    public static final Set<String> SYSTEM_PERM_CODES = Set.of("PASSWORD.EDIT", "PASSWORD.APPROVE");

    private final PermissionMapper permissionMapper;
    private final AuditLogService auditLogService;

    public List<Permission> findAll() {
        List<Permission> permissions = permissionMapper.findAll();
        permissions.forEach(this::markSystem);
        return permissions;
    }

    @Transactional
    public Permission create(PermissionRequest request, AuthPrincipal actor) {
        Permission permission = new Permission();
        permission.setPermCode(request.getPermCode());
        permission.setPermName(request.getPermName());
        permission.setDescription(request.getDescription());
        permissionMapper.insert(permission);
        auditLogService.log(actor.getUserId(), "PERMISSION_CREATE", "perm:" + permission.getPermId(),
                "권한 생성: " + permission.getPermCode());
        return permission;
    }

    @Transactional
    public Permission update(Integer permId, PermissionRequest request, AuthPrincipal actor) {
        Permission permission = permissionMapper.findById(permId);
        if (permission == null) {
            throw new NotFoundException("권한을 찾을 수 없습니다.");
        }
        if (SYSTEM_PERM_CODES.contains(permission.getPermCode())
                && !permission.getPermCode().equals(request.getPermCode())) {
            throw new BusinessException("'" + permission.getPermCode() + "'는 시스템에서 사용 중인 권한 코드라 변경할 수 없습니다.");
        }
        permission.setPermCode(request.getPermCode());
        permission.setPermName(request.getPermName());
        permission.setDescription(request.getDescription());
        permissionMapper.update(permission);
        auditLogService.log(actor.getUserId(), "PERMISSION_UPDATE", "perm:" + permId,
                "권한 수정: " + permission.getPermCode());
        return permission;
    }

    @Transactional
    public void delete(Integer permId, AuthPrincipal actor) {
        Permission permission = permissionMapper.findById(permId);
        if (permission == null) {
            throw new NotFoundException("권한을 찾을 수 없습니다.");
        }
        if (SYSTEM_PERM_CODES.contains(permission.getPermCode())) {
            throw new BusinessException("'" + permission.getPermCode() + "'는 시스템에서 사용 중인 권한이라 삭제할 수 없습니다.");
        }
        permissionMapper.deleteById(permId);
        auditLogService.log(actor.getUserId(), "PERMISSION_DELETE", "perm:" + permId,
                "권한 삭제: " + permission.getPermCode());
    }

    private void markSystem(Permission permission) {
        permission.setSystem(SYSTEM_PERM_CODES.contains(permission.getPermCode()));
    }
}
