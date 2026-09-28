package com.thematrix.labmanagement.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thematrix.labmanagement.common.entity.Notification;
import com.thematrix.labmanagement.common.entity.SysUser;
import com.thematrix.labmanagement.common.mapper.NotificationMapper;
import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.common.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户消息通知 Service 实现类
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    @Lazy
    private SysUserService sysUserService;

    @Override
    public Notification sendNotification(Long senderId, Long receiverId, String title, String content, String type) {
        Notification notification = new Notification();
        notification.setSenderId(senderId);
        notification.setReceiverId(receiverId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type != null ? type : "info");
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());
        save(notification);

        // 通过 WebSocket 实时推送给接收者（所有通知统一在此推送）
        try {
            // 查找发送者姓名
            String senderName = "系统";
            if (senderId != null && senderId > 0) {
                SysUser sender = sysUserService.getById(senderId);
                if (sender != null && sender.getName() != null) {
                    senderName = sender.getName();
                }
            }

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> data = new HashMap<>();
            data.put("id", notification.getId());
            data.put("title", title);
            data.put("content", content);
            data.put("senderName", senderName);
            data.put("time", LocalDateTime.now().toString());
            data.put("isRead", false);

            Map<String, Object> wsMsg = new HashMap<>();
            wsMsg.put("type", "notification");
            wsMsg.put("data", data);

            String wsMessage = objectMapper.writeValueAsString(wsMsg);
            WebSocketServerImpl.sendToUser(receiverId, wsMessage);
        } catch (Exception e) {
            System.err.println("[Notification] WebSocket 推送失败: " + e.getMessage() + ", receiverId=" + receiverId);
            // WebSocket 推送失败不影响消息保存（用户刷新或点开面板仍可看到）
        }
        System.out.println("[Notification] 通知已保存: id=" + notification.getId() + " receiverId=" + receiverId + " title=" + title);

        return notification;
    }

    @Override
    public List<Notification> getByReceiverId(Long receiverId) {
        return notificationMapper.getByReceiverId(receiverId);
    }

    @Override
    public Long countUnread(Long receiverId) {
        return notificationMapper.countUnread(receiverId);
    }

    @Override
    public void markAsRead(Long id, Long userId) {
        Notification notification = getById(id);
        if (notification != null && notification.getReceiverId().equals(userId)) {
            notification.setIsRead(true);
            updateById(notification);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiverId, userId)
               .eq(Notification::getIsRead, false);
        List<Notification> list = list(wrapper);
        for (Notification n : list) {
            n.setIsRead(true);
        }
        updateBatchById(list);
    }

    @Override
    public void clearAll(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiverId, userId);
        remove(wrapper);
    }
}
