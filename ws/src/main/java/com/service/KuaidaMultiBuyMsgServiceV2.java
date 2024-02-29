package com.service;

import cn.hutool.core.lang.Snowflake;
import com.GlobalConst;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.beans.*;
import com.config.RequestDataHelper;
import com.dao.ChatRoomMsgDAO;
import com.dao.DrawBuyRecordDAO;
import com.google.common.collect.Maps;
import com.util.Code3DCreateUtils;
import com.util.StringUtil;
import com.vo.BuyRecord3DVO;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 快打下注，
 * P3、3D进行整合
 */
@Service
@Lazy
public class KuaidaMultiBuyMsgServiceV2 {


    @Autowired
    private ChatRoomMsgDAO dataDao;

    @Autowired
    private DrawBuyRecordDAO drawBuyRecordDAO;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    @Autowired
    private PlayerService playerService;


    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private BuyRecord3DServiceV2 buyRecord3DServiceV2;

    @Autowired
    private BotUserSettingService botUserSettingService;

    @Autowired
    private DrawService drawService;

    @Autowired
    private P3DrawService p3DrawService;

    @Autowired
    private ReportToPanService reportToPanService;

    @Autowired
    private WechatApiService wechatApiService;

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private BotUserPanService botUserPanService;

    @Autowired
    private LotterySettingService lotterySettingService;

