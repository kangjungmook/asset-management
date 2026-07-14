package com.company.ams.service;

import com.company.ams.entity.Notification;
import com.company.ams.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int LIST_LIMIT = 50;

    private final NotificationMapper notificationMapper;

    public List<Notification> list(Integer userId) {
        return notificationMapper.findByUserId(userId, LIST_LIMIT);
    }

    public int unreadCount(Integer userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    @Transactional
    public void markRead(Integer notificationId, Integer userId) {
        notificationMapper.markRead(notificationId, userId);
    }

    @Transactional
    public void markAllRead(Integer userId) {
        notificationMapper.markAllRead(userId);
    }

    @Transactional
    public void notify(Integer userId, String type, String title, String message, String link) {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setLink(link);
            notificationMapper.insert(notification);
        } catch (Exception e) {
            log.error("알림 기록 실패: userId={}, type={}", userId, type, e);
        }
    }

    @Transactional
    public void notifyMany(List<Integer> userIds, String type, String title, String message, String link) {
        for (Integer userId : userIds) {
            notify(userId, type, title, message, link);
        }
    }
}
