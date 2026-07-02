package com.company.ams.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionGrantRequest {

    @NotNull(message = "권한을 선택해주세요.")
    private Integer permId;
}
