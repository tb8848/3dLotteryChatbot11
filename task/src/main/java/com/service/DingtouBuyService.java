package com.service;

import cn.hutool.core.lang.Snowflake;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.beans.*;
import com.config.RequestDataHelper;
import com.dao.ChatRoomMsgDAO;
import com.dao.DrawBuyRecordDAO;

import com.util.StringUtil;
import com.vo.BuyRecord3DVO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.swing.plaf.metal.MetalBorders;
import java.math.BigDecimal;
import java.util.*;


@Service
@Lazy
public class DingtouBuyService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ChatRoomMsgDAO dataDao;

    @Autowired
    private DrawBuyRecordDAO drawBuyRecordDAO;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private BuyRecord3DService buyRecord3DService;

    @Autowired
    private BuyRecord3DFastServiceV2 buyRecord3DFastServiceV2;

    @Autowired
    private BotUserSettingService botUserSettingService;

    @Autowired
    private DrawService drawService;

    @Autowired
    private P3DrawService p3DrawService;

    @Autowired
    private ReportToPanService reportToPanService;

    @Autowired
    private BotUserPanService botUserPanService;

    @Autowired
    private ChatRoomMsgService chatRoomMsgService;

    @Autowired
    private Snowflake snowflake;

    private Logger logger = LoggerFactory.getLogger(DingtouBuyService.class);



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

    public void runTask(PlayerFixedBuy dtTask,ChatRoomMsg fromMsg, BotUser botUser, Player player,List<BuyRecord3DVO> buyList){
//        logger.info(String.format("执行定投任务：%s的玩家%s定投%s",botUser.getLoginName(),player.getNickname(),dtTask.getBuyDesc()));
        dataDao.insert(fromMsg);
        rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(fromMsg));
        xiazhu(dtTask,botUser,player,fromMsg,buyList);
    }


    public void reportToPan(PlayerFixedBuy dtTask,BotUser botUser,Player player,ChatRoomMsg msg,List<BuyRecord3DVO> buyList,BotUserPan botUserPan){
        String reportToPanUrl = botUserPan.getLottery3dUrl();
        if(reportToPanUrl.endsWith("/")){
            reportToPanUrl = reportToPanUrl.substring(0,reportToPanUrl.length()-1);
        }

        if(reportToPanUrl.indexOf("aa.3d11bb.com")>-1){
            reportToPanUrl+=":9992";
        }else if(reportToPanUrl.indexOf("aa.pai3bb11.com")>-1){
            reportToPanUrl+=":9892";
        }else if(reportToPanUrl.indexOf("aa.3d11aa.com")>-1){
            reportToPanUrl+=":9092";
        }else if(reportToPanUrl.indexOf("aa.pai3aa11.com")>-1){
            reportToPanUrl+=":9292";
        }

        String lotName = "3D";
        if(dtTask.getLotteryType()==2){
            lotName = "P3";
        }else{
            lotName = "3D";
        }

        Draw draw = null;
        if(botUserPan.getLotteryType()==2){
            draw = p3DrawService.getLastDrawInfo();
        }else{
            draw = drawService.getLastDrawInfo();
        }
        BotUserSetting botUserSetting = botUserSettingService.getByUserId(botUser.getId());

        String playerBuyId = snowflake.nextIdStr();
        PlayerBuyRecord playerBuyRecord = new PlayerBuyRecord();
        playerBuyRecord.setId(playerBuyId);
        playerBuyRecord.setPlayerId(player.getId());
        playerBuyRecord.setBuyType(0);
        playerBuyRecord.setBotUserId(player.getBotUserId());
        playerBuyRecord.setDtTaskId(dtTask.getId());
        playerBuyRecord.setBuyFrom(1);

        String lmId = buyList.get(0).getLmId();
        Map<String,Object> buyResult = null;
        ResponseBean reportRespData = null;
        buyList.forEach(item->{
            item.setPlayerBuyId(playerBuyId);
        });
        if(dtTask.getFastBuyFlag()==1){
            reportRespData = reportToPanService.buyFast(reportToPanUrl,buyList,botUserPan.getLogin3dToken());
        }else{
            if("5".equals(lmId) || "13".equals(lmId)){
                reportRespData = reportToPanService.buyHs(reportToPanUrl,buyList,botUserPan.getLogin3dToken());
            }else{
                reportRespData = reportToPanService.buy(reportToPanUrl,buyList,botUserPan.getLogin3dToken());
            }
        }
        ChatRoomMsg toMsg = null;
        switch (reportRespData.getCode()){
            case 403: //盘口登录token已失效
                botUserPanService.clearInfo(dtTask.getLotteryType(),botUser.getId());
                String err = "机器人未登录"+lotName+"网盘";
                playerBuyRecord.setDtStatus(0);
                playerBuyRecord.setDtDesc(err);
                playerBuyRecordService.save(playerBuyRecord);
                toMsg = createMsg(botUser,player,err);
                toMsg.setSource(1);
                chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
                break;
            case -1:
            case 500:
                String errmsg = reportRespData.getMsg();
                playerBuyRecord.setDtStatus(0);
                playerBuyRecord.setDtDesc(errmsg);
                playerBuyRecordService.save(playerBuyRecord);
                toMsg = createMsg(botUser,player,errmsg);
                toMsg.setSource(1);
                chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
                break;
            case 0:
                Map<String,Object> dataMap = (Map<String,Object>)reportRespData.getData();
                JSONArray array = (JSONArray)dataMap.get("buyList");
                List<DrawBuyRecord> dataList = JSONArray.parseArray(JSON.toJSONString(array),DrawBuyRecord.class);
                if(null!=dataList && dataList.size()>0){
                    int buyAmount = dataList.size();
                    BigDecimal totalPoints = dataList.stream().map(item->item.getBuyMoney()).reduce(BigDecimal.ZERO,BigDecimal::add);
                    playerBuyRecord.setBuyAmount(dataList.size());
                    playerBuyRecord.setBuyPoints(totalPoints);
                    playerBuyRecord.setBuyStatus(0);
                    playerBuyRecord.setBuyTime(new Date());
                    playerBuyRecord.setDrawNo(draw.getDrawId());
                    playerBuyRecord.setBotUserId(botUser.getId());
                    playerBuyRecord.setBuyDesc(msg.getMsg());

                    playerBuyRecord.setKuaixuanRule(msg.getKuaixuanRule());
                    playerBuyRecord.setEarnPoints(BigDecimal.ZERO.subtract(totalPoints));
                    playerBuyRecord.setLotteryType(dtTask.getLotteryType());
                    playerBuyRecord.setDtStatus(1);
                    dataList.forEach(item->{
                        item.setBaopaiId(playerBuyId);
                        item.setVipId(player.getId()); //报网返回的数据，更改vipId的值为玩家ID
                        item.setBuyType(dtTask.getLotteryType());
                    });
                    Map dynamicPrama = new HashMap();
                    dynamicPrama.put("qihao", String.valueOf(draw.getDrawId()));
                    RequestDataHelper.setRequestData(dynamicPrama);

                    if(drawBuyRecordDAO.batchAddBuyCode(dataList)>0){
                        playerBuyRecordService.save(playerBuyRecord);
                        playerService.updatePoint(player.getId(),totalPoints,false);
                        BigDecimal points = playerService.getPoints(player.getId());
                        String newmsg = "["+lotName+"课号]"+draw.getDrawId()+"\r\n"+msg.getMsg()+"\r\n交作业成功√√\r\n【份数】："+buyAmount+"\r\n"
                                +"【扣面】："+totalPoints.stripTrailingZeros().toPlainString()+"\r\n"+"【盛鱼】："+points.stripTrailingZeros().toPlainString();

                        toMsg = new ChatRoomMsg();
                        toMsg.setFromUserId(botUser.getId());
                        toMsg.setFromUserNick("机器人");
                        toMsg.setFromUserType(1);
                        toMsg.setMsg(newmsg);
                        toMsg.setMsgType(0);
                        toMsg.setToUserNick(player.getNickname());
                        toMsg.setToUserId(player.getId());
                        toMsg.setCreateTime(new Date());
                        toMsg.setToUserType(0);
                        toMsg.setSource(1);
                        toMsg.setBotUserId(botUser.getId());
                        if(botUserSetting.getTuimaEnable()==1){//开启了退码
                            toMsg.setPlayerBuyId(playerBuyRecord.getId());
                            toMsg.setOptType(3);
                        }else{//未开启退码
                            toMsg.setPlayerBuyId(null);
                            toMsg.setOptType(1);
                        }
                        toMsg.setSource(1);
                        chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
                    }
                }else{
                    playerBuyRecord.setDtStatus(0);
                    playerBuyRecord.setDtDesc("盘口返回数据长度为0");
                    playerBuyRecordService.save(playerBuyRecord);
                }
                break;
        }

    }


    public void xiazhu(PlayerFixedBuy dtTask,BotUser botUser, Player player,ChatRoomMsg fromMsg, List<BuyRecord3DVO> buyList) {
        Draw draw = null;
        BigDecimal totalPoints = buyList.stream().map(item->item.getBuyMoney()).reduce(BigDecimal.ZERO,BigDecimal::add);
        int buyAmount = buyList.stream().mapToInt(item->item.getBuyAmount()).sum();
        String lotName = "3D";
        if(dtTask.getLotteryType()==2){
            lotName = "P3";
            draw = p3DrawService.getLastDrawInfo();
        }else{
            lotName = "3D";
            draw = drawService.getLastDrawInfo();
        }
        if(totalPoints.compareTo(player.getPoints())>0){
            String errmsg = "面上不足";
            dtTask.setStopReason(errmsg);
            PlayerBuyRecord playerBuyRecord = playerBuyRecordService.createFailRecord(player,botUser,dtTask,fromMsg.getMsg(),
                    draw.getDrawId(),totalPoints,buyAmount);
            playerBuyRecordService.save(playerBuyRecord);
            //玩家积分不够
            ChatRoomMsg toMsg = createMsg(botUser,player,errmsg);
            toMsg.setSource(1);
            chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
            return ;
        }
        if(player.getPretexting()==1 || player.getEatPrize()==1){
            String msg = fromMsg.getMsg();
            BotUserSetting botUserSetting = botUserSettingService.getByUserId(botUser.getId());
            String lmId = buyList.get(0).getLmId();
            Map<String,Object> buyResult = null;
            if(dtTask.getFastBuyFlag()==1){
                buyResult = buyRecord3DFastServiceV2.codesBatchBuy(player,buyList,draw,-2,null,msg,dtTask.getLotteryType());
            }else{
                if("5".equals(lmId) || "13".equals(lmId)){
                    buyResult = buyRecord3DService.buy3dHs(player,buyList,draw,-2,null,msg,dtTask.getLotteryType());
                }else{
                    buyResult = buyRecord3DService.codesBatchBuy(player,buyList,draw,-2,null,msg,dtTask.getLotteryType());
                }
            }

            if(buyResult.containsKey("playerBuyId")){
                String playerBuyId = (String)buyResult.get("playerBuyId");
                PlayerBuyRecord record = playerBuyRecordService.getById(playerBuyId);
                record.setDtDesc("");
                record.setDtStatus(1);
                record.setDtTaskId(dtTask.getId());
                record.setBuyFrom(1);
                playerBuyRecordService.updateById(record);
                BigDecimal points = playerService.getPoints(player.getId());
                String newmsg = "["+lotName+"课号]"+draw.getDrawId()+"\r\n"+msg+"\r\n交作业成功√√\r\n【份数】："+record.getBuyAmount()+"\r\n"
                        +"【扣面】："+record.getBuyPoints().stripTrailingZeros().toPlainString()+"\r\n"+"【盛鱼】："+points.stripTrailingZeros().toPlainString();

                ChatRoomMsg toMsg = new ChatRoomMsg();
                toMsg.setFromUserId(botUser.getId());
                toMsg.setFromUserNick("机器人");
                toMsg.setFromUserType(1);
                toMsg.setMsg(newmsg);
                toMsg.setToUserNick(player.getNickname());
                toMsg.setToUserId(player.getId());
                toMsg.setCreateTime(new Date());
                toMsg.setToUserType(0);
                toMsg.setMsgType(0);
                toMsg.setBotUserId(botUser.getId());
                if(botUserSetting.getTuimaEnable()==1){
                    toMsg.setPlayerBuyId(playerBuyId);
                    toMsg.setOptType(3);
                }else{
                    toMsg.setPlayerBuyId(null);
                    toMsg.setOptType(1);
                }
                toMsg.setMsgType(0);
                toMsg.setSource(1);
                chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
            }else{
                String errmsg = (String)buyResult.get("errmsg");
                if(StringUtil.isNotNull(errmsg)){
                    dtTask.setStopReason(errmsg);
                    PlayerBuyRecord playerBuyRecord = playerBuyRecordService.createFailRecord(player,botUser,dtTask,fromMsg.getMsg(),
                            draw.getDrawId(),totalPoints,buyAmount);
                    playerBuyRecordService.save(playerBuyRecord);

                    ChatRoomMsg toMsg = createMsg(botUser,player,errmsg);
                    toMsg.setSource(1);
                    chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
                }
            }
        }else{
            if(player.getReportNet()==1){
                BotUserPan botUserPan = botUserPanService.getOneBy(dtTask.getLotteryType(),player.getBotUserId());
                if(null!=botUserPan && StringUtil.isNotNull(botUserPan.getLogin3dToken())){
                    //报网
                    reportToPan(dtTask,botUser,player,fromMsg,buyList,botUserPan);
                }else{
                    String errmsg = "机器人未登录"+lotName+"网盘";
                    dtTask.setStopReason(errmsg);
                    PlayerBuyRecord playerBuyRecord = playerBuyRecordService.createFailRecord(player,botUser,dtTask,fromMsg.getMsg(),
                            draw.getDrawId(),totalPoints,buyAmount);
                    playerBuyRecordService.save(playerBuyRecord);

                    ChatRoomMsg toMsg = createMsg(botUser,player,errmsg);
                    toMsg.setSource(1);
                    chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
                }
            }
        }
    }

}
