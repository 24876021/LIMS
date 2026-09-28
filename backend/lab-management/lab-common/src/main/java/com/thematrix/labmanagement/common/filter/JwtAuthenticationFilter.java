package com.thematrix.labmanagement.common.filter;

import cn.hutool.core.util.StrUtil;
import com.thematrix.labmanagement.common.entity.SysUser;
import com.thematrix.labmanagement.common.service.SysUserService;
import com.thematrix.labmanagement.common.user.UserDetailServiceImpl;
import com.thematrix.labmanagement.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * jwt校验器
 */
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Autowired
    private SysUserService sysUserService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String jwt = request.getHeader(jwtUtils.getHeader());
        System.out.println("接收的jwt:"+jwt);
        if (StrUtil.isBlankOrUndefined(jwt)) {
            chain.doFilter(request, response);
            return;
        }

        Claims claim = jwtUtils.getClaimsByToken(jwt);
        if (claim == null) {
            throw new JwtException("token 异常");
        }
        if (jwtUtils.isTokenExpired(claim)) {
            throw new JwtException("token 已过期");
        }

        String account = claim.getSubject();

        // 通过 Service 获取用户（统一走 Service 层，AOP 日志会有 userId 为空的情况，属于已知可接受行为）
        SysUser sysUser = sysUserService.getUserByAccount(account);
        if (sysUser == null) {
            throw new JwtException("用户不存在");
        }

        // 设置 MDC，确保后续 AOP 日志能拿到用户信息
        MDC.put("userId", String.valueOf(sysUser.getUserId()));
        MDC.put("name", sysUser.getName());

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                account, null, userDetailService.getUserAuthority(sysUser.getUserId()));

        SecurityContextHolder.getContext().setAuthentication(token);

        try {
            chain.doFilter(request, response);
        } finally {
            // 请求结束后清理 MDC，防止线程复用时数据错乱
            MDC.clear();
        }
    }
}
