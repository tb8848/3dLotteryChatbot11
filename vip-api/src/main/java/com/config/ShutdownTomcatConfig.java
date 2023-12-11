package com.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * curl -u admin:ddks123456  -X POST http://127.0.0.1:12888/ddks/shutdown
 */
@Configuration
public class ShutdownTomcatConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.headers().frameOptions().disable();

        //其他所有的放行
        http.authorizeRequests().antMatchers("**/**").permitAll();

        //这个要密码的
        http.csrf().disable().authorizeRequests().antMatchers("/caipiao/**").permitAll()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR")
                .and()
                //开启basic认证，若不添加此项，将不能通过curl的basic方式传递用户信息
                .httpBasic();


    }



}
