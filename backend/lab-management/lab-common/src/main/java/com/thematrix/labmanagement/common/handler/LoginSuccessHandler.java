package com.thematrix.labmanagement.common.handler;

import cn.hutool.json.JSONUtil;
import com.thematrix.labmanagement.common.utils.Result;
import com.thematrix.labmanagement.common.entity.SysUser;
import com.thematrix.labmanagement.common.service.SysUserService;
import com.thematrix.labmanagement.common.user.UserDetailServiceImpl;
import com.thematrix.labmanagement.common.utils.CommonUtil;
import com.thematrix.labmanagement.common.utils.JwtUtils;
import com.thematrix.labmanagement.common.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录成功处理器
 */
@Slf4j
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    SysUserService sysUserService;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();

        String account = CommonUtil.getStringBodyParameterFromRequest(httpServletRequest,"account");

        if (account == null || account.isEmpty()) {
            throw new IllegalArgumentException("没有账户");
        }

        String failKey = UserDetailServiceImpl.LOGIN_FAIL_KEY + account;
        String lockKey = UserDetailServiceImpl.LOCK_KEY + account;
        redisUtil.del(failKey);
        redisUtil.del(lockKey);

        SysUser sysUser = sysUserService.getUserByAccount(account);
        if (sysUser == null) {
            throw new IllegalArgumentException("没有找到账户: " + account);
        }

        // 登录成功后立即设置 MDC，确保后续日志能正确显示用户信息
        MDC.put("userId", String.valueOf(sysUser.getUserId()));
        MDC.put("name", sysUser.getName());
        
        log.info("用户 [{}] 登录成功", sysUser.getName());
        
        try {
            String jwt = jwtUtils.generateToken(authentication.getName());
            httpServletResponse.setHeader(jwtUtils.getHeader(), jwt);

            Map<String, Object> map = new HashMap<>();
            map.put("userId", sysUser.getUserId());
            map.put("jwt", jwt);

            outputStream.write(JSONUtil.toJsonStr(Result.success(map)).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        } finally {
            // 清理 MDC，防止线程复用时数据错乱
            MDC.clear();
        }
    }
}
