package com.company.ams.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PasswordRequest {

    @NotNull(message = "계정유형을 선택해주세요.")
    private Integer typeId;

    private String requesterName;

    @NotNull(message = "대상 사용자를 선택해주세요.")
    private Integer userId;

    private String accountId;

    private LocalDateTime changedAt;

    private LocalDate expireAt;

    private String changeReason;

    private String note;
}
