package com.service;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.*;
import com.dao.PlayerBuyRecordDAO;
import com.util.StringUtil;
import org.assertj.core.util.Lists;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class PlayerBuyRecordService extends ServiceImpl<PlayerBuyRecordDAO, PlayerBuyRecord> {

    @Autowired
    private PlayerBuyRecordDAO dataDao;

    @Autowired
    private DrawService drawService;

    @Autowired
    private P3DrawService p3DrawService;

    @Autowired
    private ChatRoomMsgService chatRoomMsgService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WechatApiService wechatApiService;


    @Autowired
    private ReportToPanService reportToPanService;

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private BuyRecord3DService buyRecord3DService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private BotUserPanService botUserPanService;

    public IPage<PlayerBuyRecord> getByPage(Integer pageNo, Integer pageSize, Integer buyType, String playerId,String startTime,String endTime,String uid){
        Page<PlayerBuyRecord> page = new Page(pageNo,pageSize);
        LambdaQueryWrapper<PlayerBuyRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(StringUtil.isNotNull(playerId)){
            lambdaQueryWrapper.eq(PlayerBuyRecord::getPlayerId,playerId);
        }else{
            lambdaQueryWrapper.eq(PlayerBuyRecord::getBotUserId,uid);
            lambdaQueryWrapper.in(PlayerBuyRecord::getBuyFrom,Lists.newArrayList(0,1));
        }
        if(buyType!=-1){
            lambdaQueryWrapper.eq(PlayerBuyRecord::getBuyType,buyType);
        }
        lambdaQueryWrapper.ge(PlayerBuyRecord::getBuyTime,startTime);
        lambdaQueryWrapper.le(PlayerBuyRecord::getBuyTime,endTime);
        lambdaQueryWrapper.in(PlayerBuyRecord::getBuyStatus, Lists.newArrayList(0,1));
        lambdaQueryWrapper.orderByDesc(PlayerBuyRecord::getBuyTime);
        return dataDao.selectPage(page,lambdaQueryWrapper);

    }


    public IPage<PlayerBuyRecord> todayTopN(Integer pageNo, Integer pageSize,String playerId,String startTime,String endTime,String uid){
        Page<PlayerBuyRecord> page = new Page(pageNo,pageSize);
        LambdaQueryWrapper<PlayerBuyRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(StringUtil.isNotNull(playerId)){
            lambdaQueryWrapper.eq(PlayerBuyRecord::getPlayerId,playerId);
        }else{
            lambdaQueryWrapper.eq(PlayerBuyRecord::getBotUserId,uid);
        }
        lambdaQueryWrapper.eq(PlayerBuyRecord::getBuyFrom,0); //仅查询手动下注的记录
        lambdaQueryWrapper.ge(PlayerBuyRecord::getBuyTime,startTime);
        lambdaQueryWrapper.le(PlayerBuyRecord::getBuyTime,endTime);
        lambdaQueryWrapper.in(PlayerBuyRecord::getBuyStatus, Lists.newArrayList(0,1));
        lambdaQueryWrapper.orderByDesc(PlayerBuyRecord::getBuyTime);
        return dataDao.selectPage(page,lambdaQueryWrapper);

    }

    /**
     * 清空玩家购买记录
     * 只清空已结算和已退码的数据
     * @param playerId
     */
    public void clearData(String playerId){
        LambdaUpdateWrapper<PlayerBuyRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PlayerBuyRecord::getPlayerId,playerId);
        updateWrapper.in(PlayerBuyRecord::getBuyStatus,Lists.newArrayList(1,-1));
        dataDao.delete(updateWrapper);
    }


    public PlayerBuyRecord countData(String playerId){
        QueryWrapper<PlayerBuyRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sum(buyPoints) as buyPoints,sum(earnPoints) as earnPoints");
        Date startTime  = DateUtil.beginOfDay(new Date());
        Date endTime = DateUtil.endOfDay(new Date());
        queryWrapper.between("buyTime", startTime,endTime);
        queryWrapper.lambda().eq(PlayerBuyRecord::getPlayerId,playerId).in(PlayerBuyRecord::getBuyStatus,0,1);
        return dataDao.selectOne(queryWrapper);
    }

    public PlayerBuyRecord countDayData(String botUserId,Date startTime,Date endTime){
        return dataDao.countDayDataBy(botUserId,startTime,endTime);
    }

    public List<PlayerBuyRecord> getPlayerByRecordSumByBotUserId(String botUserId, Date startTime, Date endTime){
        return dataDao.getPlayerByRecordSumByBotUserId(botUserId,startTime,endTime);
    }

    public PlayerBuyRecord getPlayerByRecordSumByPlayerId(String playerId,Date startTime,Date endTime){
        return dataDao.getPlayerByRecordSumByPlayerId(playerId,startTime,endTime);
    }

    /**
     * 根据期号获取报网金额
     * @param drawId
     * @return
     */
    public BigDecimal getBaowangCountByDrawId(String drawId,String botUserId){
        return dataDao.getBaowangCountByDrawId(drawId,botUserId);
    }

    public BigDecimal getBaowangCountByDrawId(String drawId,String botUserId,Integer lotteryType){
        return dataDao.getBaowangCountByDrawId1(drawId,botUserId,lotteryType);
    }


    /**
     *
     * @param playerBuyId 下注记录ID
     * @param userId 用户账号
     * @return
     */
    public String tuima(String playerBuyId,String userId){

        BotUser botUser = botUserService.getById(userId);
        PlayerBuyRecord existOne = dataDao.selectById(playerBuyId);
        if(null == existOne){
            return"参数错误";
        }
        Player player = playerService.getById(existOne.getPlayerId());
        if(null == player){
            return"参数错误";
        }

        Draw draw = null;
        //PlayerBuyRecord record = dataDao.selectById(playerBuyId);
        if(null == existOne){
            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser,player,"退米失败:订单不存在");
            chatRoomMsgService.save(toMsg);
            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
            //simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                //wechatIpadTokenService.sendMsgToFriend(toMsg);
                if (player.getChatStatus() == 1){
                    wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(),toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                }else{
                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(),toMsg.getMsg());
                }
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
            }
            return "退米失败：订单不存在";
        }
        if(existOne.getBuyStatus()==-1){
            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser,player,"订单【"+existOne.getBuyDesc()+"】已退米");
            chatRoomMsgService.save(toMsg);
            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg",JSON.toJSONString(toMsg));
            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                //wechatIpadTokenService.sendMsgToFriend(toMsg);
                if (player.getChatStatus() == 1){
                    wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(),toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                }else{
                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(),toMsg.getMsg());
                }
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
            }
            return "退米失败：订单【"+existOne.getBuyDesc()+"】已退米";
        }
        Map<String,Object> buyResult = null;
        int lotteryType = existOne.getLotteryType();
        String lotName = "";
        if(lotteryType==2){
            draw = p3DrawService.getLastDrawInfo();
            lotName = "P3";
        }else{
            draw = drawService.getLastDrawInfo();
            lotName = "3D";
        }
        if(draw.getOpenStatus()!=1){
            return "退米失败：已停止上课";
        }
        if(existOne.getDrawNo().intValue()!=draw.getDrawId().intValue()){
            return "退米失败：非当期订单";
        }
        if(existOne.getBuyType()==0){
            BotUserPan botUserPan = botUserPanService.getOneBy(lotteryType,botUser.getId());
            if(null==botUserPan || StringUtil.isNull(botUserPan.getLogin3dToken())){
                return "退米失败：未登录"+lotName+"盘口";
            }
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
            //属于报网订单
            ResponseBean reportRespData = reportToPanService.tuima(reportToPanUrl, Lists.newArrayList(playerBuyId),botUserPan.getLogin3dToken());
            ChatRoomMsg toMsg = null;
            switch (reportRespData.getCode()){
                case 403: //盘口登录token已失效
                    botUserPanService.clearInfo(lotteryType,botUser.getId());
                    toMsg = chatRoomMsgService.createMsg(botUser,player,"机器人未登录"+lotName+"网盘");
                    toMsg.setSource(0);
                    chatRoomMsgService.save(toMsg);
                    rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg",JSON.toJSONString(toMsg));
                    //dataDao.insert(toMsg);
                    //simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                    if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                        if (player.getChatStatus() == 1){
                            wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(),toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                        }else{
                            wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(),toMsg.getMsg());
                        }
