package com.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.beans.ChatRoom;
import com.beans.ChatRoomMsg;
import com.beans.Player;
import com.dao.BotUserDAO;
import com.dao.ChatRoomDAO;
import com.dao.ChatRoomMsgDAO;
import com.util.StringUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class ChatRoomMsgService extends ServiceImpl<ChatRoomMsgDAO, ChatRoomMsg> {

    @Autowired
    private ChatRoomMsgDAO dataDao;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WechatApiService wechatApiService;

    @Autowired
    private BotUserDAO botUserDAO;


    /**
     * 后台上下分消息推送
     * @param player
     * @param points
     * @param isUp
     */
    public void upDownPointsMsg(Player player, BigDecimal points, boolean isUp){
        BotUser botUser = botUserDAO.selectById(player.getBotUserId());
        String tlp = "【%s】：%s\r\n【当前盛鱼】：%s";
        String msgtxt = null;
        if(isUp){
            msgtxt ="后台上分";
        }else{
            msgtxt = "后台下分";
        }
        msgtxt = String.format(tlp,msgtxt,points.stripTrailingZeros().toPlainString(),player.getPoints().stripTrailingZeros().toPlainString());
        ChatRoomMsg toMsg = new ChatRoomMsg();
        toMsg.setFromUserId(player.getBotUserId());
        toMsg.setFromUserNick("机器人");
        toMsg.setFromUserType(1);
        toMsg.setMsgType(0);
        toMsg.setMsg(msgtxt);
        toMsg.setToUserNick(player.getNickname());
        toMsg.setToUserId(player.getId());
        toMsg.setCreateTime(new Date());
        toMsg.setBotUserId(player.getBotUserId());
        toMsg.setToUserType(0);
        toMsg.setOptType(0);
        dataDao.insert(toMsg);
        rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));

        //微信点对点消息
        if(StringUtil.isNotNull(player.getWxFriendId()) && StringUtil.isNotNull(botUser.getWxId())){
            if (player.getChatStatus() == 1){
                wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(),msgtxt,player.getWxGroup(),player.getNickname());
            }else{
                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(),msgtxt);
            }
//            wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(),msgtxt);
        }
    }



    public void pushMsg(ChatRoomMsg toMsg,Player player){
        BotUser botUser = botUserDAO.selectById(player.getBotUserId());
        dataDao.insert(toMsg);
        rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
        //微信点对点消息
        if(StringUtil.isNotNull(player.getWxFriendId()) && StringUtil.isNotNull(botUser.getWxId())){
            if (player.getChatStatus() == 1){
                wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(),toMsg.getMsg(),player.getWxGroup(),player.getNickname());
            }else{
                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(),toMsg.getMsg());
            }
//            wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(),toMsg.getMsg());
        }
    }



    /**
     * 微信相关消息推送
     * @param player
     * @param points
     * @param isUp
     */
    public void wechatyMsg(Player player, BigDecimal points, boolean isUp){
        String tlp = "【%s】：%s\r\n【当前盛鱼】：%s";
        String msgtxt = null;
        if(isUp){
            msgtxt ="后台上分";
        }else{
            msgtxt = "后台下分";
        }
        msgtxt = String.format(tlp,msgtxt,points.stripTrailingZeros().toPlainString(),player.getPoints().stripTrailingZeros().toPlainString());
        ChatRoomMsg toMsg = new ChatRoomMsg();
        toMsg.setFromUserId(player.getBotUserId());
        toMsg.setFromUserNick("机器人");
        toMsg.setFromUserType(1);
        toMsg.setMsgType(0);
        toMsg.setMsg(msgtxt);
        toMsg.setToUserNick(player.getNickname());
        toMsg.setToUserId(player.getId());
        toMsg.setCreateTime(new Date());
        toMsg.setBotUserId(player.getBotUserId());
        toMsg.setOptType(5);
        dataDao.insert(toMsg);
        rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));



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


    public List<ChatRoomMsg> listHistory(String botUserId, String endTime,Integer size) {
        LambdaQueryWrapper<ChatRoomMsg> qw = new LambdaQueryWrapper<>();
        qw.eq(ChatRoomMsg::getBotUserId,botUserId);
        qw.eq(ChatRoomMsg::getIsShow,1);
        qw.lt(ChatRoomMsg::getCreateTime,endTime);
        qw.orderByDesc(ChatRoomMsg::getId);
        qw.last("limit "+size);
        return dataDao.selectList(qw);
    }
}
