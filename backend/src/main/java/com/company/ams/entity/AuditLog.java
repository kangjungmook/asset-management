package com.company.ams.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLog {
    private Integer logId;
    private Integer userId;
    private String userName;
    private String action;
    private String target;
    private String detail;
    private String ipAddress;
    private String result;
    private LocalDateTime createdAt;
}
