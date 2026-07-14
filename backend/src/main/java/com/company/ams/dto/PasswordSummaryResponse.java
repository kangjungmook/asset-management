package com.company.ams.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordSummaryResponse {
    private long expired;
    private long expiringSoon;
    private long normal;
}
