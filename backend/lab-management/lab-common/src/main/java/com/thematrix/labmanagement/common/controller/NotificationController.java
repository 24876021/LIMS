package com.thematrix.labmanagement.common.controller;

import com.thematrix.labmanagement.common.entity.Notification;
import com.thematrix.labmanagement.common.entity.NotificationSendDTO;
import com.thematrix.labmanagement.common.entity.SysUser;
import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.common.service.SysUserService;
import com.thematrix.labmanagement.common.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

@RestController
@Api(tags = "消息通知管理接口")
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/send")
    @ApiOperation("发送消息通知给指定用户")
    public Result sendNotification(@RequestBody NotificationSendDTO dto) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SysUser currentUser = sysUserService.getUserByAccount(username);
        // Service 层已统一处理保存 + WebSocket 实时推送
        notificationService.sendNotification(
                currentUser.getUserId(), dto.getReceiverId(), dto.getTitle(), dto.getContent(), dto.getType()
        );
        return Result.success("发送成功");
    }

    @GetMapping("/list")
    @ApiOperation("获取当前用户的消息通知列表")
    public Result getMyNotifications() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SysUser currentUser = sysUserService.getUserByAccount(username);
        List<Notification> list = notificationService.getByReceiverId(currentUser.getUserId());
        return Result.success(list);
    }

    @PutMapping("/read/{id}")
    @ApiOperation("标记单条消息已读")
    public Result markAsRead(@PathVariable Long id) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SysUser currentUser = sysUserService.getUserByAccount(username);
        notificationService.markAsRead(id, currentUser.getUserId());
        return Result.success("标记已读成功");
    }

    @PutMapping("/read-all")
    @ApiOperation("标记所有消息已读")
    public Result markAllAsRead() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SysUser currentUser = sysUserService.getUserByAccount(username);
        notificationService.markAllAsRead(currentUser.getUserId());
        return Result.success("全部标记已读成功");
    }

    @DeleteMapping("/clear")
    @ApiOperation("清空所有消息通知")
    public Result clearAll() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SysUser currentUser = sysUserService.getUserByAccount(username);
        notificationService.clearAll(currentUser.getUserId());
        return Result.success("清空成功");
    }
}
