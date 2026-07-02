package com.company.ams.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RefreshToken {
    private String tokenId;
    private Integer userId;
    private LocalDateTime expiresAt;
    private Boolean revoked;
    private LocalDateTime createdAt;
}
