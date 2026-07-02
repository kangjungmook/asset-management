package com.company.ams.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AuthPrincipal {

    private Integer userId;
    private String employeeNo;
    private String name;
    private boolean admin;
    private Integer deptId;
    private String deptName;
    private List<String> roles;
    private List<String> permissions;

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean hasPermission(String permCode) {
        return permissions != null && permissions.contains(permCode);
    }
}
