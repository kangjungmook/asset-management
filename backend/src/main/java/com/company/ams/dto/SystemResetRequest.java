package com.company.ams.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SystemResetRequest {

    @NotBlank(message = "확인 문구를 입력해주세요.")
    private String confirm;
}
