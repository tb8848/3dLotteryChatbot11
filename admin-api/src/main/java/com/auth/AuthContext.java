package com.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Component
public class AuthContext {

    @Resource
    private HttpServletRequest request;

    public AuthInfo getInfo() {

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //设置子线程共享
        RequestContextHolder.setRequestAttributes(servletRequestAttributes,true);
        return (AuthInfo) request.getAttribute("robot-admin");
    }


    public void setInfo(AuthInfo authInfo) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //设置子线程共享
        RequestContextHolder.setRequestAttributes(servletRequestAttributes,true);
        request.setAttribute("robot-admin", authInfo);
    }


}
