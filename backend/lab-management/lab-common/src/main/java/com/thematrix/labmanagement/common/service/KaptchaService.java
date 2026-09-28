package com.thematrix.labmanagement.common.service;

import java.util.Map;

/**
 * 验证码服务接口
 */
public interface KaptchaService {

    /**
     * 生成验证码（返回 Base64 图片 + userKey）
     */
    Map<String, String> generateCaptcha() throws Exception;
}
