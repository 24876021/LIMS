package com.thematrix.labmanagement.common.handler;

import cn.hutool.json.JSONUtil;
import com.thematrix.labmanagement.common.service.WebSocketPushService;
import com.thematrix.labmanagement.common.handler.error.CaptchaException;
import com.thematrix.labmanagement.common.utils.Result;
import com.thematrix.labmanagement.common.entity.SysUser;
import com.thematrix.labmanagement.common.service.SysUserService;
import com.thematrix.labmanagement.common.utils.CommonUtil;
import com.thematrix.labmanagement.common.utils.RedisUtil;
import com.thematrix.labmanagement.common.user.UserDetailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
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
 * 登录失败处理器
 */
@Slf4j
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private WebSocketPushService webSocketPushService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        ServletOutputStream out = response.getOutputStream();
        String account = CommonUtil.getStringBodyParameterFromRequest(request, "account");
        String userName = account != null ? account : "未知";
        if (account != null && !account.isEmpty()) {
            SysUser user = sysUserService.getUserByAccount(account);
            if (user != null) {
                userName = user.getName();
            }
        }
        String msg = "登录失败";

        if (e instanceof LockedException) {
            String lockType = null;
            String lockKey = UserDetailServiceImpl.LOCK_KEY + account;
            Object lockObj = redisUtil.get(lockKey);

            if (lockObj != null) {
                Map<String, String> lockMap = (Map<String, String>) lockObj;
                lockType = lockMap.get("type");
            }

            if (UserDetailServiceImpl.LOCK_TYPE_MANUAL.equals(lockType)) {
                msg = "账户已被管理员锁定，30分钟后自动解锁";
            } else if (UserDetailServiceImpl.LOCK_TYPE_PWD_ERROR.equals(lockType)) {
                msg = "连续输错密码过多，账户已锁定15分钟";
            } else {
                msg = "账户已锁定，请稍后重试";
            }
        }
        else if (e instanceof CaptchaException) {
            msg = "验证码错误";
        }
        else if (e instanceof BadCredentialsException) {
            String key = UserDetailServiceImpl.LOGIN_FAIL_KEY + account;
            int curr = redisUtil.hasKey(key) ? Integer.parseInt(redisUtil.get(key).toString()) : 0;
            int next = curr + 1;

            redisUtil.set(key, next, 600);
            int left = UserDetailServiceImpl.MAX_FAIL_COUNT - next;

            if (left > 0) {
                msg = "密码错误，还能尝试 " + left + " 次";
            } else {
                SysUser user = sysUserService.getUserByAccount(account);
                if (user != null) {
                    user.setStatus(false);
                    sysUserService.updateById(user);

                    String lockKey = UserDetailServiceImpl.LOCK_KEY + account;
                    Map<String, String> map = new HashMap<>();
                    map.put("type", UserDetailServiceImpl.LOCK_TYPE_PWD_ERROR);
                    map.put("expire", String.valueOf(System.currentTimeMillis() + UserDetailServiceImpl.LOCK_EXPIRE_SECONDS * 1000));

                    redisUtil.set(lockKey, map, UserDetailServiceImpl.LOCK_EXPIRE_SECONDS);
                    webSocketPushService.pushByRole(1L, "lock:" + account);
                }
                redisUtil.del(key);
                msg = "连续输错5次密码，账户已锁定15分钟";
            }
        }
        else if (e instanceof DisabledException) {
            msg = "账户已禁用";
        }
        else if (e instanceof UsernameNotFoundException) {
            msg = "用户名不存在";
        }

        log.warn("用户 [{}] 登录失败，原因: {}", userName, msg);
        out.write(JSONUtil.toJsonStr(Result.fail(msg)).getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
    }
}
