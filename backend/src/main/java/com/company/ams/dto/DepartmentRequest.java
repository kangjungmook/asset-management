package com.company.ams.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentRequest {

    @NotBlank(message = "부서명을 입력해주세요.")
    private String deptName;
}
