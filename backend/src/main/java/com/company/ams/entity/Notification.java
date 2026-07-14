package com.company.ams.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notification {
    private Integer notificationId;
    private Integer userId;
    private String type;
    private String title;
    private String message;
    private String link;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
