package com.company.ams.controller;

import com.company.ams.common.ApiResponse;
import com.company.ams.entity.Notification;
import com.company.ams.security.AuthPrincipal;
import com.company.ams.security.CurrentUser;
import com.company.ams.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUser currentUser;

    @GetMapping
    public ApiResponse<List<Notification>> list() {
        AuthPrincipal me = currentUser.get();
        return ApiResponse.success(notificationService.list(me.getUserId()));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Integer> unreadCount() {
        AuthPrincipal me = currentUser.get();
        return ApiResponse.success(notificationService.unreadCount(me.getUserId()));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Integer id) {
        AuthPrincipal me = currentUser.get();
        notificationService.markRead(id, me.getUserId());
        return ApiResponse.success(null);
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        AuthPrincipal me = currentUser.get();
        notificationService.markAllRead(me.getUserId());
        return ApiResponse.success(null);
    }
}
