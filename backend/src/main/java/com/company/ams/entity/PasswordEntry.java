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
    private Integer requesterId;
    private String requesterName;
    private Integer userId;
    private String userName;
    private Integer userDeptId;
    private String userDeptName;
    private Integer approverId;
    private String approverName;
    private String accountId;
    private LocalDateTime changedAt;
    private LocalDate expireAt;
    private String changeReason;
    private LocalDateTime confirmedAt;
    private Integer confirmedBy;
    private String confirmerName;
    private String note;
}
