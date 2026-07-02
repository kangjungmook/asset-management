package com.company.ams.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Department {
    private Integer deptId;
    private String deptName;
    private LocalDateTime createdAt;
}
