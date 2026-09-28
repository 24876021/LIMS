package com.thematrix.labmanagement.common.config;

import com.thematrix.labmanagement.common.filter.CaptchaFilter;
import com.thematrix.labmanagement.common.filter.CustomAuthenticationFilter;
import com.thematrix.labmanagement.common.filter.JwtAuthenticationFilter;
import com.thematrix.labmanagement.common.handler.LoginFailureHandler;
import com.thematrix.labmanagement.common.handler.LoginSuccessHandler;
import com.thematrix.labmanagement.common.handler.jwt.JWTLogoutSuccessHandler;
import com.thematrix.labmanagement.common.handler.jwt.JwtAccessDeniedHandler;
import com.thematrix.labmanagement.common.handler.jwt.JwtAuthenticationEntryPoint;
import com.thematrix.labmanagement.common.handler.password.PasswordEncoder;
import com.thematrix.labmanagement.common.user.UserDetailServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 整合所有组件，进行Spring Security全局配置
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    LoginFailureHandler loginFailureHandler;

    @Autowired
    LoginSuccessHandler loginSuccessHandler;

    @Autowired
    CaptchaFilter captchaFilter;

    @Autowired
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Autowired
    UserDetailServiceImpl userDetailService;

    @Autowired
    JWTLogoutSuccessHandler jwtLogoutSuccessHandler;

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        return new JwtAuthenticationFilter(authenticationManager());
    }

    //基础接口解除登录限制
    private static final String[] URL_WHITELIST = {
            "/login",
            "/logout",
            "/captcha",
            "/sysUser/register",
            "/ws/**",
            "/uploads/**",//上传文件的文件夹访问
            "/swagger-ui.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v2/api-docs/**",
            "/"
    };

    /**
     * 使用 Spring Security 自带的密码编译器
     */
    @Bean
    PasswordEncoder PasswordEncoder() {
        return new PasswordEncoder();
    }

    // 区分用户不存在/密码错误
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailService);
        provider.setPasswordEncoder(PasswordEncoder());
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and()
                .csrf().disable()

                // 登录配置
                .formLogin()

                //登出配置
                .and()
                .logout()
                .logoutSuccessHandler(jwtLogoutSuccessHandler)

                // 禁用session存储用户认证信息
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // 配置拦截规则
                .and()
                .authorizeRequests()
                .antMatchers(URL_WHITELIST).permitAll()// 放行一些没有用@PreAuthorize控制的接口，让没有登录的人使用，维持基础功能
                .antMatchers("/api/**").permitAll() // 放行所有业务模块接口的登录限制，由@PreAuthorize控制
                .anyRequest().authenticated()// 剩下的所有接口都需要登陆后才能访问

                // 异常处理器
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                // 配置自定义的过滤器
                .and()
                .addFilter(jwtAuthenticationFilter())
                .addFilterAt(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(captchaFilter, UsernamePasswordAuthenticationFilter.class)
        ;
    }

    /**
     * 登录配置，使用自定义的登录状态处理器
     */
    @Bean
    CustomAuthenticationFilter customAuthenticationFilter() throws Exception {
        CustomAuthenticationFilter filter = new CustomAuthenticationFilter();
        filter.setAuthenticationSuccessHandler(loginSuccessHandler);
        filter.setAuthenticationFailureHandler(loginFailureHandler);
        filter.setAuthenticationManager(authenticationManagerBean());

        return filter;
    }
}
