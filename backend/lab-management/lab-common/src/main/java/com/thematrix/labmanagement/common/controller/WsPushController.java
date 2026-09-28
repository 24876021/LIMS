package com.thematrix.labmanagement.common.controller;

import com.thematrix.labmanagement.common.service.WebSocketPushService;
import com.thematrix.labmanagement.common.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ws")
public class WsPushController {

    @Autowired
    private WebSocketPushService webSocketPushService;

    @GetMapping("/push/{userId}")
    public Result pushToUser(@PathVariable Long userId, String msg) {
        webSocketPushService.pushToUser(userId, msg);
        return Result.success("推送成功");
    }

    @GetMapping("/pushRole/{roleId}")
    public Result pushByRole(@PathVariable Long roleId, String msg) {
        webSocketPushService.pushByRole(roleId, msg);
        return Result.success("按角色推送成功");
    }

    @GetMapping("/kickUser/{userId}")
    public Result kickUser(@PathVariable Long userId) {
        try {
            webSocketPushService.kickUser(userId);
            return Result.success("已强制该用户下线");
        } catch (Exception e) {
            return Result.error("用户不在线或推送失败");
        }
    }
}
