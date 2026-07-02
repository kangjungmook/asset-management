package com.company.ams.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionRequest {

    @NotBlank(message = "권한 코드를 입력해주세요.")
    private String permCode;

    @NotBlank(message = "권한명을 입력해주세요.")
    private String permName;
}
