package com.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.GlobalConst;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.exception.LockException;
import com.beans.*;
import com.mysql.jdbc.StringUtils;
import com.util.Code3DCreateUtils;
import com.util.StringUtil;
import com.util.Tools;
import com.vo.BuyRecord3DVO;
import com.vo.WechatApiMsgVo;
import com.vo.WechatPushMsgVo;
import com.wechat.api.RespData;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 账号服务类
 */
@Service
public class WechatApiService{

    @Value("${wechat.api.url}")
    private String wechatApiUrl;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private BotUserSettingService botUserSettingService;

    @Autowired
    private ChatRoomMsgService chatRoomMsgService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PlayerPointsRecordService playerPointsRecordService;

    @Autowired
    private DrawService drawService;

    @Autowired
    private P3DrawService p3DrawService;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    @Autowired
    private ChatDomainService chatDomainService;

    @Autowired
    private KuaidaBuyMsgServiceV2 kuaidaBuyMsgServiceV2;

    @Autowired
    private PlayerReturnPointsService playerReturnPointsService;

    @Autowired
    private LockTemplate lockTemplate;

    @Autowired
    private WechatMsgService wechatMsgService;


    private Logger logger = LoggerFactory.getLogger(WechatApiService.class);



    public RespData receiveMsg(BotUser user,Integer Scene,String syncKey) {
        String wxId = user.getWxId();
        String syncMsgUrl =  wechatApiUrl+"Msg/Sync";
        HttpRequest request = HttpUtil.createPost(syncMsgUrl);
        request.contentType("application/json");
        Map<String,Object> reqData = new HashMap<>();
        reqData.put("Scene",Scene);
        reqData.put("Synckey",syncKey);
        reqData.put("Wxid",wxId);
        request.body(JSON.toJSONString(reqData));
        HttpResponse httpResponse = request.execute();
        String result = httpResponse.body().trim();
        if(result.startsWith("{") && result.endsWith("}")){
            RespData respData = JSONObject.parseObject(result, RespData.class);
            return respData;
        }
        return null;

    }


    //检查重复消息
    public boolean checkRepeatMsg(String msgId,String text,String fromUserName,String toUserName){
        WechatMsg wechatMsg = new WechatMsg();
        wechatMsg.setMsgId(msgId);
        wechatMsg.setFromUser(fromUserName);
        wechatMsg.setToUser(toUserName);
        wechatMsg.setContent(text);
        wechatMsg.setReceiveTime(new Date());
        boolean msgIdExist = wechatMsgService.checkExist(msgId);
        if(msgIdExist){
            logger.info(String.format("%s>>>>收到重复的微信消息ID>>>>>%s>>>>>%s",toUserName,msgId,text));
            //wechatMsgService.save(wechatMsg);
            return true;
        }else{
            wechatMsgService.save(wechatMsg);
            return false;
        }
    }


