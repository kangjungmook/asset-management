package com.company.ams.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleGrantRequest {

    @NotBlank(message = "역할을 선택해주세요.")
    private String role;
}
