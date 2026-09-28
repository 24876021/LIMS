package com.thematrix.labmanagement.common.service;

/**
 * WebSocket 消息推送服务接口
 * 封装 WebSocketServerImpl 的静态方法，供 Service 和 Controller 层调用
 */
public interface WebSocketPushService {

    /**
     * 推送消息给指定用户
     */
    void pushToUser(Long userId, String msg);

    /**
     * 按角色推送消息
     */
    void pushByRole(Long roleId, String msg);

    /**
     * 强制用户下线
     */
    void kickUser(Long userId);
}
