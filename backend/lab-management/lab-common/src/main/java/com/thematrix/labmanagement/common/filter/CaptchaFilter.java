package com.thematrix.labmanagement.common.filter;

import com.thematrix.labmanagement.common.constant.Const;
import com.thematrix.labmanagement.common.handler.LoginFailureHandler;
import com.thematrix.labmanagement.common.handler.error.CaptchaException;
import com.thematrix.labmanagement.common.utils.CommonUtil;
import com.thematrix.labmanagement.common.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 验证码校验器
 */
@Component
public class CaptchaFilter extends OncePerRequestFilter {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private LoginFailureHandler loginFailureHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        String url = httpServletRequest.getRequestURI();
        if ("/login".equals(url) && "POST".equals(httpServletRequest.getMethod())) {
            try {
                validate(httpServletRequest);
            } catch (CaptchaException e) {
                loginFailureHandler.onAuthenticationFailure(httpServletRequest, httpServletResponse, e);
                httpServletResponse.flushBuffer();
                return;
            }
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private void validate(HttpServletRequest httpServletRequest) {
        String code = CommonUtil.getStringBodyParameterFromRequest(httpServletRequest,"code");
        String key = CommonUtil.getStringBodyParameterFromRequest(httpServletRequest,"userKey");

        if (StringUtils.isBlank(code) || StringUtils.isBlank(key)) {
            throw new CaptchaException("验证码错误");
        }

        Object cacheCode = redisUtil.hget(Const.CAPTCHA_KEY, key);
        if (cacheCode == null || !code.equalsIgnoreCase(cacheCode.toString())) {
            throw new CaptchaException("验证码错误");
        }

        redisUtil.hdel(Const.CAPTCHA_KEY, key);
    }
}
