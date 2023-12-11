package com.action;


import com.auth.AuthContext;
import com.auth.AuthInfo;
import com.beans.BotUser;
import com.beans.BotUserSetting;
import com.beans.ResponseBean;
import com.config.NoLogin;
import com.service.BotUserService;
import com.service.BotUserSettingService;
import com.util.JwtUtil;
import com.util.MD5Util;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping(value = "/bot/setting")
public class BotUserSettingAction {

    @Autowired
    private BotUserSettingService botUserSettingService;

    @GetMapping(value = "/one")
    public ResponseBean get(@RequestHeader(value = "token")String token,HttpServletRequest request){
        String uid = JwtUtil.getUsername(token);

        if(StringUtil.isNull(uid)){
            return new ResponseBean(403,0,"请重新登录",null,true);//用户名或密码为空
        }

        try{
        BotUserSetting botUserSetting = botUserSettingService.getByUserId(uid);
        if(null == botUserSetting) {
            return new ResponseBean(-1,0,"数据错误",null,true);//用户名或密码为空
        }
            return new ResponseBean(0, 0, "", botUserSetting);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, 0, "系统繁忙", null,true);
        }

    }
}
