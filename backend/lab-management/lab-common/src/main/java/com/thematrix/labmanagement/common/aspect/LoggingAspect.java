package com.thematrix.labmanagement.common.aspect;

import com.thematrix.labmanagement.common.annotation.NoLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 全局日志切面：
 * 1. 拦截所有 @Service 和 @RestController 的方法
 * 2. 使用参数化日志（{} 占位符），避免不必要的字符串拼接
 * 3. 被 @NoLog 注解标记的方法不记录日志（含敏感数据）
 * 4. 从 MDC 读取 name（真实姓名），每条日志自动关联用户（userId 由 logback pattern 输出）
 * 5. @Order 设为较低优先级，让日志切面尽量在外层，记录完整输入输出
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class LoggingAspect {

    /** 敏感字段关键字，日志输出时用 *** 脱敏 */
    private static final String[] SENSITIVE_KEYWORDS = {
            "password", "pwd", "pass", "token", "secret", "authorization"
    };

    @Around("@within(org.springframework.stereotype.Service) || @within(org.springframework.web.bind.annotation.RestController)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        // 检查方法或类是否标注了 @NoLog
        NoLog methodNoLog = method.getAnnotation(NoLog.class);
        NoLog classNoLog = targetClass.getAnnotation(NoLog.class);
        if (methodNoLog != null || classNoLog != null) {
            return joinPoint.proceed();
        }

        Logger log = LoggerFactory.getLogger(targetClass);
        String className = targetClass.getSimpleName();
        String methodName = method.getName();
        String name = MDC.get("name");
        if (name == null) name = "-";

        // 记录方法入口日志（参数化，只有 info 启用时才拼接字符串）
        if (log.isInfoEnabled()) {
            Object[] args = joinPoint.getArgs();
            log.info("用户 [{}] 调用 {}.{}() 开始，参数个数: {}，参数: {}",
                    name, className, methodName, args.length, maskSensitive(Arrays.toString(args)));
        }

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - startTime;

            if (log.isInfoEnabled()) {
                String output = maskSensitive(truncate(String.valueOf(result)));
                log.info("用户 [{}] 调用 {}.{}() 完成，耗时: {}ms，返回: {}",
                        name, className, methodName, elapsed, output);
            }
            return result;
        } catch (Throwable e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("用户 [{}] 调用 {}.{}() 异常，耗时: {}ms，异常类型: {}，异常信息: {}",
                        name, className, methodName, elapsed, e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    /** 对敏感字段值进行脱敏处理 */
    private String maskSensitive(String input) {
        if (input == null) return "null";
        String result = input;
        for (String keyword : SENSITIVE_KEYWORDS) {
            // 匹配 keyword 后的引号内容，替换为 ***
            result = result.replaceAll(
                    "(?i)\"" + keyword + "\"\\s*:\\s*\"[^\"]*\"",
                    "\"" + keyword + "\":\"***\""
            );
            result = result.replaceAll(
                    "(?i)" + keyword + "\\s*=\\s*[^,\\s}]+",
                    keyword + "=***"
            );
        }
        return result;
    }

    /** 截断过长字符串，避免日志过大 */
    private String truncate(String s) {
        if (s == null) return "null";
        int maxLen = 500;
        return s.length() > maxLen ? s.substring(0, maxLen) + "...(truncated)" : s;
    }
}
