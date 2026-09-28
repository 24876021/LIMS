package com.thematrix.labmanagement.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 发送通知请求 DTO
 */
@Setter
@Getter
public class NotificationSendDTO implements Serializable {

    private Long receiverId;
    private String title;
    private String content;
    private String type;

}
