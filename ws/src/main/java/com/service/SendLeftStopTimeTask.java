package com.service;

import com.alibaba.fastjson.JSON;
import com.websocket.DrawOpenStatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 发送停盘剩余时间任务
 * 用while循环实现每秒执行效果
 * 在spring boot 启动完成后执行
 */
@Component
@Lazy
public class SendLeftStopTimeTask implements ApplicationRunner {

    public static Logger logger = LoggerFactory.getLogger(SendLeftStopTimeTask.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

//    @Autowired
//    private BasicSettingService basicSettingService;

    private void init() {
//        logger.info("【3D】开盘/停盘剩余时间推送任务开启=================");
        redisTemplate.boundValueOps("3dchatbot-pushLeftTime").set(0);
        while(true){
            long sleepTime = 1000;
            try {
                Integer pushLeftTimeFlag = (Integer) redisTemplate.opsForValue().get("3dchatbot-pushLeftTime");
                if (null!=pushLeftTimeFlag && pushLeftTimeFlag == 1) {
                    long currTime = new Date().getTime();
                    String data = (String) redisTemplate.opsForValue().get("3dchatbot-pushData");
                    DrawOpenStatusMessage msgData = JSON.parseObject(data, DrawOpenStatusMessage.class);
                    if(msgData.getOpenStatus()==1 || msgData.getOpenStatus()==3){
                        sleepTime = 1000;
                    }else{
                        sleepTime = 2000;
                    }
                    Map<String,Object> msgBody = new HashMap<>();
                    msgBody.put("type",1);
                    msgBody.put("data",msgData);
                    simpMessagingTemplate.convertAndSend("/topic/drawOpenStatus/3d",msgBody);
                    Thread.sleep(sleepTime);
                } else {
                    Thread.sleep(sleepTime);
                }
            }catch (Exception e){
//                logger.error("停盘剩余时间推送任务-----------error");
                e.printStackTrace();
            }
        }
    }


    private void initP3() {
//        logger.info("【P3】开盘/停盘剩余时间推送任务开启=================");
        redisTemplate.boundValueOps("p3chatbot-pushLeftTime").set(0);
        while(true){
            long sleepTime = 1000;
            try {
                Integer pushLeftTimeFlag = (Integer) redisTemplate.opsForValue().get("p3chatbot-pushLeftTime");
                if (null!=pushLeftTimeFlag && pushLeftTimeFlag == 1) {
                    long currTime = new Date().getTime();
                    String data = (String) redisTemplate.opsForValue().get("p3chatbot-pushData");
                    DrawOpenStatusMessage msgData = JSON.parseObject(data, DrawOpenStatusMessage.class);
                    if(msgData.getOpenStatus()==1 || msgData.getOpenStatus()==3){
                        sleepTime = 1000;
                    }else{
                        sleepTime = 2000;
                    }
                    Map<String,Object> msgBody = new HashMap<>();
                    msgBody.put("type",1);
                    msgBody.put("data",msgData);
                    simpMessagingTemplate.convertAndSend("/topic/drawOpenStatus/p3",msgBody);
                    Thread.sleep(sleepTime);
                } else {
                    Thread.sleep(sleepTime);
                }
            }catch (Exception e){
//                logger.error("停盘剩余时间推送任务-----------error");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        new Thread(()->{
            init();
        }).start();
        new Thread(()->{
            initP3();
        }).start();
    }
}
