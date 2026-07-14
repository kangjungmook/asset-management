package com.company.ams.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccountType {
    private Integer typeId;
    private String typeName;
    private String description;
    private Integer accountCount;
    private LocalDateTime createdAt;
}
