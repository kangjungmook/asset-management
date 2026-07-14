package com.company.ams.mapper;

import com.company.ams.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    List<Notification> findByUserId(@Param("userId") Integer userId, @Param("limit") int limit);

    int countUnreadByUserId(@Param("userId") Integer userId);

    void insert(Notification notification);

    void markRead(@Param("notificationId") Integer notificationId, @Param("userId") Integer userId);

    void markAllRead(@Param("userId") Integer userId);

    void deleteAllByUserId(@Param("userId") Integer userId);
}
