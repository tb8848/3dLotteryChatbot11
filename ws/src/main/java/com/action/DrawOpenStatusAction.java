package com.action;

import com.alibaba.fastjson.JSON;
import com.beans.Draw;
import com.service.BuyRecord3DService;
import com.service.DrawService;
import com.service.P3DrawService;
import com.websocket.DrawOpenStatusMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * 开关盘消息处理类
 */

/**
 * @author TZ
 * @since 2022.1.7
 */
@RestController
@Lazy
public class DrawOpenStatusAction {

    @Autowired
    private DrawService drawService;

    @Autowired
    private P3DrawService p3DrawService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private BuyRecord3DService buyRecord3DService;

    @SubscribeMapping("/topic/drawOpenStatus/3d")
    public String subscribeDrawOpenStatus(Principal principal) throws Exception {
        //logger.info("【3D】有人订阅了:"+principal.getName());
        Draw draw = drawService.getLastDrawInfo();

        DrawOpenStatusMessage msg = new DrawOpenStatusMessage();
        msg.setDrawNo(draw.getDrawId());
        msg.setId(draw.getId());
        msg.setOpenStatus(draw.getOpenStatus());
        if(draw.getOpenStatus()==3){
            //开盘中
            if(draw.getOpenDateTime()!=null){
                long leftMillSeconds = draw.getOpenDateTime().getTime();
                msg.setLeftMillSeconds(leftMillSeconds);
            }else{
                msg.setLeftMillSeconds(0);
            }
        }else if(draw.getOpenStatus()==1){
            if(draw.getCloseDateTime()!=null){
                long leftMillSeconds = draw.getCloseDateTime().getTime();
                msg.setLeftMillSeconds(leftMillSeconds);
            }else{
                msg.setLeftMillSeconds(0);
            }
        }else{
            msg.setLeftMillSeconds(0);
        }
        String content = JSON.toJSONString(msg);
        if(draw.getOpenStatus()==1){
            redisTemplate.boundValueOps("3dchatbot-pushLeftTime").set(1);
            redisTemplate.boundValueOps("3dchatbot-pushData").set(content);
        }
        buyRecord3DService.copyTable(draw.getDrawId());
        Map<String,Object> msgBody = new HashMap<>();
        msgBody.put("type",1);
        msgBody.put("data",msg);
        return JSON.toJSONString(msgBody);
    }


    @SubscribeMapping("/topic/drawOpenStatus/p3")
    public String subscribeDrawOpenStatusOfP3(Principal principal) throws Exception {
        //System.out.println("【P3】有人订阅了:"+principal.getName());
        Draw draw = p3DrawService.getLastDrawInfo();

        DrawOpenStatusMessage msg = new DrawOpenStatusMessage();
        msg.setDrawNo(draw.getDrawId());
        msg.setId(draw.getId());
        msg.setOpenStatus(draw.getOpenStatus());
        if(draw.getOpenStatus()==3){
            //开盘中
            if(draw.getOpenDateTime()!=null){
                long leftMillSeconds = draw.getOpenDateTime().getTime();
                msg.setLeftMillSeconds(leftMillSeconds);
            }else{
                msg.setLeftMillSeconds(0);
            }
        }else if(draw.getOpenStatus()==1){
            if(draw.getCloseDateTime()!=null){
                long leftMillSeconds = draw.getCloseDateTime().getTime();
                msg.setLeftMillSeconds(leftMillSeconds);
            }else{
                msg.setLeftMillSeconds(0);
            }
        }else{
            msg.setLeftMillSeconds(0);
        }
        String content = JSON.toJSONString(msg);
        if(draw.getOpenStatus()==1){
            redisTemplate.boundValueOps("p3chatbot-pushLeftTime").set(1);
            redisTemplate.boundValueOps("p3chatbot-pushData").set(content);
        }
        buyRecord3DService.copyTable(draw.getDrawId());
        Map<String,Object> msgBody = new HashMap<>();
        msgBody.put("type",1);
        msgBody.put("data",msg);
        return JSON.toJSONString(msgBody);
    }

    /**
     * 订阅重登操作
     * @param principal
     * @return
     * @throws Exception
     */
    @SubscribeMapping("/user/queue/relogin")
    public String subscribeReLogin(Principal principal) throws Exception {
        //Thread.sleep(1000); // simulated delay
        System.out.println("有人订阅了[重登消息]:"+principal.getName());
        return "订阅成功";
    }

}
