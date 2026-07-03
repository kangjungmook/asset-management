package com.company.ams.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Integer userId;
    private String employeeNo;
    private String name;
    private Boolean isAdmin;
    private Integer deptId;
    private String deptName;
    private List<String> roles;
    private List<String> permissions;
    private Boolean mustChangePassword;
}