    //接受消息
    public void receiveMsg(BotUser user){
        String lockKey = "3dbot:wx:syncmsg:"+user.getId();
        final LockInfo lockInfo = lockTemplate.lock(lockKey,60000,30000);
        if(null!=lockKey){
            try {
                String syncKey = "";
                Integer Scene = 7;
                String wxId = user.getWxId();
                List<String> excludeWxId = Arrays.asList("weixin", wxId);
                if (!GlobalConst.msgBuffer.containsKey(wxId)) {
                    RespData respData = receiveMsg(user, Scene, syncKey);
                    if(null == respData){
                        return;
                    }
                    if (respData.getCode() == 0) {
                        Map<String, Object> datas = respData.getData();
                        if (datas.containsKey("KeyBuf")) {
                            JSONObject obj = (JSONObject) datas.get("KeyBuf");
                            String buffer = (String) obj.get("buffer");
                            GlobalConst.msgBuffer.put(wxId, buffer);
                        }
                    }
                }


                if (GlobalConst.msgBuffer.containsKey(wxId)) {
                    syncKey = GlobalConst.msgBuffer.get(wxId);
                    Scene = 1;
                    RespData respData = receiveMsg(user, Scene, syncKey);
                    if(null == respData){
                        return;
                    }
                    if (respData.getCode() == 0) {
                        Map<String, Object> datas = respData.getData();
                        if (datas.containsKey("AddMsgs")) {
                            JSONArray arr = (JSONArray) datas.get("AddMsgs");
                            if (null != arr) {
                                for (Object obj : arr) {

                                    try {
                                        WechatApiMsgVo oneMsg = JSONObject.parseObject(JSONObject.toJSONString(obj), WechatApiMsgVo.class);
                                        Map<String, String> fromUser = oneMsg.getFromUserName();
                                        String fromUserName = fromUser.get("string");
                                        Map<String, String> toUser = oneMsg.getToUserName();
                                        String toUserName = toUser.get("string");
                                        Map<String, String> content = oneMsg.getContent();
                                        String text = content.get("string");

                                        String[] multiArr = text.split("\n");
                                        String buyDesc = Arrays.stream(multiArr).collect(Collectors.joining(","));
                                        //检查消息是否有重复，如果重复，则直接返回
                                        boolean checkResult = checkRepeatMsg(String.valueOf(oneMsg.getMsgId()),buyDesc,fromUserName,toUserName);
                                        if(checkResult){
                                            return;
                                        }

//                                        if (text.toUpperCase().startsWith("3D") || text.toUpperCase().startsWith("P3")) {
//                                            logger.info(String.format(">>>>收到微信消息1>>>>>%s>>>>>%s", oneMsg.getMsgId(),text));
//                                            String[] multiArr = text.split("\n");
//                                            String buyDesc = Arrays.stream(multiArr).collect(Collectors.joining(","));
//                                            WechatMsg wechatMsg = new WechatMsg();
//                                            wechatMsg.setMsgId(String.valueOf(oneMsg.getMsgId()));
//                                            wechatMsg.setFromUser(fromUserName);
//                                            wechatMsg.setToUser(toUserName);
//                                            wechatMsg.setContent(buyDesc);
//                                            wechatMsg.setReceiveTime(new Date());
//                                            boolean msgIdExist = wechatMsgService.checkExist(String.valueOf(oneMsg.getMsgId()));
//                                            if(msgIdExist){
//                                                logger.info(String.format(">>>>重复的微信消息id>>>>>%s>>>>>%s", oneMsg.getMsgId(),buyDesc));
//                                                wechatMsgService.save(wechatMsg);
//                                                return;
//                                            }else{
//                                                wechatMsgService.save(wechatMsg);
//                                            }
//                                        }
                                        if (toUserName.equals(wxId) && !excludeWxId.contains(fromUserName)) {
//                                            if (text.contains(":\n")){
//                                                String[] msgArr = text.split(":\n");
//                                                String fromUserNameTwo = msgArr[0];
//                                                String textTwo = msgArr[1];
//                                                addNewPlayerGroup(textTwo,user,fromUserNameTwo,wxId,fromUserName);
//                                            }else{
                                                addNewPlayer(text,user,fromUserName,wxId);
//                                            }
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }finally {
                                        if(null!=lockInfo){
                                            lockTemplate.releaseLock(lockInfo);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                lockTemplate.releaseLock(lockInfo);
            }
        }
    }

    public void addNewPlayerGroup(String text,BotUser user,String fromUserName,String wxId,String groupName){
        String txt = text.toUpperCase();
        if (txt.equals("3D") || txt.equals("P3") || txt.equals("福") || txt.equals("体")) {
            int lottype = -1;
            if(txt.equals("3D")  || txt.equals("福")){
                lottype = 1;
            }else{
                lottype = 2;
            }
            if(lottype>0){
                BotUserSetting botUserSetting = botUserSettingService.getByUserId(user.getId());
                Player player = playerService.getOneBy(user.getId(), fromUserName);
                if (null == player) {
                    String chatUrl = "";
                    ChatDomain chatDomain = chatDomainService.getOneBy(); //获取可用的域名
                    if (null != chatDomain) {
                        chatUrl = chatDomain.getUrl();
                    }
                    if(StringUtils.isNullOrEmpty(chatUrl)){
                        String tips = "域名资源不足，请联系老师";
                        chatRoomMsgService.sendMsgGroup(fromUserName, wxId, tips, groupName);
                        return;
                    }
                    player = new Player();
                    player.setBotUserId(user.getId());
                    player.setWxFriendId(fromUserName);
                    player.setNickname(fromUserName);
                    String openId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
                    player.setOpenid(openId);
                    player.setUserType(2);
                    player.setChaturl(chatUrl);
                    player.setHsvalue(botUserSetting.getHsvalue());
                    player.setHsType(0);

                    if(user.getLotteryType()==3 || user.getLotteryType() == lottype){
                        player.setLotteryType(lottype);
                        playerService.save(player);
                    }else{
                        String tips = "机器人未开通"+txt+"服务";
                        chatRoomMsgService.sendMsgGroup(fromUserName, wxId, tips, groupName);
                        return;
                    }
                }else{
                    //System.out.println("机器人【" + user.getLoginName() + "】===========玩家[" + player.getNickname() + "]已存在");
                    if(user.getLotteryType()==3){
                        if(player.getLotteryType()!=lottype){
                            if(player.getLotteryType()!=3){
                                player.setLotteryType(3);
                                playerService.updateById(player);
                            }
                        }
                    }else{
                        if(user.getLotteryType()!=lottype){
                            String tips = "机器人未开通"+txt+"服务";
                            chatRoomMsgService.sendMsgGroup(fromUserName, wxId, tips, groupName);
                            return;
                        }else{
                            player.setLotteryType(user.getLotteryType());
                            playerService.updateById(player);
                        }
                    }
                }
                String chatUrl = player.getChaturl();
                if (chatUrl.contains("&")) {
                    chatUrl = player.getChaturl() + "&openId=" + player.getOpenid();
                } else {
                    chatUrl = player.getChaturl() + "?openId=" + player.getOpenid();
                }
                //String chatUrl = player.getChaturl()+"/?openId="+player.getOpenid();
                String buffer = "ᵕ̈ ᴹᴼᴿᴺᴵᴺᴳ 🌼 ᵕ̈ 🌼\n人生的路虽然难走但是没有绝境\n☛只要寻找总有路可走\n" + chatUrl;
                chatRoomMsgService.sendMsgGroup(fromUserName, wxId, buffer, groupName);
                if (StringUtil.isNull(player.getHeadimg())) {
                    //头像为空；
                    getFriendInfo(fromUserName, wxId, player);
                }
            }
        } else {
            Player player = playerService.getOneBy(user.getId(), fromUserName);
            if (null != player) {
                //handleMsg(text, user, player);
//                handleMultiMsg(text,user,player);
                handleMultiMsgGroup(text,user,player,groupName);
            }
        }
    }

    /**
     * 处理多组下注
     * @param text
     * @param botUser
     * @param player
     */
    public void handleMultiMsgGroup(String text,BotUser botUser, Player player,String groupName){
        boolean isCommonCmd = false;
        for(String cmd : GlobalConst.commonCmd){
            if(text.startsWith(cmd)){
                isCommonCmd = true;
                break;
            }
        }
        if(isCommonCmd){
//            logger.info(String.format("收到微信消息>>>>>>>>>>toUser===%s,fromUser===%s,text===%s", botUser.getLoginName(), player.getNickname(), text));
            handleCommonMsgGroup(botUser,text,player,groupName);
        }else{
            Boolean checkTxtResult = true;
            String[] multiArr = text.split("\n");
            //System.out.println("=====multi group wx=="+ Arrays.stream(multiArr).collect(Collectors.joining(",")));
            for(String cmdText : multiArr){
                if(cmdText.toUpperCase().startsWith("P3")
                        || cmdText.toUpperCase().startsWith("3D") || cmdText.toUpperCase().startsWith("福")
                        || cmdText.toUpperCase().startsWith("体")){
                    if(cmdText.toUpperCase().startsWith("P3") || cmdText.toUpperCase().startsWith("体")){
                        Draw draw =  p3DrawService.getLastDrawInfo();
                        if(null==draw || draw.getOpenStatus()!=1){
                            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player,"【P3】^^★★★停止-上课★★★");
                            chatRoomMsgService.saveAndSendMsgGroup(toMsg,player.getWxFriendId(),botUser.getWxId(),groupName);
                            return;
                        }
                    }else{
                        Draw draw =  drawService.getLastDrawInfo();
                        if(null==draw || draw.getOpenStatus()!=1){
                            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player,"【3D】^^★★★停止-上课★★★");
                            chatRoomMsgService.saveAndSendMsgGroup(toMsg,player.getWxFriendId(),botUser.getWxId(),groupName);
                            return;
                        }
                    }
                }else{
                    checkTxtResult = false;
                    break;
                }
            }

            if(checkTxtResult){
                logger.info(String.format("收到微信消息3>>>>>>>>>>toUser===%s,fromUser===%s,text===%s", botUser.getLoginName(), player.getNickname(), text));
                ChatRoomMsg fromMsg = chatRoomMsgService.createFromWxMsg(botUser,player,text);
                chatRoomMsgService.save(fromMsg);
                rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(fromMsg));

                for(String cmdText : multiArr){
                    String text1 = cmdText.toUpperCase();
                    if(text1.startsWith("P3") || text1.startsWith("3D") || text1.startsWith("福") || text1.startsWith("体")){
                        ChatRoomMsg childMsg = chatRoomMsgService.createFromWxMsg(botUser,player,text1);
                        int lottype = 1;
                        if(text1.startsWith("P3") || text1.startsWith("体")){
                            lottype = 2;
                        }
                        String txt = "";
                        if(text1.startsWith("P3") || text1.startsWith("3D")){
                            txt = text1.substring(2);
                        }else{
                            txt = text1.substring(1);
                        }

                        //int lottype = text1.startsWith("P3")?2:1;
                        String lotName = lottype==2?"P3":"3D";
                        if(player.getLotteryType()==3 || player.getLotteryType()==lottype){
                            //String txt = text1.substring(2);
                            boolean isBuy = false;
                            for(String word : GlobalConst.keywords2){
                                if(txt.startsWith(word)){
                                    kuaidaBuyMsgServiceV2.handleMsgGroup(childMsg,botUser,player,lottype,groupName);
                                    isBuy = true;
                                    break;
                                }
                            }
                            if(!isBuy){
                                ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player,"作业格式有误:"+cmdText);
                                chatRoomMsgService.saveAndSendMsgGroup(toMsg,player.getWxFriendId(),botUser.getWxId(),groupName);
                            }
                        }else{
                            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player,"哦噢，您无提交"+lotName+"作业的权限");
                            chatRoomMsgService.saveAndSendMsgGroup(toMsg,player.getWxFriendId(),botUser.getWxId(),groupName);
                        }
                    }
                }
            }
        }
    }

    public void handleCommonMsgGroup(BotUser botUser, String text,Player player,String groupName){
        BotUserSetting botUserSetting = botUserSettingService.getByUserId(botUser.getId());
        Draw draw = drawService.getLastDrawInfo();

        Draw p3Draw = p3DrawService.getLastDrawInfo();

        List<Integer> drawNoList = Lists.newArrayList();
        if(null!=draw){
            drawNoList.add(draw.getDrawId());
        }
        if(null!=p3Draw){
            drawNoList.add(p3Draw.getDrawId());
        }

        ChatRoomMsg fromMsg = chatRoomMsgService.createFromWxMsg(botUser,player,text);
        chatRoomMsgService.save(fromMsg);
        rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(fromMsg));

        if (text.startsWith("上分") || text.startsWith("下分")) {
            upDownPointsGroup(fromMsg, botUser, player,groupName);
            return;
        }

        if (text.startsWith("作业格式")) {
            buyFormatGroup(fromMsg,botUser,player,groupName);
            return;
        }

        if (text.startsWith("流水")) {
            BigDecimal totalBuyPoints = BigDecimal.ZERO;
            //返回当期流水
            BigDecimal p1 = playerBuyRecordService.sumTotalBuy(player.getId(),draw.getDrawId(),1);
            if(null !=p1){
                totalBuyPoints = totalBuyPoints.add(p1);
            }
            BigDecimal p2 = playerBuyRecordService.sumTotalBuy(player.getId(),p3Draw.getDrawId(),2);
            if(null !=p2){
                totalBuyPoints = totalBuyPoints.add(p2);
            }
            String msg = "【累计流水】" + totalBuyPoints.stripTrailingZeros().toPlainString();
            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player, msg);
            chatRoomMsgService.saveAndSendMsgGroup(toMsg,player.getWxFriendId(),botUser.getWxId(),groupName);
            return;
        }

        if (text.startsWith("盈亏")) {
            BigDecimal totalEarnPoints = BigDecimal.ZERO;
            //返回当期盈亏
            BigDecimal p1 = playerBuyRecordService.sumYKBuy(player.getId(),draw.getDrawId(),1);
            if(null !=p1){
                totalEarnPoints = totalEarnPoints.add(p1);
            }
            BigDecimal p2 = playerBuyRecordService.sumYKBuy(player.getId(),p3Draw.getDrawId(),2);
            if(null !=p2){
                totalEarnPoints = totalEarnPoints.add(p2);
            }
            String msg = "YK：" + totalEarnPoints.stripTrailingZeros().toPlainString();
            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player, msg);
            chatRoomMsgService.saveAndSendMsgGroup(toMsg,player.getWxFriendId(),botUser.getWxId(),groupName);
            return;
        }

        if (text.startsWith("盛鱼")) {
            String msg = "【当前盛鱼】" +player.getPoints().stripTrailingZeros().toPlainString();
            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player, msg);
            chatRoomMsgService.saveAndSendMsgGroup(toMsg,player.getWxFriendId(),botUser.getWxId(),groupName);
            return;
        }