    @Autowired
    private Snowflake snowflake;

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
        return toMsg;
    }

    //快打下注，多组
    public void kuaidaBuyForMultiGroup(ChatRoomMsg fromMsg, BotUser botUser, Player player,Integer lotteryType){
        try {
            Draw draw = null;
            ChatRoomMsg toMsg = null;
            String lotteryName = "3D";
            if(lotteryType == 2){
                lotteryName = "P3";
                draw = p3DrawService.getLastDrawInfo();
            }else{
                lotteryName = "3D";
                draw = drawService.getLastDrawInfo();
            }
            if(draw.getOpenStatus()!=1){
                toMsg = createMsg(botUser, player, "【"+lotteryName+"】^^★★★停止-上课★★★");
                toMsg.setSource(1);
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                    //wechatIpadTokenService.sendMsgToFriend(toMsg);
                    if (player.getChatStatus() == 1){
                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                    }else{
                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                }
                return;
            }
            List<BuyRecord3DVO> buyList = JSONArray.parseArray(fromMsg.getKuaixuanRule(),BuyRecord3DVO.class);

            BigDecimal totalPoints = buyList.stream().map(item->item.getBuyMoney().multiply(new BigDecimal(item.getBuyAmount()))).reduce(BigDecimal.ZERO,BigDecimal::add);
            if(totalPoints.compareTo(player.getPoints())>0){
                //玩家积分不够
                toMsg = createMsg(botUser,player,"面上不足");
                toMsg.setSource(0);
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                    if (player.getChatStatus() == 1){
                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                    }else{
                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                }
                return;
            }

            if (null != buyList && buyList.size()>0) {
                xiazhu(botUser, player, fromMsg, buyList,lotteryType,draw);
            }else{
                toMsg = createMsg(botUser, player, "未指定号码");
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                    if (player.getChatStatus() == 1){
                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                    }else{
                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            ChatRoomMsg toMsg = createMsg(botUser, player, "系统繁忙，请稍后重试");
            toMsg.setSource(0);
            dataDao.insert(toMsg);
            simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
            if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                if (player.getChatStatus() == 1){
                    wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                }else{
                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                }
//                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
            }
        }
    }


    public void reportToPan(BotUser botUser,Player player,ChatRoomMsg msg,List<BuyRecord3DVO> buyList,
                            BotUserPan botUserPan,Draw draw,Integer lotteryType,String lotteryName,String buyDesc){
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
        BotUserSetting botUserSetting = botUserSettingService.getByUserId(botUser.getId());

        String playerBuyId = snowflake.nextIdStr();
        PlayerBuyRecord playerBuyRecord = new PlayerBuyRecord();
        playerBuyRecord.setId(playerBuyId);
        playerBuyRecord.setPlayerId(player.getId());
        playerBuyRecord.setBuyType(0);
        playerBuyRecord.setBotUserId(player.getBotUserId());
        playerBuyRecord.setLotteryType(lotteryType);

        String lmId = buyList.get(0).getLmId();
        Map<String,Object> buyResult = null;
        ResponseBean reportRespData = null;
        buyList.forEach(item->{
            item.setPlayerBuyId(playerBuyId);
        });
//        System.out.println("========="+lotteryName+"报网====》"+JSON.toJSONString(buyList));
        if("5".equals(lmId) || "13".equals(lmId)){
            reportRespData = reportToPanService.buyHs(reportToPanUrl,buyList,botUserPan.getLogin3dToken());
        }else{
            reportRespData = reportToPanService.buy(reportToPanUrl,buyList,botUserPan.getLogin3dToken());
        }
        ChatRoomMsg toMsg = null;
        switch (reportRespData.getCode()){
            case 403: //盘口登录token已失效
                botUserPanService.clearPanInfo(botUser.getId(),lotteryType);
                toMsg = createMsg(botUser,player,"机器人未登录"+lotteryName+"网盘");
                toMsg.setSource(0);
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                    if (player.getChatStatus() == 1){
                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                    }else{
                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                }
                break;
            case -1:
            case 500:
                String errmsg = reportRespData.getMsg();
                toMsg = createMsg(botUser,player,errmsg);
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                    if (player.getChatStatus() == 1){
                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                    }else{
                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                }
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
                    playerBuyRecord.setBuyDesc(buyDesc);
                    playerBuyRecord.setBuyFrom(0);
                    playerBuyRecord.setKuaixuanRule(JSON.toJSONString(buyList));
                    playerBuyRecord.setEarnPoints(BigDecimal.ZERO.subtract(totalPoints));
                    dataList.forEach(item->{
                        item.setBaopaiId(playerBuyRecord.getId());
                        item.setVipId(player.getId());
                        item.setBuyType(0);
                        item.setHasOneFlag(lotteryType);
                    });
                    Map dynamicPrama = new HashMap();
                    dynamicPrama.put("qihao", String.valueOf(draw.getDrawId()));
                    RequestDataHelper.setRequestData(dynamicPrama);

                    String cmdTxt = buyList.stream().map(item->item.getCmd()).collect(Collectors.joining("\n"));

                    if(drawBuyRecordDAO.batchAddBuyCode(dataList)>0){
                        playerBuyRecordService.save(playerBuyRecord);
                        playerService.updatePoint(player.getId(),totalPoints,false);
                        BigDecimal points = playerService.getPoints(player.getId());
                        String newmsg = "["+lotteryName+"课号]"+draw.getDrawId()+"\r\n"+cmdTxt+"\r\n交作业成功√√\r\n【份数】："+buyAmount+"\r\n"
                                +"【扣面】："+totalPoints.stripTrailingZeros().toPlainString()+"\r\n"+"【盛鱼】："+points.stripTrailingZeros().toPlainString();

                        toMsg = new ChatRoomMsg();
                        toMsg.setFromUserId(botUser.getId());
                        toMsg.setFromUserNick("机器人");
                        toMsg.setFromUserType(1);
                        toMsg.setMsg(newmsg);
                        toMsg.setToUserNick(player.getNickname());
                        toMsg.setToUserId(player.getId());
                        toMsg.setCreateTime(new Date());
                        toMsg.setToUserType(0);
                        toMsg.setBotUserId(botUser.getId());
                        if(botUserSetting.getTuimaEnable()==1){//开启了退码
                            toMsg.setPlayerBuyId(playerBuyRecord.getId());
                            toMsg.setOptType(3);
                        }else{//未开启退码
                            toMsg.setPlayerBuyId(null);
                            toMsg.setOptType(1);
                        }
                        toMsg.setMsgType(0);
                        dataDao.insert(toMsg);
                        simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                        if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                            //wechatIpadTokenService.sendMsgToFriend(toMsg);
                            if (player.getChatStatus() == 1){
                                wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                            }else{
                                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                            }
//                            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                        }
                    }
                }else{
                    JSONArray array1 = (JSONArray)dataMap.get("stopCodeList");
                    if(null!=array1 && array1.size()>0){
                        String newmsg = "["+lotteryName+"课号]"+draw.getDrawId()+"\r\n"+msg.getMsg()+"\r\n交作业成功√√\r\n【份数】："+0+"\r\n"
                                +"【扣面】："+0+"\r\n"+"【停押】："+array1.size();
                        toMsg = createMsg(botUser,player,newmsg);
                        toMsg.setSource(0);
                        dataDao.insert(toMsg);
                        simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                        if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                            if (player.getChatStatus() == 1){
                                wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                            }else{
                                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                            }
//                            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                        }
                    }
                }
                break;
        }

    }


    private void xiazhu(BotUser botUser, Player player,ChatRoomMsg fromMsg,
                        List<BuyRecord3DVO> buyList,
                        Integer lotteryType,Draw draw) {
        BigDecimal totalBuyPoints = buyList.stream().map(item->item.getBuyMoney().multiply(new BigDecimal(item.getBuyAmount()))).reduce(BigDecimal.ZERO,BigDecimal::add);
        if(totalBuyPoints.compareTo(player.getPoints())>0){
            //玩家积分不够
            ChatRoomMsg toMsg = createMsg(botUser,player,"面上不足");
            toMsg.setSource(0);
            dataDao.insert(toMsg);
            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                if (player.getChatStatus() == 1){
                    wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                }else{
                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                }
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
            }
            return;
        }
        String lotteryName = "3D";
        if(lotteryType == 2){
            lotteryName = "P3";
        }else{
            lotteryName = "3D";
        }

        String msg = fromMsg.getMsg();
        for(BuyRecord3DVO one : buyList){
            String lmId = one.getLmId();
            List<BuyRecord3DVO> dlist = new ArrayList<>();
            dlist.add(one);
            if(player.getPretexting()==1 || player.getEatPrize()==1){
                BotUserSetting botUserSetting = botUserSettingService.getByUserId(botUser.getId());
                Map<String,Object> buyResult = null;
                if("5".equals(lmId) || "13".equals(lmId)){
                    buyResult = buyRecord3DServiceV2.buy3dHs(player,dlist,draw,-2,null,one.getBuyDesc(),lotteryType);
                }else{
                    buyResult = buyRecord3DServiceV2.codesBatchBuy(player,dlist,draw,-2,null,one.getBuyDesc(),lotteryType);
                }
                if(buyResult.containsKey("playerBuyId")){
                    String playerBuyId = (String)buyResult.get("playerBuyId");
                    PlayerBuyRecord record = playerBuyRecordService.getById(playerBuyId);
                    BigDecimal points = playerService.getPoints(player.getId());
                    String newmsg = "["+lotteryName+"课号]"+draw.getDrawId()+"\r\n"+one.getCmd()+"\r\n交作业成功√√\r\n【份数】："+record.getBuyAmount()+"\r\n"
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
                    toMsg.setBotUserId(botUser.getId());
                    if(botUserSetting.getTuimaEnable()==1){
                        toMsg.setPlayerBuyId(playerBuyId);
                        toMsg.setOptType(3);
                    }else{
                        toMsg.setPlayerBuyId(null);
                        toMsg.setOptType(1);
                    }
                    toMsg.setMsgType(0);
                    toMsg.setSource(0);
                    dataDao.insert(toMsg);
                    simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                    if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                        if (player.getChatStatus() == 1){
                            wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                        }else{
                            wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                        }
//                        wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                    }
                }else{
                    String errmsg = (String)buyResult.get("errmsg");
                    if(StringUtil.isNotNull(errmsg)){
                        ChatRoomMsg toMsg = createMsg(botUser,player,errmsg);
                        toMsg.setSource(0);
                        dataDao.insert(toMsg);
                        simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                        if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                            if (player.getChatStatus() == 1){
                                wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                            }else{
                                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                            }
//                            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                        }
                    }
                }
            }else{
                if(player.getReportNet()==1){
                    BotUserPan botUserPan = botUserPanService.getOneBy(botUser.getId(),lotteryType);
                    if(null!=botUserPan && StringUtil.isNotNull(botUserPan.getLogin3dToken())){
                        //报网
                        reportToPan(botUser,player,fromMsg,dlist,botUserPan,draw,lotteryType,lotteryName,one.getBuyDesc());
                    }else{
                        ChatRoomMsg toMsg = createMsg(botUser,player,"机器人未登录"+lotteryName+"网盘");
                        toMsg.setSource(0);
                        dataDao.insert(toMsg);
                        simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                        if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                            if (player.getChatStatus() == 1){
                                wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                            }else{
                                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                            }
//                            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                        }
                    }
                }
            }
        }


    }


}
