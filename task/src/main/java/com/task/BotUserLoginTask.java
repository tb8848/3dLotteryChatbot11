package com.task;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.exception.LockException;
import com.beans.BotUser;
import com.service.BotUserService;
import com.service.WechatApiService;
import com.service.kotlin.DummyBuyService;

import com.service.kotlin.SyncWechatMsgService;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BotUserLoginTask {

    @Autowired
    private LockTemplate lockTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private WechatApiService wechatApiService;

    @Value("${wechat.api.url}")
    private String wechatApiUrl;

    @Autowired
    private SyncWechatMsgService syncWechatMsgService;

    @Autowired
    private DummyBuyService dummyBuyService;

    /**
     * 监听机器人账号的登录状态
     * 包括PC登录和微信登录
     */
    //@Scheduled(cron = "0/3 * * * * ?")
    public void task () {
        String lockKey = "3d:chatbot:login";
        final LockInfo lockInfo = lockTemplate.lock(lockKey,60000,30000);
        if (null == lockInfo) {
            throw new LockException("业务处理中，请稍后再试...");
        }
        try {
//            Set<String> newWxLoginSet = redisTemplate.boundValueOps("3d:chatbot:newWxLogin").members();
//            Set<String> newUserSet = redisTemplate.boundSetOps("3d:chatbot:newLogin").members();
//            if(null!=newUserSet && newUserSet.size()>0){
//                List<BotUser> botUserList = botUserService.listByIds(newUserSet);
//                for(BotUser botUser : botUserList){
//                    if(botUser.getOnlineStatus()==1){
//                        redisTemplate.boundSetOps("3d:chatbot:newLogin").remove(botUser.getId());
//                        dummyBuyService.dummyBuy(botUser);
//                    }
//                }
//            }
//            if(null!=newWxLoginSet && newWxLoginSet.size()>0){
//                List<BotUser> botUserList = botUserService.listByIds(newWxLoginSet);
//                for(BotUser botUser : botUserList){
//                    if(botUser.getWxStatus()==1){
//                        redisTemplate.boundSetOps("3d:chatbot:newWxLogin").remove(botUser.getId());
//                        syncWechatMsgService.getMsg(botUser);
//                    }
//                }
//            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            lockTemplate.releaseLock(lockInfo);
        }

    }


}
