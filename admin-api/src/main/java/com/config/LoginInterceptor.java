package com.config;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.HttpMethod;
import com.auth.AuthContext;
import com.auth.AuthInfo;
import com.auth.OpLogIpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.beans.ResponseBean;
import com.beans.Admin;
import com.service.AdminService;
import com.service.BotUserService;
import com.util.IpUtil;
import com.util.JwtUtil;
import com.util.StringUtil;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Value("${management.server.port}")
    private int shutdownPort;

    @Resource
    AuthContext authContext;

    @Resource
    private AdminService adminService;

    @Resource
    OpLogIpUtil opLogIpUtil;

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
        String uri = request.getServletPath();

       // System.out.println("uri------------"+uri);

        //优雅关机接口
        if (request.getServerPort()==shutdownPort)
        {
            return true;
        }

//        System.out.println("------------"+url);
        if ("download".equals(url)){
            return true;
        }

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

        String token;
        try {
            token = request.getHeader("token");
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
        Admin admin = adminService.getById(uid);
        try {
            if (admin != null) {
                authContext.setInfo(authInfo);
                return true;
            }else{
                //子账号登录
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

    public boolean authAgent(String url) {
        // peiRateChange, openQuotation,bpSetting,dingPan, notice, /admin/shipan, /admin/drawbuy/bpshipan, chuHuo,shiHuo
        List<String> authList = Lists.newArrayList("/draw/settleAccounts", "/draw/frontendShow", "/periodOperationLog/selectPeriodOperationLogPage",
                "/fixedDiscLog/selectFixeDiscLogPage", "/VipModifyLog/getVipModifyLog", "/bagCardLog/selectBagCardLogPage", "/oddsChange/selectOddsChangeLogPage",
                "/packageLog/selectPackageLogPage", "/inBatchesOddsLog/selectInBatchesOddsLog", "/basicSetting/updateBasicSetting",
                "/admin/stopBuyCodes/listAll", "/notice/addNituce", "/notice/selectNoticeByPage", "/notice/getNoticeById", "/notice/updateNotice",
                "/notice/deleteNotice");
        if (authList.contains(url)) {
            return false;
        }else if (url.startsWith("/peiRateChange") || url.startsWith("/openQuotation") || url.startsWith("/bpSetting") || url.startsWith("/dingPan")
                || url.startsWith("/shiHuo") || url.startsWith("/admin/shipan") || url.startsWith("/admin/drawbuy/bpshipan") || url.startsWith("/chuHuo")
                || url.startsWith("/admin/xupan")) {
            return false;
        }
        return true;
    }
}