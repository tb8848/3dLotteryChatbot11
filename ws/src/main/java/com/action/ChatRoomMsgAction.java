package com.action;

import com.alibaba.fastjson.JSON;

import com.beans.*;
import com.config.ThreadPool;
import com.google.common.collect.Lists;
import com.service.*;
import com.util.StringUtil;
import com.util.Tools;
import com.websocket.DrawOpenStatusMessage;
import org.redisson.misc.Hash;
import org.simpleframework.xml.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 开关盘消息处理类
 */

/**
 * @author TZ
 * @since 2022.1.7
 */
@RestController
@Lazy
public class ChatRoomMsgAction {

//    @Autowired
//    private DrawService drawService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ChatRoomMsgService chatRoomMsgService;

    @Autowired
    private ThreadPoolExecutor threadPool;


    private Logger logger = LoggerFactory.getLogger(ChatRoomMsgAction.class);



    @SubscribeMapping("/topic/room/{userId}")
    public String subscribeRoomMsg(@DestinationVariable(value = "userId")String userId, Principal principal) throws Exception {
        //Thread.sleep(1000); // simulated delay
        logger.info("[topic/room]有人订阅了:"+userId);

        BotUser user = botUserService.getById(userId);
        if(null == user){
            return JSON.toJSONString(Lists.newArrayList());
        }
        //读取最新30条信息
        List<ChatRoomMsg> chatRoomMsgList = chatRoomMsgService.listLastN(userId,30);
        chatRoomMsgList = Lists.reverse(chatRoomMsgList);
        if(null!=chatRoomMsgList && chatRoomMsgList.size()>0){
            return JSON.toJSONString(chatRoomMsgList);
        }

        return JSON.toJSONString(Lists.newArrayList());
    }



    /**
     * 发送消息
     * @return
     * @throws Exception
     */
    @PostMapping("/sendMsg/{userId}")
    public ResponseBean sendMsg(@PathVariable("userId")String userId, @RequestBody ChatRoomMsg msg, HttpServletRequest request) throws Exception {
        //Thread.sleep(1000); // simulated delay

        BotUser user = botUserService.getById(userId);
        if(null == user){
            return new ResponseBean(-1,0,"参数错误",null,true);
        }

        String playerId = msg.getFromUserId();
        Player player = playerService.getById(playerId);
        if(null == player){
            return new ResponseBean(-1,0,"参数错误",null,true);
        }

        if(!player.getBotUserId().equals(user.getId())){
            return new ResponseBean(-1,0,"参数错误",null,true);
        }

        threadPool.execute(()->{
            chatRoomMsgService.handleMsg(user,msg,player);
        });
        //simpMessagingTemplate.convertAndSend("topic/room/{id}");

        return new ResponseBean(0,0,"",null,true);
    }


     //定义消息请求路径
    @MessageMapping("/send/{userId}")
    public String sendMsg(@DestinationVariable(value = "userId")String userId, String value){
        // 客户端发送消息到服务端

        logger.info("收到客户端["+userId+"]的消息："+value);
        //System.out.println("["+userId+"]"+"receive message from client:"+value);

        ChatRoomMsg msg = JSON.parseObject(value,ChatRoomMsg.class);

        BotUser user = botUserService.getById(userId);
        if(null == user){
            ResponseBean error = new ResponseBean(-1,0,"参数错误",null,true);
            return JSON.toJSONString(error);

        }

        String playerId = msg.getFromUserId();
        Player player = playerService.getById(playerId);
        if(null == player){
            ResponseBean error = new ResponseBean(-1,0,"参数错误",null,true);
            return JSON.toJSONString(error);
        }

        if(!player.getBotUserId().equals(user.getId())){
            ResponseBean error = new ResponseBean(-1,0,"参数错误",null,true);
            return JSON.toJSONString(error);
        }

        threadPool.execute(()->{
            chatRoomMsgService.handleMsg(user,msg,player);
        });

        // 这里返回消息到'/sub/chat'订阅路径
        ResponseBean OK = new ResponseBean(0,0,"",null,true);
        return JSON.toJSONString(OK);
    }

}
