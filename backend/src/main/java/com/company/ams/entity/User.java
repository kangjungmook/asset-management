package com.company.ams.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Integer userId;
    private String employeeNo;
    private String password;
    private String name;
    private Integer deptId;
    private String deptName;
    private Boolean isAdmin;
    private Boolean isActive;
    private Boolean mustChangePassword;
    private Boolean isDeptManager;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
