package com.thematrix.labmanagement.common.controller;

import com.thematrix.labmanagement.common.annotation.NoLog;
import com.thematrix.labmanagement.common.service.KaptchaService;
import com.thematrix.labmanagement.common.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码获取控制器（不需要记录日志）
 */
@NoLog("验证码接口，高频调用，无需记录日志")
@RestController
@Api(tags = "验证码相关接口")
public class KaptchaController {

    @Autowired
    private KaptchaService kaptchaService;

    @GetMapping("/captcha")
    @ApiOperation("验证码Base64获取")
    public Result captcha() throws Exception {
        return Result.success(kaptchaService.generateCaptcha());
    }
}
