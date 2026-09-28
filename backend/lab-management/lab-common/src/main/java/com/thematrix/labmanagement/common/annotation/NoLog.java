package com.thematrix.labmanagement.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记不需要记录输入输出日志的方法。
 * 用于含有敏感数据的方法，如登录、注册、修改密码等。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoLog {
    /** 原因说明（可选） */
    String value() default "";
}
