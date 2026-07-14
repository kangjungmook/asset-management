package com.company.ams.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Permission {
    private Integer permId;
    private String permCode;
    private String permName;
    private String description;
    private LocalDateTime createdAt;
    private Integer holderCount;
    private Boolean system;
}
