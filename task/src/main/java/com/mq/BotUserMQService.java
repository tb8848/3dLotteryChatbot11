package com.mq;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.exception.LockException;
import com.beans.BotUser;
import com.beans.PlayerFixedBuy;
import com.rabbitmq.client.Channel;
import com.service.BotUserService;
import com.service.PlayerFixedBuyService;
import com.service.kotlin.DingTouTaskService;
import com.service.kotlin.DummyBuyService;
import com.service.kotlin.SyncWechatMsgService;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BotUserMQService {

    @Autowired
    private LockTemplate lockTemplate;

    @Autowired
    private SyncWechatMsgService syncWechatMsgService;

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private DummyBuyService dummyBuyService;

    @Autowired
    private DingTouTaskService dingTouTaskService;

    @Autowired
    private PlayerFixedBuyService playerFixedBuyService;

    /**
     * 监听机器人账号登录消息队列
     * @param msg
     * @param channel
     */
    @RabbitListener(queues = "bot_user_queue_3d")
    public void recvMessage(Message msg, final Channel channel) throws IOException {
        String body = new String(msg.getBody());
        System.out.println(DateUtil.now()+">>>>>>>>>"+body);
        JSONObject data = JSON.parseObject(body);
        String lockKey = "3dbot:user:login";
        final LockInfo lockInfo = lockTemplate.lock(lockKey,60000,3600000);
        if (null == lockInfo) {
            throw new LockException("业务处理中，请稍后再试...");
        }
        try{
            String userId = data.getString("userId");
            String type = data.getString("type");
            Integer value = data.getIntValue("value");
            BotUser user = botUserService.getById(userId);
            if(null!=user){
                if("pcOnline".equals(type) && 1==value){
                    dummyBuyService.dummyBuy(user);
                }else if("wxOnline".equals(type) && 1==value){
                    syncWechatMsgService.getMsg(user);
                }else if("dingtou".equals(type) && 1==value){
                    String taskId = data.getString("taskId");
                    PlayerFixedBuy dtTask = playerFixedBuyService.getById(taskId);
                    if(null!=dtTask){
                        dingTouTaskService.startDingTou(dtTask); //启动定投任务
                    }
                }
            }
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        }catch (Exception e){
            e.printStackTrace();
            if (msg.getMessageProperties().getRedelivered()) {
                System.out.println("重复失败丢弃消息 plan:{}" + msg.getBody());
                channel.basicReject(msg.getMessageProperties().getDeliveryTag(), false);
            } else {
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, true);
            }
        }finally {
            if(null!=lockInfo){
                lockTemplate.releaseLock(lockInfo);
            }
        }
    }
}
