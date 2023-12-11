package com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoginService {

    @Autowired
    private RedisTemplate redisTemplate;

    public Map<String,Object> checkRelogin(String token){
        //两小时内未操作，自动退出
        int idleMinu = 120; //单位为分钟
        Map<String,Object> result = new HashMap<>();
        result.put("3d-chatbot-needLogin",0);
        result.put("3d-chatbot-validTime",idleMinu); //最长未操作等待时长,值单位为分钟，0表示无效
        if(redisTemplate.hasKey(token)){
            Long opTime = (Long)redisTemplate.boundValueOps(token).get();
            Long currTime = System.currentTimeMillis();
            long diff = (currTime-opTime)/1000 - idleMinu*60;
            if(diff>0){
                result.put("3d-chatbot-needLogin",1);
                result.put("3d-chatbot-validTime",idleMinu);
                return result;
            }
        }
        return result;
    }

}
