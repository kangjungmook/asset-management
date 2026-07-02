package com.company.ams.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequest {

    @NotBlank(message = "리프레시 토큰이 필요합니다.")
    private String refreshToken;
}
