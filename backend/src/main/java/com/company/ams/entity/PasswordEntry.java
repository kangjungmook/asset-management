package com.company.ams.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PasswordEntry {
    private Integer pwId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String lastModifier;
    private Integer typeId;
    private String typeName;
    private String requesterName;
    private Integer userId;
    private String userName;
    private Integer userDeptId;
    private String userDeptName;
    private String approverName;
    private LocalDateTime approvedAt;
    private String status;
    private String rejectedBy;
    private LocalDateTime rejectedAt;
    private String rejectReason;
    private String accountId;
    private LocalDateTime changedAt;
    private LocalDate expireAt;
    private String changeReason;
    private String note;
}
