package com.company.ams.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountTypeRequest {

    @NotBlank(message = "계정유형명을 입력해주세요.")
    private String typeName;
}