        if (text.startsWith("查")) {
            List<PlayerBuyRecord> allList = Lists.newArrayList();
            //查询当期下注记录
            List<PlayerBuyRecord> buyList = playerBuyRecordService.getListBy(player.getId(),draw.getDrawId(),1);
            List<PlayerBuyRecord> buyList2 = playerBuyRecordService.getListBy(player.getId(),p3Draw.getDrawId(),2);
            if(null!=buyList && buyList.size()>0) {
                allList.addAll(buyList);
            }
            if(null!=buyList2 && buyList2.size()>0) {
                allList.addAll(buyList2);
            }
            String msg = null;
            if(allList.size()>0) {
                StringBuffer sb = new StringBuffer();
                for (PlayerBuyRecord playerBuyRecord : allList) {
                    sb.append(playerBuyRecord.getBuyDesc().replace("\r\n","")).append("\r\n");
                }
                msg = "【当前作业】\r\n" + sb.toString() + "【状态】交作业成功√√\r\n"
                        + "【当前盛鱼】" + player.getPoints().stripTrailingZeros().toPlainString();
            }else{
                msg = "【当前作业】未提交任何作业哦\r\n" + "【当前盛鱼】" + player.getPoints().stripTrailingZeros().toPlainString();

            }
            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player, msg);
            chatRoomMsgService.saveAndSendMsgGroup(toMsg,player.getWxFriendId(),botUser.getWxId(),groupName);
            return;
        }


        if (text.startsWith("返")) {
            //机器人开启了玩家自助返水
            if(null!=botUserSetting && null!=botUserSetting.getHsHelpBack() && botUserSetting.getHsHelpBack()==1){

                switch (player.getLotteryType()){
                    case 3:
                        backWaterMsgGroup(botUser,player,1,groupName);
                        backWaterMsgGroup(botUser,player,2,groupName);
                        break;
                    case 2:
                        backWaterMsgGroup(botUser,player,2,groupName);
                        break;
                    case 1:
                        backWaterMsgGroup(botUser,player,1,groupName);
                        break;
                    default:
                        break;
                }
            }else{
                ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player, "回水功能未开启");
                chatRoomMsgService.saveAndSendMsgGroup(toMsg,player.getWxFriendId(),botUser.getWxId(),groupName);
            }
        }
    }

    public void upDownPointsGroup(ChatRoomMsg fromMsg, BotUser botUser, Player player, String groupName){
        try {
            String content = fromMsg.getMsg();

            String points = "";
            Integer optType = null;
            if (content.contains("上分")) {
                optType = 0;
                //读取上分值
            } else if (content.contains("下分")) {
                optType = 1;
            }
            points = Tools.substractDigit(content);
            if (StringUtil.isNull(points)) {
                ChatRoomMsg toMsg = chatRoomMsgService.getErrorMsg(botUser, player);
                chatRoomMsgService.saveAndSendMsgGroup(toMsg,player.getWxFriendId(),botUser.getWxId(),groupName);
            } else {
                if (optType == 1) {
                    if (player.getPoints().compareTo(new BigDecimal(points)) < 0) {
                        //可用积分小于下分值
                        ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player, "面上不足");
                        chatRoomMsgService.saveAndSendMsgGroup(toMsg,player.getWxFriendId(),botUser.getWxId(),groupName);
                        return;
                    }
                }

                PlayerPointsRecord pointsRecord = new PlayerPointsRecord();
                pointsRecord.setPoints(new BigDecimal(points));
                pointsRecord.setAuthStatus(0);
                pointsRecord.setApplyTime(new Date());
                pointsRecord.setOptType(optType);
                pointsRecord.setPlayerId(player.getId());
                pointsRecord.setBotUserId(botUser.getId());
                playerPointsRecordService.save(pointsRecord);
                ChatRoomMsg toMsg = new ChatRoomMsg();
                toMsg.setFromUserId(botUser.getId());
                toMsg.setFromUserNick("机器人");
                toMsg.setFromUserType(1);
                toMsg.setMsg("发送成功，请联系老师");
                toMsg.setToUserNick(player.getNickname());
                toMsg.setToUserId(player.getId());
                toMsg.setCreateTime(new Date());
                toMsg.setBotUserId(botUser.getId());
                toMsg.setToUserType(0);
                toMsg.setMsgType(0);
                toMsg.setOptType(0);
                toMsg.setSource(1);
                chatRoomMsgService.saveAndSendMsgGroup(toMsg,player.getWxFriendId(),botUser.getWxId(),groupName);
            }
        }catch (Exception e){
            e.printStackTrace();
            ChatRoomMsg toMsg = new ChatRoomMsg();
            toMsg.setFromUserId(botUser.getId());
            toMsg.setFromUserNick("机器人");
            toMsg.setFromUserType(1);
            toMsg.setMsg("系统繁忙，请稍后重试");
            toMsg.setToUserNick(player.getNickname());
            toMsg.setToUserId(player.getId());
            toMsg.setCreateTime(new Date());
            toMsg.setBotUserId(botUser.getId());
            toMsg.setToUserType(0);
            toMsg.setMsgType(0);
            toMsg.setOptType(0);
            toMsg.setSource(1);
            chatRoomMsgService.saveAndSendMsgGroup(toMsg,player.getWxFriendId(),botUser.getWxId(),groupName);
        }
    }

    public void buyFormatGroup(ChatRoomMsg fromMsg, BotUser botUser, Player player, String groupName){
        String url = player.getChaturl();
        if(url.endsWith("/")){
            url = player.getChaturl()+"sendFormat.html";
        }else{
            url = player.getChaturl()+"/sendFormat.html";
        }
        String msg = "点此查看\r\n"+url;

        ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser,player,msg);
        toMsg.setSource(1);
        chatRoomMsgService.saveAndSendMsgGroup(toMsg,player.getWxFriendId(),botUser.getWxId(),groupName);
    }

    //返水消息
    public void backWaterMsgGroup(BotUser botUser,Player player,Integer lotteryType,String groupName){
        String lotteryName = "";
        Draw draw = null;
        if(lotteryType==1){
            draw = drawService.getLastDrawInfo();
            lotteryName = "3D";
        }else if(lotteryType==2){
            draw = p3DrawService.getLastDrawInfo();
            lotteryName = "P3";
        }
        if(null!=draw){
            BigDecimal totalBuyPoints = BigDecimal.ZERO; //总投
            BigDecimal totalValidPoints = BigDecimal.ZERO; //有效流水
            BigDecimal validHs = BigDecimal.ZERO; //回水金额
            BigDecimal playerTotalPoints = player.getPoints();
            BigDecimal p1 = playerBuyRecordService.sumTotalBuy(player.getId(),draw.getDrawId(),lotteryType);
            if(null !=p1){
                totalBuyPoints = totalBuyPoints.add(p1);
            }
            p1 = playerBuyRecordService.sumValidBuy(player.getId(),draw.getDrawId(),lotteryType);
            if(null !=p1){
                totalValidPoints = totalValidPoints.add(p1);
            }
            PlayerReturnPoints exist = playerReturnPointsService.getOneBy(player.getId(),draw.getDrawId(),lotteryType);
            if(null!=exist){
                validHs = validHs.add(exist.getReturnPoints());
                if(exist.getStatus()==0){
                    exist.setStatus(1);
                    exist.setReturnTime(new Date());
                    exist.setReturnType(1);
                    if(playerReturnPointsService.updateById(exist)){
                        playerService.updatePoint(player.getId(),exist.getReturnPoints(),true);
                    }
                }
            }
            playerTotalPoints = playerService.getPoints(player.getId());
            StringBuffer buffer = new StringBuffer();
            buffer.append("【"+lotteryName+"返】").append("\r\n");
            buffer.append("【当期总投】" + totalBuyPoints.stripTrailingZeros().toPlainString()).append("\r\n");
            buffer.append("【有效流水】" + totalValidPoints.stripTrailingZeros().toPlainString()).append("\r\n");
            buffer.append("【当期返水】" + validHs.stripTrailingZeros().toPlainString()).append("\r\n");
            buffer.append("【当前盛鱼】" + playerTotalPoints.stripTrailingZeros().toPlainString()).append("\r\n");
            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player, buffer.toString());
            chatRoomMsgService.saveAndSendMsgGroup(toMsg,player.getWxFriendId(),botUser.getWxId(),groupName);
        }
    }

    public void addNewPlayer(String text,BotUser user,String fromUserName,String wxId){
        String txt = text.toUpperCase();
        if (txt.equals("3D") || txt.equals("P3") || txt.equals("福") || txt.equals("体")) {
            int lottype = -1;
            if(txt.equals("3D")  || txt.equals("福")){
                lottype = 1;
            }else{
                lottype = 2;
            }
            if(lottype>0){
                BotUserSetting botUserSetting = botUserSettingService.getByUserId(user.getId());
                Player player = playerService.getOneBy(user.getId(), fromUserName);
                if (null == player) {
                    String chatUrl = "";
                    ChatDomain chatDomain = chatDomainService.getOneBy(); //获取可用的域名
                    if (null != chatDomain) {
                        chatUrl = chatDomain.getUrl();
                    }
                    if(StringUtils.isNullOrEmpty(chatUrl)){
                        String tips = "域名资源不足，请联系老师";
                        chatRoomMsgService.sendMsg(fromUserName, wxId, tips);
                        return;
                    }
                    player = new Player();
                    player.setBotUserId(user.getId());
                    player.setWxFriendId(fromUserName);
                    player.setNickname(fromUserName);
                    String openId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
                    player.setOpenid(openId);
                    player.setUserType(2);
                    player.setChaturl(chatUrl);
                    player.setHsvalue(botUserSetting.getHsvalue());
                    player.setHsType(0);

                    if(user.getLotteryType()==3 || user.getLotteryType() == lottype){
                        player.setLotteryType(lottype);
                        playerService.save(player);
                    }else{
                        String tips = "机器人未开通"+txt+"服务";
                        chatRoomMsgService.sendMsg(fromUserName, wxId, tips);
                        return;
                    }
                }else{
                    //System.out.println("机器人【" + user.getLoginName() + "】===========玩家[" + player.getNickname() + "]已存在");
                    if(user.getLotteryType()==3){
                        if(player.getLotteryType()!=lottype){
                            if(player.getLotteryType()!=3){
                                player.setLotteryType(3);
                                playerService.updateById(player);
                            }
                        }
                    }else{
                        if(user.getLotteryType()!=lottype){
                            String tips = "机器人未开通"+txt+"服务";
                            chatRoomMsgService.sendMsg(fromUserName, wxId, tips);
                            return;
                        }else{
                            player.setLotteryType(user.getLotteryType());
                            playerService.updateById(player);
                        }
                    }
                }
                String chatUrl = player.getChaturl();
                if (chatUrl.contains("&")) {
                    chatUrl = player.getChaturl() + "&openId=" + player.getOpenid();
                } else {
                    chatUrl = player.getChaturl() + "?openId=" + player.getOpenid();
                }
                //String chatUrl = player.getChaturl()+"/?openId="+player.getOpenid();
                String buffer = "ᵕ̈ ᴹᴼᴿᴺᴵᴺᴳ 🌼 ᵕ̈ 🌼\n人生的路虽然难走但是没有绝境\n☛只要寻找总有路可走\n" + chatUrl;
                chatRoomMsgService.sendMsg(fromUserName, wxId, buffer);
                if (StringUtil.isNull(player.getHeadimg())) {
                    //头像为空；
                    getFriendInfo(fromUserName, wxId, player);
                }
            }
        } else {
            Player player = playerService.getOneBy(user.getId(), fromUserName);
            if (null != player) {
                //handleMsg(text, user, player);
                handleMultiMsg(text,user,player);
            }
        }
    }

    //获取好友信息
    public void getFriendInfo(String fromWxId, String wxId, Player player){
//        String url = wechatApiUrl+"Friend/GetContractDetail100";
        String url = wechatApiUrl+"Friend/GetContractDetail";
        Map<String,Object> reqData = new HashMap<>();
        reqData.put("ToWxid",fromWxId);
        reqData.put("Wxid",wxId);
        HttpRequest httpRequest = HttpUtil.createPost(url);
        httpRequest.contentType("application/json");
        httpRequest.body(JSON.toJSONString(reqData));
        HttpResponse httpResponse = httpRequest.execute();
        String result = httpResponse.body();
//        logger.info(">>>>>>Friend/GetContractDetail100>>>>>>"+result);
        RespData respData = JSONObject.parseObject(result,RespData.class);
        if(respData.getCode()==0){
            Map<String,Object> datas =  respData.getData();
//            JSONArray contactList = (JSONArray) datas.get("contactList");
//            JSONObject contactObj = contactList.getJSONObject(0);
//            JSONObject contact = contactObj.getJSONObject("contact");
//
//            JSONObject NickNameObj = contact.getJSONObject("NickName");
//            String nickname = NickNameObj.getString("string");
//
//            String headImgUrl = contact.getString("SmallHeadImgUrl");

            JSONArray contactList = (JSONArray) datas.get("ContactList");
            JSONObject contactObj = contactList.getJSONObject(0);

            JSONObject NickNameObj = contactObj.getJSONObject("NickName");
            String nickname = NickNameObj.getString("string");

            String headImgUrl = contactObj.getString("SmallHeadImgUrl");

            playerService.updateWxInfo(nickname,headImgUrl,player.getId());

        }
    }

    //发送消息


    public void buyFormat(ChatRoomMsg fromMsg, BotUser botUser, Player player){
        //System.out.println(">>>>>>>>>>>>>>有消息来了");
        //chatRoomMsgService.save(fromMsg);
        //rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(fromMsg));
        String url = player.getChaturl();
        if(url.endsWith("/")){
            url = player.getChaturl()+"sendFormat.html";
        }else{
            url = player.getChaturl()+"/sendFormat.html";
        }
        String msg = "点此查看\r\n"+url;

//        StringBuffer buffer = new StringBuffer();
//        buffer.append("<a href='"+url+"'>点此查看</a>");
//        buffer.append("上分+数字，如：上分1000\r\n");
//        buffer.append("下分+数字，如：下分1000\r\n");
//        buffer.append("直选普通+号码+ '各' +金额，如：直选普通123,234,456各100\r\n");
//        buffer.append("直选和值+和值+ '各' +金额，如：直选和值0,1,2,3,4,5各10\r\n");
//        buffer.append("通选+号码+ '各' +金额，如：通选123,234,456各100\r\n");
//        buffer.append("组三普通+号码+ '各' +金额，如：组三普通123456各100\r\n");
//        buffer.append("组三胆拖+胆码+拖码+ '各' +金额，如：组三胆拖5,34567各100\r\n");
//        buffer.append("组三和值+和值+ '各' +金额，如：组三和值7,8,9,10各100\r\n");
        ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser,player,msg);
        toMsg.setSource(1);
        chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());

    }


    public void handleCommonMsg(BotUser botUser, String text,Player player){
        BotUserSetting botUserSetting = botUserSettingService.getByUserId(botUser.getId());
        Draw draw = drawService.getLastDrawInfo();

        Draw p3Draw = p3DrawService.getLastDrawInfo();

        List<Integer> drawNoList = Lists.newArrayList();
        if(null!=draw){
            drawNoList.add(draw.getDrawId());
        }
        if(null!=p3Draw){
            drawNoList.add(p3Draw.getDrawId());
        }

        ChatRoomMsg fromMsg = chatRoomMsgService.createFromWxMsg(botUser,player,text);
        chatRoomMsgService.save(fromMsg);
        rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(fromMsg));

        if (text.startsWith("上分") || text.startsWith("下分")) {
            upDownPoints(fromMsg, botUser, player);
            return;
        }

        if (text.startsWith("作业格式")) {
            buyFormat(fromMsg,botUser,player);
            return;
        }

        if (text.startsWith("流水")) {
            BigDecimal totalBuyPoints = BigDecimal.ZERO;
            //返回当期流水
            BigDecimal p1 = playerBuyRecordService.sumTotalBuy(player.getId(),draw.getDrawId(),1);
            if(null !=p1){
                totalBuyPoints = totalBuyPoints.add(p1);
            }
            BigDecimal p2 = playerBuyRecordService.sumTotalBuy(player.getId(),p3Draw.getDrawId(),2);
            if(null !=p2){
                totalBuyPoints = totalBuyPoints.add(p2);
            }
            String msg = "【累计流水】" + totalBuyPoints.stripTrailingZeros().toPlainString();
            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player, msg);
            chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
            return;
        }

        if (text.startsWith("盈亏")) {
            BigDecimal totalEarnPoints = BigDecimal.ZERO;
            //返回当期盈亏
            BigDecimal p1 = playerBuyRecordService.sumYKBuy(player.getId(),draw.getDrawId(),1);
            if(null !=p1){
                totalEarnPoints = totalEarnPoints.add(p1);
            }
            BigDecimal p2 = playerBuyRecordService.sumYKBuy(player.getId(),p3Draw.getDrawId(),2);
            if(null !=p2){
                totalEarnPoints = totalEarnPoints.add(p2);
            }
            String msg = "YK：" + totalEarnPoints.stripTrailingZeros().toPlainString();
            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player, msg);
            chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
            return;
        }

        if (text.startsWith("盛鱼")) {
            String msg = "【当前盛鱼】" +player.getPoints().stripTrailingZeros().toPlainString();
            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player, msg);
            chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
            return;
        }

        if (text.startsWith("查")) {
            List<PlayerBuyRecord> allList = Lists.newArrayList();
            //查询当期下注记录
            List<PlayerBuyRecord> buyList = playerBuyRecordService.getListBy(player.getId(),draw.getDrawId(),1);
            List<PlayerBuyRecord> buyList2 = playerBuyRecordService.getListBy(player.getId(),p3Draw.getDrawId(),2);
            if(null!=buyList && buyList.size()>0) {
                allList.addAll(buyList);
            }
            if(null!=buyList2 && buyList2.size()>0) {
                allList.addAll(buyList2);
            }
            String msg = null;
            if(allList.size()>0) {
                StringBuffer sb = new StringBuffer();
                for (PlayerBuyRecord playerBuyRecord : allList) {
                    sb.append(playerBuyRecord.getBuyDesc().replace("\r\n","")).append("\r\n");
                }
                msg = "【当前作业】\r\n" + sb.toString() + "【状态】交作业成功√√\r\n"
                        + "【当前盛鱼】" + player.getPoints().stripTrailingZeros().toPlainString();
            }else{
                msg = "【当前作业】未提交任何作业哦\r\n" + "【当前盛鱼】" + player.getPoints().stripTrailingZeros().toPlainString();

            }
            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player, msg);
            chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
            return;
        }


        if (text.startsWith("返")) {
            //机器人开启了玩家自助返水
            if(null!=botUserSetting && null!=botUserSetting.getHsHelpBack() && botUserSetting.getHsHelpBack()==1){

                switch (player.getLotteryType()){
                    case 3:
                        backWaterMsg(botUser,player,1);
                        backWaterMsg(botUser,player,2);
                        break;
                    case 2:
                        backWaterMsg(botUser,player,2);
                        break;
                    case 1:
                        backWaterMsg(botUser,player,1);
                        break;
                    default:
                        break;
                }
            }else{
                ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player, "回水功能未开启");
                chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
            }
        }
    }



    public void handleMsg(String text,BotUser botUser, Player player){
        boolean isCommonCmd = false;
        for(String cmd : GlobalConst.commonCmd){
            if(text.startsWith(cmd)){
                isCommonCmd = true;
                break;
            }
        }
        if(isCommonCmd){
//            logger.info(String.format("收到微信消息>>>>>>>>>>toUser===%s,fromUser===%s,text===%s", botUser.getLoginName(), player.getNickname(), text));
            handleCommonMsg(botUser,text,player);
        }else{
            String text1 = text.toUpperCase();
            if(text1.startsWith("P3") || text1.startsWith("3D")){
//                logger.info(String.format("收到微信消息>>>>>>>>>>toUser===%s,fromUser===%s,text===%s", botUser.getLoginName(), player.getNickname(), text));
                ChatRoomMsg fromMsg = chatRoomMsgService.createFromWxMsg(botUser,player,text);
                chatRoomMsgService.save(fromMsg);
                rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(fromMsg));

                int lottype = text1.startsWith("P3")?2:1;
                String lotName = lottype==2?"P3":"3D";
                if(player.getLotteryType()==3 || player.getLotteryType()==lottype){

                    String txt = text1.substring(2);
                    boolean isBuy = false;
                    for(String word : GlobalConst.keywords1){
                        if(txt.startsWith(word)){
                            kuaidaBuyMsgServiceV2.handleMsg(fromMsg,botUser,player,lottype);
//                            if(lottype==1){
//                                kuaidaBuyMsgService2.handleMsg(fromMsg,botUser,player,lottype);
//                            }else if(lottype==2){
//                                p3KuaidaBuyMsgService.handleMsg(fromMsg,botUser,player);
//                            }
                            isBuy = true;
                            break;
                        }
                    }
                    if(!isBuy){
                        ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player,"作业格式有误");
                        chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
                    }
                }else{
                    ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player,"哦噢，您无提交"+lotName+"作业的权限");
                    chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
                }
            }
        }
    }

    /**
     * 处理多组下注
     * @param text
     * @param botUser
     * @param player
     */
    public void handleMultiMsg(String text,BotUser botUser, Player player){
        boolean isCommonCmd = false;
        for(String cmd : GlobalConst.commonCmd){
            if(text.startsWith(cmd)){
                isCommonCmd = true;
                break;
            }
        }
        if(isCommonCmd){
//            logger.info(String.format("收到微信消息>>>>>>>>>>toUser===%s,fromUser===%s,text===%s", botUser.getLoginName(), player.getNickname(), text));
            handleCommonMsg(botUser,text,player);
        }else{

            Boolean checkTxtResult = true;
            String[] multiArr = text.split("\n");
            //System.out.println("=====multi group wx=="+ Arrays.stream(multiArr).collect(Collectors.joining(",")));
            for(String cmdText : multiArr){
                if(cmdText.toUpperCase().startsWith("P3")
                        || cmdText.toUpperCase().startsWith("3D") || cmdText.toUpperCase().startsWith("福")
                        || cmdText.toUpperCase().startsWith("体")){
                    if(cmdText.toUpperCase().startsWith("P3") || cmdText.toUpperCase().startsWith("体")){
                       Draw draw =  p3DrawService.getLastDrawInfo();
                       if(null==draw || draw.getOpenStatus()!=1){
                           ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player,"【P3】^^★★★停止-上课★★★");
                           chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
                           return;
                       }
                    }else{
                        Draw draw =  drawService.getLastDrawInfo();
                        if(null==draw || draw.getOpenStatus()!=1){
                            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player,"【3D】^^★★★停止-上课★★★");
                            chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
                            return;
                        }
                    }


                }else{
                    checkTxtResult = false;
                    break;
                }
            }

            if(checkTxtResult){
                logger.info(String.format("收到微信消息3>>>>>>>>>>toUser===%s,fromUser===%s,text===%s", botUser.getLoginName(), player.getNickname(), text));
                ChatRoomMsg fromMsg = chatRoomMsgService.createFromWxMsg(botUser,player,text);
                chatRoomMsgService.save(fromMsg);
                rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(fromMsg));

                for(String cmdText : multiArr){
                    String text1 = cmdText.toUpperCase();
                    if(text1.startsWith("P3") || text1.startsWith("3D") || text1.startsWith("福") || text1.startsWith("体")){
                        ChatRoomMsg childMsg = chatRoomMsgService.createFromWxMsg(botUser,player,text1);
                        int lottype = 1;
                        if(text1.startsWith("P3") || text1.startsWith("体")){
                            lottype = 2;
                        }
                        String txt = "";
                        if(text1.startsWith("P3") || text1.startsWith("3D")){
                            txt = text1.substring(2);
                        }else{
                            txt = text1.substring(1);
                        }

                        //int lottype = text1.startsWith("P3")?2:1;
                        String lotName = lottype==2?"P3":"3D";
                        if(player.getLotteryType()==3 || player.getLotteryType()==lottype){
                            //String txt = text1.substring(2);
                            boolean isBuy = false;
                            for(String word : GlobalConst.keywords2){
                                if(txt.startsWith(word)){
                                    kuaidaBuyMsgServiceV2.handleMsg(childMsg,botUser,player,lottype);
                                    isBuy = true;
                                    break;
                                }
                            }
                            if(!isBuy){
                                ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player,"作业格式有误:"+cmdText);
                                chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
                            }
                        }else{
                            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player,"哦噢，您无提交"+lotName+"作业的权限");
                            chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
                        }
                    }
                }
            }


        }
    }


    public void upDownPoints(ChatRoomMsg fromMsg, BotUser botUser, Player player){

        try {

//            chatRoomMsgService.save(fromMsg);
//            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(fromMsg));

            String content = fromMsg.getMsg();

            String points = "";
            Integer optType = null;
            if (content.contains("上分")) {
                optType = 0;
                //读取上分值
            } else if (content.contains("下分")) {
                optType = 1;
            }
            points = Tools.substractDigit(content);
            if (StringUtil.isNull(points)) {
                ChatRoomMsg toMsg = chatRoomMsgService.getErrorMsg(botUser, player);
                chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());

            } else {

                if (optType == 1) {
                    if (player.getPoints().compareTo(new BigDecimal(points)) < 0) {
                        //可用积分小于下分值
                        ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player, "面上不足");
                        chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
                        return;
                    }
                }

                PlayerPointsRecord pointsRecord = new PlayerPointsRecord();
                pointsRecord.setPoints(new BigDecimal(points));
                pointsRecord.setAuthStatus(0);
                pointsRecord.setApplyTime(new Date());
                pointsRecord.setOptType(optType);
                pointsRecord.setPlayerId(player.getId());
                pointsRecord.setBotUserId(botUser.getId());
                playerPointsRecordService.save(pointsRecord);
                ChatRoomMsg toMsg = new ChatRoomMsg();
                toMsg.setFromUserId(botUser.getId());
                toMsg.setFromUserNick("机器人");
                toMsg.setFromUserType(1);
                toMsg.setMsg("发送成功，请联系老师");
                toMsg.setToUserNick(player.getNickname());
                toMsg.setToUserId(player.getId());
                toMsg.setCreateTime(new Date());
                toMsg.setBotUserId(botUser.getId());
                toMsg.setToUserType(0);
                toMsg.setMsgType(0);
                toMsg.setOptType(0);
                toMsg.setSource(1);
                chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
            }
        }catch (Exception e){
            e.printStackTrace();
            ChatRoomMsg toMsg = new ChatRoomMsg();
            toMsg.setFromUserId(botUser.getId());
            toMsg.setFromUserNick("机器人");
            toMsg.setFromUserType(1);
            toMsg.setMsg("系统繁忙，请稍后重试");
            toMsg.setToUserNick(player.getNickname());
            toMsg.setToUserId(player.getId());
            toMsg.setCreateTime(new Date());
            toMsg.setBotUserId(botUser.getId());
            toMsg.setToUserType(0);
            toMsg.setMsgType(0);
            toMsg.setOptType(0);
            toMsg.setSource(1);
            chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
        }
    }




    //推送微信登录状态信息至MQ
    public void pushWxLoginStatusMsg(BotUser botUser,Integer wxStatus){
        Map<String,Object> info = new HashMap<>();
        info.put("wxId",botUser.getWxId());
        info.put("wxNick",botUser.getWxNick());
        info.put("wxHeadimg",botUser.getWxHeadimg());
        info.put("wxStatus",wxStatus);
        info.put("flag","login");

        ResponseBean responseBean = new ResponseBean(0, 0, "", info,true);
        WechatPushMsgVo vo = new WechatPushMsgVo();
        vo.setBotUserId(botUser.getWxId());
        vo.setResponseBean(responseBean);
        rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botWechat",JSON.toJSONString(vo));
    }



    //返水消息
    public void backWaterMsg(BotUser botUser,Player player,Integer lotteryType){
        String lotteryName = "";
        Draw draw = null;
        if(lotteryType==1){
            draw = drawService.getLastDrawInfo();
            lotteryName = "3D";
        }else if(lotteryType==2){
            draw = p3DrawService.getLastDrawInfo();
            lotteryName = "P3";
        }
        if(null!=draw){
            BigDecimal totalBuyPoints = BigDecimal.ZERO; //总投
            BigDecimal totalValidPoints = BigDecimal.ZERO; //有效流水
            BigDecimal validHs = BigDecimal.ZERO; //回水金额
            BigDecimal playerTotalPoints = player.getPoints();
            BigDecimal p1 = playerBuyRecordService.sumTotalBuy(player.getId(),draw.getDrawId(),lotteryType);
            if(null !=p1){
                totalBuyPoints = totalBuyPoints.add(p1);
            }
            p1 = playerBuyRecordService.sumValidBuy(player.getId(),draw.getDrawId(),lotteryType);
            if(null !=p1){
                totalValidPoints = totalValidPoints.add(p1);
            }
            PlayerReturnPoints exist = playerReturnPointsService.getOneBy(player.getId(),draw.getDrawId(),lotteryType);
            if(null!=exist){
                validHs = validHs.add(exist.getReturnPoints());
                if(exist.getStatus()==0){
                    exist.setStatus(1);
                    exist.setReturnTime(new Date());
                    exist.setReturnType(1);
                    if(playerReturnPointsService.updateById(exist)){
                        playerService.updatePoint(player.getId(),exist.getReturnPoints(),true);
                    }
                }
            }
            playerTotalPoints = playerService.getPoints(player.getId());
            StringBuffer buffer = new StringBuffer();
            buffer.append("【"+lotteryName+"返】").append("\r\n");
            buffer.append("【当期总投】" + totalBuyPoints.stripTrailingZeros().toPlainString()).append("\r\n");
            buffer.append("【有效流水】" + totalValidPoints.stripTrailingZeros().toPlainString()).append("\r\n");
            buffer.append("【当期返水】" + validHs.stripTrailingZeros().toPlainString()).append("\r\n");
            buffer.append("【当前盛鱼】" + playerTotalPoints.stripTrailingZeros().toPlainString()).append("\r\n");
            ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser, player, buffer.toString());
            chatRoomMsgService.saveAndSendMsg(toMsg,player.getWxFriendId(),botUser.getWxId());
        }
    }




}
