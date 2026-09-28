package com.thematrix.labmanagement.common.service.impl;

import com.thematrix.labmanagement.common.service.UserRoleService;
import com.thematrix.labmanagement.common.service.WebSocketPushService;
import com.thematrix.labmanagement.common.service.impl.WebSocketServerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebSocketPushServiceImpl implements WebSocketPushService {

    @Autowired
    private UserRoleService userRoleService;

    @Override
    public void pushToUser(Long userId, String msg) {
        WebSocketServerImpl.sendToUser(userId, msg);
    }

    @Override
    public void pushByRole(Long roleId, String msg) {
        List<Long> userIds = userRoleService.getUserIdsByRoleId(roleId);
        for (Long userId : userIds) {
            WebSocketServerImpl.sendToUser(userId, msg);
        }
    }

    @Override
    public void kickUser(Long userId) {
        WebSocketServerImpl.sendToUser(userId, "forceLogout");
    }
}
