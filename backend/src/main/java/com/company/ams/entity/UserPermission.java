package com.company.ams.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserPermission {
    private Integer id;
    private Integer userId;
    private Integer permId;
    private String permCode;
    private String permName;
    private Integer grantedBy;
    private LocalDateTime grantedAt;
}
