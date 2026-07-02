package com.company.ams.service;

import com.company.ams.common.exception.NotFoundException;
import com.company.ams.dto.PermissionRequest;
import com.company.ams.entity.Permission;
import com.company.ams.mapper.PermissionMapper;
import com.company.ams.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionMapper permissionMapper;
    private final AuditLogService auditLogService;

    public List<Permission> findAll() {
        return permissionMapper.findAll();
    }

    @Transactional
    public Permission create(PermissionRequest request, AuthPrincipal actor) {
        Permission permission = new Permission();
        permission.setPermCode(request.getPermCode());
        permission.setPermName(request.getPermName());
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
        permission.setPermCode(request.getPermCode());
        permission.setPermName(request.getPermName());
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
        permissionMapper.deleteById(permId);
        auditLogService.log(actor.getUserId(), "PERMISSION_DELETE", "perm:" + permId,
                "권한 삭제: " + permission.getPermCode());
    }
}
