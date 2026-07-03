package com.company.ams.dto;

import com.company.ams.entity.UserPermission;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDetailResponse {
    private Integer userId;
    private String employeeNo;
    private String name;
    private Integer deptId;
    private String deptName;
    private Boolean isAdmin;
    private Boolean isActive;
    private Boolean mustChangePassword;
    private List<String> roles;
    private List<UserPermission> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
