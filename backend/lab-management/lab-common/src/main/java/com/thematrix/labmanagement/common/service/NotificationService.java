package com.thematrix.labmanagement.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.common.entity.Notification;

import java.util.List;

/**
 * 用户消息通知 Service 接口
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 发送通知给指定用户
     */
    Notification sendNotification(Long senderId, Long receiverId, String title, String content, String type);

    /**
     * 获取指定用户的通知列表（按时间倒序）
     */
    List<Notification> getByReceiverId(Long receiverId);

    /**
     * 获取指定用户的未读通知数量
     */
    Long countUnread(Long receiverId);

    /**
     * 标记单条通知已读
     */
    void markAsRead(Long id, Long userId);

    /**
     * 标记指定用户的所有通知已读
     */
    void markAllAsRead(Long userId);

    /**
     * 清空指定用户的所有通知
     */
    void clearAll(Long userId);
}
