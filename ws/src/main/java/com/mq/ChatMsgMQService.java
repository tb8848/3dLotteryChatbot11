package com.mq;

import com.alibaba.fastjson.JSON;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.exception.LockException;
import com.beans.ChatRoomMsg;
import com.rabbitmq.client.Channel;
import com.service.ChatRoomMsgService;
import com.websocket.DrawOpenStatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ChatMsgMQService {

    @Autowired
    private ChatRoomMsgService chatRoomMsgService;

    @Autowired
    private LockTemplate lockTemplate;


    private Logger logger = LoggerFactory.getLogger(ChatMsgMQService.class);


    /**
     * 推送开关盘消息
     * @param msg
     * @param channel
     */
    @RabbitListener(queues = "bot_chatmsg_queue_3d")
    public void recvMessage(Message msg, final Channel channel) throws IOException {
        String body = new String(msg.getBody());
//        logger.info("收到聊天消息>>>>>>>>"+body);
        //System.out.println("【3D】收到消息>>>>>>>>"+body);
        String lockKey = "3d:chatbot:chatmsg:mq";
        final LockInfo lockInfo = lockTemplate.lock(lockKey,60000,30000);
        if (null == lockInfo) {
            throw new LockException("业务处理中，请稍后再试...");
        }
        try{
            ChatRoomMsg msgData = JSON.parseObject(body, ChatRoomMsg.class);
            String userId = msgData.getBotUserId();
            chatRoomMsgService.sendbackMsg(userId,msgData);
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
            if(msg.getMessageProperties().getRedelivered()){
//                logger.info("重复失败丢弃消息>>>>>>>>"+body);
                //System.out.println("重复失败丢弃消息 plan:{}"+msg.getBody());
                channel.basicReject(msg.getMessageProperties().getDeliveryTag(), false);
            }else{
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false,true);
            }
        }finally {
            if(null!=lockInfo){
                lockTemplate.releaseLock(lockInfo);
            }
        }
    }

}
