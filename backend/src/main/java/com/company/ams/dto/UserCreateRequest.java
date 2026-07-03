package com.company.ams.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateRequest {

    @NotBlank(message = "사번을 입력해주세요.")
    private String employeeNo;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    private Integer deptId;
}
