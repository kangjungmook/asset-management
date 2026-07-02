package com.company.ams.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TempPasswordResponse {
    private String employeeNo;
    private String tempPassword;
}
