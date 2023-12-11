package com.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.exception.LockException;
import com.beans.BotUser;
import com.service.BotUserService;
import com.service.WechatApiService;
import com.util.StringUtil;
import com.wechat.api.RespData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

//@Component
//@EnableAsync
@Deprecated
public class RecvMsgTask {

    @Autowired
    private LockTemplate lockTemplate;

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private WechatApiService wechatApiService;

    @Value("${wechat.api.url}")
    private String wechatApiUrl;

    /**
     * 每分钟执行更新购买记录表退码状态，超过字典表配置分钟数则不可退码
     */
    //@Scheduled(cron = "0/5 * * * * ?")
    //@Lock4j( expire = 60000, acquireTimeout = 1000)
    //@Async
    public void task () {
        String lockKey = "3d:chatbot:recvmsg";
        final LockInfo lockInfo = lockTemplate.lock(lockKey,60000,30000);
        if (null == lockInfo) {
            throw new LockException("业务处理中，请稍后再试...");
        }
        try {
            List<BotUser> botUserList = botUserService.listBy();
            if(null!=botUserList && botUserList.size()>0){
                List<BotUser> wxList = botUserList.stream().filter(item-> StringUtil.isNotNull(item.getWxId())).collect(Collectors.toList());
                for(BotUser botUser : wxList){
                    if(botUser.getWxStatus()==1){
                        long st = System.currentTimeMillis();
                        wechatApiService.receiveMsg(botUser);
                        long et = System.currentTimeMillis();
                        //System.out.println("====================耗时："+(et-st)+"ms");
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
