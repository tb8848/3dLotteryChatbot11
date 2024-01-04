package com.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.exception.LockException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.beans.BotUser;
import com.beans.BotUserSetting;
import com.beans.ResponseBean;
import com.service.BotUserService;
import com.service.WechatApiService;
import com.task.runner.RecoverWechatSessionRunner;
import com.util.StringUtil;
import com.vo.WechatPushMsgVo;
import com.wechat.api.RespData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

@Component
@EnableAsync
public class HeatBeatTask {

    @Autowired
    private LockTemplate lockTemplate;

    @Autowired
    private BotUserService botUserService;


    @Autowired
    private WechatApiService wechatApiService;

    @Value("${wechat.api.url}")
    private String wechatApiUrl;

    private Logger logger = LoggerFactory.getLogger(HeatBeatTask.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 每分钟执行更新购买记录表退码状态，超过字典表配置分钟数则不可退码
     */
    @Scheduled(cron = "0/10 * * * * ?")
    //@Lock4j( expire = 60000, acquireTimeout = 1000)
    @Async
    public void task () {
        String lockKey = "3d:chatbot:heatbeat";
        final LockInfo lockInfo = lockTemplate.lock(lockKey,60000,30000);
        if (null == lockInfo) {
            throw new LockException("业务处理中，请稍后再试...");
        }
        String heartBeatUrl =  wechatApiUrl+"Login/HeartBeat?wxid=";
        String logoutUrl = wechatApiUrl + "Login/LogOut?wxid=";
        try {
            List<BotUser> botUserList = botUserService.listBy();
            if(null!=botUserList && botUserList.size()>0){
                List<BotUser> wxList = botUserList.stream().filter(item-> StringUtil.isNotNull(item.getWxId())).collect(Collectors.toList());
                for(BotUser botUser : wxList){
                    if(botUser.getWxStatus()==1){
                        if(botUser.getStatus()==2){
                            HttpRequest httpRequest = HttpUtil.createPost(logoutUrl+botUser.getWxId());
                            httpRequest.contentType("application/json");
                            HttpResponse httpResponse = httpRequest.execute();
                            String result = httpResponse.body();
                            logger.info("botUser:"+botUser.getLoginName()+">>>>>>>>>>Login/LogOut>>>>>>"+result);
                            //System.out.println(DateUtil.now()+">>>>>>Login/LogOut>>>>>>"+result);
                            RespData respData = JSONObject.parseObject(result, RespData.class);
                            if(respData.getCode()<0 || respData.getCode()==0){
                                //已退出微信
                                botUser.setWxId("");
                                botUser.setWxHeadimg("");
                                botUser.setWxStatus(0);
                                botUser.setWxNick("");
                                botUser.setQrUUid("");
                                botUser.setWxLoginTime(null);
                                botUserService.updateById(botUser);
                                wechatApiService.pushWxLoginStatusMsg(botUser,0);

                            }
                        }else{
                            HttpRequest httpRequest = HttpUtil.createPost(heartBeatUrl+botUser.getWxId());
                            httpRequest.contentType("application/json");
                            HttpResponse httpResponse = httpRequest.execute();
                            String result = httpResponse.body();
                            logger.info("botUser:"+botUser.getLoginName()+">>>>>>>>>>Login/HeartBeat>>>>>>"+result);
                            //System.out.println(DateUtil.now()+">>>>>>["+botUser.getLoginName()+"]Login/HeartBeat>>>>>>"+result);
                            if(result.startsWith("{") && result.endsWith("}")){
                                RespData respData = JSONObject.parseObject(result, RespData.class);
                                if(respData.getCode()<0){
                                    botUser.setWxStatus(2);
                                    botUser.setWxLoginTime(new Date());
                                    botUserService.updateById(botUser);
                                    wechatApiService.pushWxLoginStatusMsg(botUser,2);
                                }
                            }
                        }
                    }
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            lockTemplate.releaseLock(lockInfo);
        }

    }


}
