package com.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.beans.ChatRoomMsg;
import com.beans.Player;
import com.dao.BotUserDAO;
import com.dao.ChatRoomMsgDAO;
import com.util.StringUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class ChatRoomMsgService extends ServiceImpl<ChatRoomMsgDAO, ChatRoomMsg> {

    @Autowired
    private ChatRoomMsgDAO dataDao;

    @Value("${wechat.api.url}")
    private String wechatApiUrl;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    public ChatRoomMsg createFromWxMsg(BotUser botUser,Player player,String text){
        ChatRoomMsg fromMsg = new ChatRoomMsg();
        fromMsg.setFromUserId(player.getId());
        fromMsg.setFromUserImg(player.getHeadimg());
        fromMsg.setFromUserNick(player.getNickname());
        fromMsg.setOptType(0);
        fromMsg.setFromUserType(0);
        fromMsg.setMsgType(0);
        fromMsg.setToUserId(botUser.getId());
        fromMsg.setBotUserId(botUser.getId());
        fromMsg.setToUserType(2);
        fromMsg.setCreateTime(new Date());
        fromMsg.setMsg(text);
        fromMsg.setSource(1);
        return fromMsg;
    }

    public ChatRoomMsg createMsg(BotUser botUser,Player player,String msg){
        ChatRoomMsg toMsg = new ChatRoomMsg();
        toMsg.setFromUserId(botUser.getId());
        toMsg.setFromUserNick("机器人");
        toMsg.setFromUserType(1);
        toMsg.setMsg(msg);
        toMsg.setMsgType(0);
        toMsg.setToUserType(0);
        toMsg.setToUserNick(player.getNickname());
        toMsg.setToUserId(player.getId());
        toMsg.setCreateTime(new Date());
        toMsg.setBotUserId(botUser.getId());
        toMsg.setOptType(0);
        return toMsg;
    }

    public ChatRoomMsg getErrorMsg(BotUser botUser,Player player){
        ChatRoomMsg toMsg = new ChatRoomMsg();
        toMsg.setFromUserId(botUser.getId());
        toMsg.setFromUserNick("机器人");
        toMsg.setFromUserType(1);
        toMsg.setMsg("格式错误");
        toMsg.setMsgType(0);
        toMsg.setToUserType(0);
        toMsg.setToUserNick(player.getNickname());
        toMsg.setToUserId(player.getId());
        toMsg.setCreateTime(new Date());
        toMsg.setBotUserId(botUser.getId());
        toMsg.setOptType(0);
        toMsg.setSource(1);
        return toMsg;
    }



    @Async
    public void sendMsg(String toWxId, String wxId, String text){
        String url = wechatApiUrl+"Msg/SendTxt";
        Map<String,Object> reqData = new HashMap<>();
        reqData.put("Content",text);
        reqData.put("ToWxid",toWxId);
        reqData.put("Type",1);
        reqData.put("Wxid",wxId);

        HttpRequest httpRequest = HttpUtil.createPost(url);
        httpRequest.contentType("application/json");
        httpRequest.body(JSON.toJSONString(reqData));
        HttpResponse httpResponse = httpRequest.execute();
        String result = httpResponse.body();
        System.out.println(DateUtil.now()+">>>>>>Msg/SendTxt>>>>>>"+result);

    }


    public void saveAndSendMsg(ChatRoomMsg toMsg,String toWxId,String fromWxId){
        dataDao.insert(toMsg);
        rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(toMsg));
        sendMsg(toWxId, fromWxId, toMsg.getMsg());
    }


}
