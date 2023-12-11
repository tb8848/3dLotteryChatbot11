package com.config;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.HttpMethod;
import com.auth.AuthContext;
import com.auth.AuthInfo;
import com.auth.OpLogIpUtil;
import com.beans.BotUser;
import com.beans.ResponseBean;

import com.service.BotUserService;

import com.service.LoginService;
import com.util.IpUtil;
import com.util.JwtUtil;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Value("${management.server.port}")
    private int shutdownPort;

    @Resource
    AuthContext authContext;

    @Autowired
    private BotUserService botUserService;

    @Resource
    OpLogIpUtil opLogIpUtil;

    @Resource
    private LoginService loginService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 在请求处理之前进行调用（Controller方法调用之前）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        response.setContentType("text/html;charset=utf-8");
        ResponseBean responseBean = new ResponseBean(403, "登录失效", null);
        String str = JSONObject.toJSONString(responseBean);

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        String url = ((HandlerMethod) handler).getMethod().getName();
        String u = request.getRequestURI();

        AuthInfo authInfo = new AuthInfo();
        authInfo.setUri(url);
        authInfo.setIp(IpUtil.getIpAddr(request));
        String userAgent = request.getHeader("User-Agent");
        if (StringUtils.isEmpty(userAgent)) {
            userAgent = request.getHeader("deviceID");
        }
        authInfo.setDevice(opLogIpUtil.userAgentToDevice(userAgent));
        authInfo.setCity(opLogIpUtil.getCityInfo(IpUtil.getIpAddr(request)));

        // 判断接口是否需要登录
        NoLogin methodAnnotation = method.getAnnotation(NoLogin.class);
        if (methodAnnotation != null) {
            authContext.setInfo(authInfo);
            return true;
        }

        //优雅关机接口
        if (request.getServerPort()==shutdownPort)
        {
            return true;
        }


        String token;
        String from = "app";
        try {
            token = request.getHeader("token");
            from = request.getHeader("reqFrom");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (StringUtil.isNull(token)){
            try {
                response.getWriter().println(str);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return false;
        }

        String uid = JwtUtil.getUsername(token);
        BotUser admin = botUserService.getById(uid);
        try {
            if (admin != null) {

                if(url.contains("verifyLogin")){//登录验证接口，不处理
                    return true;
                }
                authInfo.setUserName(admin.getLoginName());
                authInfo.setUserId(admin.getId());
                authContext.setInfo(authInfo);

                //判断当前登录用户停止操作的间隔时间
                Map<String,Object> result = loginService.checkRelogin(token);
                Integer needRelogin = (Integer)result.get("3d-chatbot-needLogin");
                Integer validTime = (Integer)result.get("3d-chatbot-validTime"); //最大失效时间，单位为分钟
                if(null!=needRelogin && needRelogin==1){
                    try {
                        //更改登录用户的在线状态
                        int onLineStatus = 0;
                        admin.setOnlineStatus(onLineStatus);
                        botUserService.updateOnlineStatus(admin.getId(),0,0,onLineStatus);
                        stringRedisTemplate.boundValueOps("3d:chatbot:online:"+uid).set("0");
                        response.getWriter().println(str);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    return false;
                }else{
                    if(null!=needRelogin && needRelogin==0 && null!=validTime && validTime>0){
                        //System.out.println(new Date()+"=====================更新用户操作时间");
                        stringRedisTemplate.boundValueOps(token).set(new Date().getTime()+"",validTime+10, TimeUnit.MINUTES);
                        stringRedisTemplate.boundValueOps("3d:chatbot:online:"+uid).set("1");
                    }
                }

                return true;
            }else{
                try {
                    response.getWriter().println(str);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.getWriter().println(str);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } finally {

        }
        return false;//如果设置为false时，被请求时，拦截器执行到此处将不会继续操作
    }

    /**
     * 请求处理之后进行调用，但是在视图被渲染之前（Controller方法调用之后）
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    /**
     * 在整个请求结束之后被调用，也就是在DispatcherServlet 渲染了对应的视图之后执行（主要是用于进行资源清理工作）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }
}
