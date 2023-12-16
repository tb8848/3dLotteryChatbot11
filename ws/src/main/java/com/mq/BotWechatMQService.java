package com.mq;

import com.alibaba.fastjson.JSON;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.exception.LockException;
import com.beans.ChatRoomMsg;
import com.beans.ResponseBean;
import com.rabbitmq.client.Channel;
import com.service.ChatRoomMsgService;
import com.vo.WechatPushMsgVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BotWechatMQService {

    @Autowired
    private ChatRoomMsgService chatRoomMsgService;

    private Logger logger = LoggerFactory.getLogger(BotWechatMQService.class);

    @Autowired
    private LockTemplate lockTemplate;


    /**
     * 推送开关盘消息
     * @param msg
     * @param channel
     */
    @RabbitListener(queues = "bot_wechat_queue_3d")
    public void recvMessage(Message msg, final Channel channel) throws IOException {
        String body = new String(msg.getBody());
//        logger.info("收到微信状态消息>>>>>>>>"+body);
        String lockKey = "3d:chatbot:wechat:mq";
        final LockInfo lockInfo = lockTemplate.lock(lockKey,60000,30000);
        if (null == lockInfo) {
            throw new LockException("业务处理中，请稍后再试...");
        }
        //System.out.println("【3D】收到消息>>>>>>>>"+body);
        try{
            WechatPushMsgVo msgData = JSON.parseObject(body, WechatPushMsgVo.class);
            String userId = msgData.getBotUserId();
            ResponseBean respData = msgData.getResponseBean();
            chatRoomMsgService.sendWechatBackMsg(userId,respData);
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
            lockTemplate.releaseLock(lockInfo);
        }


    }

}