//                        wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                    }
                    return "机器人未登录"+lotName+"网盘";
                    //break;
                case -1:
                case 500:
                    String errmsg = reportRespData.getMsg();
                    toMsg = chatRoomMsgService.createMsg(botUser,player,errmsg);
                    chatRoomMsgService.save(toMsg);
                    rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg",JSON.toJSONString(toMsg));
                    if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                        if (player.getChatStatus() == 1){
                            wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(),toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                        }else{
                            wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(),toMsg.getMsg());
                        }
//                        wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                    }
                    return "报网退米失败:"+errmsg;
                    //break;
                case 0:
                    //操作成功
                    buyResult = buyRecord3DService.tuima(player,playerBuyId,draw);
                    break;
            }

        }else{
            buyResult = buyRecord3DService.tuima(player,playerBuyId,draw);
        }
        if(null!=buyResult){
            Integer errcode = (Integer)buyResult.get("errcode");
            String errmsg = (String)buyResult.get("errmsg");
            if(-1==errcode){
                ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser,player,errmsg);
                chatRoomMsgService.save(toMsg);
                rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg",JSON.toJSONString(toMsg));
                if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                    //wechatIpadTokenService.sendMsgToFriend(toMsg);
                    if (player.getChatStatus() == 1){
                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(),toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                    }else{
                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(),toMsg.getMsg());
                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                }
                return "退米失败:"+errmsg;
            }else{
                BigDecimal points = playerService.getPoints(player.getId());
                BigDecimal backPoints = existOne.getBuyPoints();
                String newmsg = "["+existOne.getBuyDesc()+"]"+"\r\n[此单已退码]\r\n【退面款】："+backPoints.stripTrailingZeros().toPlainString()+"\r\n"+"【当前盛鱼】："+points.stripTrailingZeros().toPlainString()+"\r\n";
                ChatRoomMsg toMsg = new ChatRoomMsg();
                toMsg.setFromUserId(botUser.getId());
                toMsg.setFromUserNick("机器人");
                toMsg.setFromUserType(1);
                toMsg.setMsg(newmsg);
                toMsg.setToUserNick(player.getNickname());
                toMsg.setToUserId(player.getId());
                toMsg.setCreateTime(new Date());
                toMsg.setBotUserId(botUser.getId());
                toMsg.setMsgType(0);
                toMsg.setOptType(0);
                toMsg.setToUserType(0);
                chatRoomMsgService.save(toMsg);
                rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg",JSON.toJSONString(toMsg));
                if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                    //wechatIpadTokenService.sendMsgToFriend(toMsg);
                    if (player.getChatStatus() == 1){
                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(),toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                    }else{
                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(),toMsg.getMsg());
                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                }
            }
        }

        return null;

    }


}
