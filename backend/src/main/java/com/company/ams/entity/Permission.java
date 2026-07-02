package com.company.ams.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Permission {
    private Integer permId;
    private String permCode;
    private String permName;
    private LocalDateTime createdAt;
}
