package com.config;

import java.lang.annotation.*;

/**
 * 在需要登录验证的Controller的方法上使用此注解
 */
@Target({ElementType.METHOD})// 可用在方法名上
@Retention(RetentionPolicy.RUNTIME)// 运行时有效
@Documented
public @interface NoLogin {
}
