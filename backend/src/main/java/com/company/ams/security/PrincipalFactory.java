package com.company.ams.security;

import com.company.ams.entity.User;
import com.company.ams.mapper.UserPermissionMapper;
import com.company.ams.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrincipalFactory {

    private final UserRoleMapper userRoleMapper;
    private final UserPermissionMapper userPermissionMapper;

    public AuthPrincipal from(User user) {
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
