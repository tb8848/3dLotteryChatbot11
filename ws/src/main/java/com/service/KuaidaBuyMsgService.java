package com.service;

import cn.hutool.core.lang.Snowflake;
import com.GlobalConst;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.*;
import com.config.RequestDataHelper;
import com.dao.ChatRoomMsgDAO;
import com.dao.DrawBuyRecordDAO;
import com.util.Code3DCreateUtils;
import com.util.StringUtil;
import com.util.Tools;
import com.vo.BuyRecord3DVO;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Lazy
public class KuaidaBuyMsgService{


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
    private BuyRecord3DService buyRecord3DService;

    @Autowired
    private BotUserSettingService botUserSettingService;

    @Autowired
    private DrawService drawService;

    @Autowired
    private ReportToPanService reportToPanService;

    @Autowired
    private WechatApiService wechatApiService;

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private BotUserPanService botUserPanService;

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

    //快打下注，固定文本格式
    public void kuaidaBuy(ChatRoomMsg fromMsg, BotUser botUser, Player player){
        try {
            Draw draw = drawService.getLastDrawInfo();
            if(draw.getOpenStatus()!=1){
                ChatRoomMsg toMsg = createMsg(botUser, player, "^^★★★停止-上课★★★");
                toMsg.setSource(1);
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                    //wechatIpadTokenService.sendMsgToFriend(toMsg);
                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                }
                return;
            }
            String userId = botUser.getId();
//            System.out.println(">>>>>>>>>>>>>>有下注消息来了");

            String content = fromMsg.getMsg().substring(2);
            String[] arr = content.split("各");
            List<BuyRecord3DVO> buyList = Lists.newArrayList();
            if (arr.length != 2) {
                ChatRoomMsg toMsg = getErrorMsg(botUser, player);
                toMsg.setSource(0);
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                }
                return;
            }
            BigDecimal buyMoney = BigDecimal.ZERO;
            try {
                buyMoney = new BigDecimal(arr[1]);

            } catch (Exception e) {
                ChatRoomMsg toMsg = createMsg(botUser, player, content+"\r\n金额错误");
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                    //wechatIpadTokenService.sendMsgToFriend(toMsg);
                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                }
                return;
            }
            String text = arr[0].toUpperCase();
            if (text.equals("拖拉机") || text.equals("三同号") || text.equals("猜三同")
                    || text.equals("大") || text.equals("小") || text.equals("奇") || text.equals("偶") || text.equals("大小") || text.equals("奇偶")) {
                switch (text) {
                    case "大小":
                    case "大":
                    case "小":
                        buyList = noCodesBuy(botUser, player, buyMoney, arr[0], "9");
                        break;
                    case "奇偶":
                    case "偶":
                    case "奇":
                        buyList = noCodesBuy(botUser, player, buyMoney, arr[0], "10");
                        break;
                    case "拖拉机":
                        buyList = noCodesBuy(botUser, player, buyMoney, arr[0], "12");
                        break;
                    case "猜三同":
                    case "三同号":
                        buyList = noCodesBuy(botUser, player, buyMoney, arr[0], "11");

                        break;

                }
                if (null != buyList && buyList.size()>0) {
                    xiazhu(botUser, player, fromMsg, buyList);
                }else{
                    ChatRoomMsg toMsg = createMsg(botUser, player, "作业内容不符合【"+text+"】的要求");
                    dataDao.insert(toMsg);
                    simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                    if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                    }
                }
            } else {

                boolean matchSucc = false;
                for (String word : GlobalConst.keywords) {
                    if (text.startsWith(word)) {
                        String type = word;
                        String code = null;
                        matchSucc = true;
                        String bsgReg = "[百][0-9]+[十][0-9]+[个][0-9]+";
                        if(text.matches(bsgReg)){
                            code = text;
                        }else{
                            String[] typeArr = text.split(word);
                            if (typeArr.length != 2) {
                                ChatRoomMsg toMsg = getErrorMsg(botUser, player);
                                toMsg.setSource(0);
                                dataDao.insert(toMsg);
                                simpMessagingTemplate.convertAndSend("/topic/room/" + userId, toMsg);
                                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                                    //wechatIpadTokenService.sendMsgToFriend(toMsg);
                                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                                }
                                return;
                            }
                            code = typeArr[1];
                            String regex = "((\\d|\\-)+(,|，)?)+";
                            if(!code.matches(regex)){
                                ChatRoomMsg toMsg = createMsg(botUser, player, content+"\r\n号码格式错误");
                                toMsg.setSource(0);
                                dataDao.insert(toMsg);
                                simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                                }
                                return;
                            }
                        }
                        switch (type) {
                            case "单选":
                                buyList = codeBuy(botUser, player, buyMoney, code, "1");
                                break;
                            case "直选":
                            case "直选普通":
                                buyList = zxtxBuy(botUser, player, buyMoney, code, "1");
                                break;
                            case "直选和值":
                                buyList = zxhzBuy(botUser, player, buyMoney, code, "1");
                                break;
                            case "通选":
                                buyList = zxtxBuy(botUser, player, buyMoney, code, "2");
                                break;
                            case "组三":
                            case "组三普通":
                                buyList = z3Buy(botUser, player, buyMoney, code, "3");
                                break;
                            case "组三胆拖":
                                buyList = z3dtBuy(botUser, player, buyMoney, code, "3");
                                break;
                            case "组三和值":
                                buyList = z3hzBuy(botUser, player, buyMoney, code, "3");
                                break;
                            case "组六普通":
                            case "组六":
                                buyList = z6Buy(botUser, player, buyMoney, code, "4");
                                break;
                            case "组六胆拖":
                                buyList = z6dtBuy(botUser, player, buyMoney, code, "4");
                                break;
                            case "组六和值":
                                buyList = z6hzBuy(botUser, player, buyMoney, code, "4");
                                break;
                            case "和数":
                                buyList = hsBuy(botUser, player, buyMoney, code, "5");
                                break;
                            case "跨度":
                                buyList = kdBuy(botUser, player, buyMoney, code, "13");
                                break;
                            case "1D":
                                buyList = ding1Buy(botUser, player, buyMoney, code, "6");
                                break;
                            case "猜1D":
                                buyList = c1dBuy(botUser, player, buyMoney, code, "6");
                                break;
                            case "2D":
                                buyList = ding2Buy(botUser, player, buyMoney, code, "7");
                                break;
                            case "猜2D":
                                buyList = c2dBuy(botUser, player, buyMoney, code, "7");
                                break;
                            case "包选三":
                                buyList = b3Buy(botUser, player, buyMoney, code, "8");
                                break;
                            case "包选六":
                                buyList = b6Buy(botUser, player, buyMoney, code, "8");
                                break;
                            case "佰":
                            case "百":
                                buyList = D3Buy(botUser, player, buyMoney, code, "1");
                                break;
                            default:
                                ChatRoomMsg toMsg = createMsg(botUser, player, content+"\r\n类别格式错误");
                                toMsg.setSource(0);
                                dataDao.insert(toMsg);
                                simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                                }
                                return;
                        }
                        if (null != buyList && buyList.size() > 0) {
                            xiazhu(botUser, player, fromMsg, buyList);
                        }else{
                            ChatRoomMsg toMsg = createMsg(botUser, player, content+"\r\n作业内容不符合【"+type+"】的要求");
                            toMsg.setSource(0);
                            dataDao.insert(toMsg);
                            simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                            if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                            }
                        }
                        break;
                    }
                }

                if(!matchSucc){
                    ChatRoomMsg toMsg = createMsg(botUser, player, content+"\r\n类别格式错误");
                    toMsg.setSource(0);
                    dataDao.insert(toMsg);
                    simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                    if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            ChatRoomMsg toMsg = createMsg(botUser, player, "系统繁忙，请稍后重试");
            toMsg.setSource(0);
            dataDao.insert(toMsg);
            simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
            if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
            }
        }
    }


    //单选
    private List<BuyRecord3DVO> codeBuy(BotUser botUser, Player player, BigDecimal buyMoney, String codeRule, String lmId) {
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] codeArr = codeRule.split(",|，");
        if(codeArr.length==0){
//            ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//            toMsg.setSource(0);
//            dataDao.insert(toMsg);
//            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//            }
//            toMsg.setSource(1);
//            dataDao.insert(toMsg);
//            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
//            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
            return null;
        }
        for(String code : codeArr){
            if(code.length()!=3){
//                ChatRoomMsg toMsg = createMsg(botUser,player,"号码["+code+"]错误");
//                toMsg.setSource(0);
//                dataDao.insert(toMsg);
//                simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//                if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                    wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//                }
                return null;
            }
        }
        String hzname = "直选单式";
        for(String code : codeArr) {
            String[] arr = code.split("");
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBai(arr[0]);
            oneRecord.setShi(arr[1]);
            oneRecord.setGe(arr[2]);
            oneRecord.setBuyAmount(1);
            oneRecord.setValue(arr[0] + "," + arr[1] + "," + arr[2]);
            oneRecord.setLmId(lmId);
            oneRecord.setLsTypeId("1");
            oneRecord.setBuyMoney(buyMoney);
            list.add(oneRecord);
        }
        return list;
    }




    public void reportToPan(BotUser botUser,Player player,ChatRoomMsg msg,List<BuyRecord3DVO> buyList,BotUserPan botUserPan){
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
        Draw draw = drawService.getLastDrawInfo();
        BotUserSetting botUserSetting = botUserSettingService.getByUserId(botUser.getId());

        String playerBuyId = snowflake.nextIdStr();
        PlayerBuyRecord playerBuyRecord = new PlayerBuyRecord();
        playerBuyRecord.setId(playerBuyId);
        playerBuyRecord.setPlayerId(player.getId());
        playerBuyRecord.setBuyType(0);
        playerBuyRecord.setBotUserId(player.getBotUserId());
        playerBuyRecord.setLotteryType(1);

        String lmId = buyList.get(0).getLmId();
        Map<String,Object> buyResult = null;
        ResponseBean reportRespData = null;
        buyList.forEach(item->{
            item.setPlayerBuyId(playerBuyId);
        });
//        System.out.println("=========3D报网====》"+JSON.toJSONString(buyList));
        if("5".equals(lmId) || "13".equals(lmId)){
            reportRespData = reportToPanService.buyHs(reportToPanUrl,buyList,botUserPan.getLogin3dToken());
        }else{
            reportRespData = reportToPanService.buy(reportToPanUrl,buyList,botUserPan.getLogin3dToken());
        }
        ChatRoomMsg toMsg = null;
        switch (reportRespData.getCode()){
            case 403: //盘口登录token已失效
                botUserPanService.clearPanInfo(botUser.getId(),1);
                toMsg = createMsg(botUser,player,"机器人未登录3D网盘");
                toMsg.setSource(0);
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                    wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                }
                break;
            case -1:
            case 500:
                String errmsg = reportRespData.getMsg();
                toMsg = createMsg(botUser,player,errmsg);
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                    wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
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
                    playerBuyRecord.setBuyDesc(msg.getMsg());
                    playerBuyRecord.setBuyFrom(0);
                    playerBuyRecord.setKuaixuanRule(msg.getKuaixuanRule());
                    playerBuyRecord.setEarnPoints(BigDecimal.ZERO.subtract(totalPoints));
                    dataList.forEach(item->{
                        item.setBaopaiId(playerBuyRecord.getId());
                        item.setVipId(player.getId());
                        item.setBuyType(1);
                    });
                    Map dynamicPrama = new HashMap();
                    dynamicPrama.put("qihao", String.valueOf(draw.getDrawId()));
                    RequestDataHelper.setRequestData(dynamicPrama);

                    if(drawBuyRecordDAO.batchAddBuyCode(dataList)>0){
                        playerBuyRecordService.save(playerBuyRecord);
                        playerService.updatePoint(player.getId(),totalPoints,false);
                        BigDecimal points = playerService.getPoints(player.getId());
                        String newmsg = "[3D课号]"+draw.getDrawId()+"\r\n"+msg.getMsg()+"\r\n交作业成功√√\r\n【份数】："+buyAmount+"\r\n"
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
                            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                        }
                    }
                }
                break;
        }

    }


    private void xiazhu(BotUser botUser, Player player,ChatRoomMsg fromMsg, List<BuyRecord3DVO> buyList) {
        BigDecimal totalBuyPoints = buyList.stream().map(item->item.getBuyMoney()).reduce(BigDecimal.ZERO,BigDecimal::add);
        if(totalBuyPoints.compareTo(player.getPoints())>0){
            //玩家积分不够
            ChatRoomMsg toMsg = createMsg(botUser,player,"面上不足");
            toMsg.setSource(0);
            dataDao.insert(toMsg);
            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
            }
            return;
        }

        if(player.getPretexting()==1 || player.getEatPrize()==1){
            String msg = fromMsg.getMsg();
            Draw draw = drawService.getLastDrawInfo();
            BotUserSetting botUserSetting = botUserSettingService.getByUserId(botUser.getId());
            String lmId = buyList.get(0).getLmId();
            Map<String,Object> buyResult = null;
//            System.out.println("=========本地下注====》"+JSON.toJSONString(buyList));
            if("5".equals(lmId) || "13".equals(lmId)){
                buyResult = buyRecord3DService.buy3dHs(player,buyList,draw,-2,null,msg,1);
            }else{
                buyResult = buyRecord3DService.codesBatchBuy(player,buyList,draw,-2,null,msg,1);
            }
            if(buyResult.containsKey("playerBuyId")){
                String playerBuyId = (String)buyResult.get("playerBuyId");
                PlayerBuyRecord record = playerBuyRecordService.getById(playerBuyId);
                BigDecimal points = playerService.getPoints(player.getId());
                String newmsg = "[3D课号]"+draw.getDrawId()+"\r\n"+msg+"\r\n交作业成功√√\r\n【份数】："+record.getBuyAmount()+"\r\n"
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
                    wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                }
            }else{
                String errmsg = (String)buyResult.get("errmsg");
                if(StringUtil.isNotNull(errmsg)){
                    ChatRoomMsg toMsg = createMsg(botUser,player,errmsg);
                    toMsg.setSource(0);
                    dataDao.insert(toMsg);
                    simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                    if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                        wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                    }
                }
            }
        }else{
            if(player.getReportNet()==1){
                BotUserPan botUserPan = botUserPanService.getOneBy(botUser.getId(),1);
                if(null!=botUserPan && StringUtil.isNotNull(botUserPan.getLogin3dToken())){
                    //报网
                    reportToPan(botUser,player,fromMsg,buyList,botUserPan);
                }else{
                    ChatRoomMsg toMsg = createMsg(botUser,player,"机器人未登录3D网盘");
                    toMsg.setSource(0);
                    dataDao.insert(toMsg);
                    simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                    if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                        wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                    }
                }
            }
        }
    }

    public List<BuyRecord3DVO> zxtxBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] locArr = codeRule.split(",|，");
        if(locArr.length!=3){
//            ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//            toMsg.setSource(0);
//            dataDao.insert(toMsg);
//            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//            }
            return null;
        }
        String bai = locArr[0];
        String shi = locArr[1];
        String ge = locArr[2];
        String hzname = "";
        int sum = bai.length() * shi.length() * ge.length();
        if(sum==1){
            hzname = lmId.equals("1")?"直选单式":"通选单式";
        }else{
            hzname = lmId.equals("1")?"直选复式":"通选复式";
        }
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBai(bai);
        oneRecord.setShi(shi);
        oneRecord.setGe(ge);
        oneRecord.setBuyAmount(sum);
        oneRecord.setValue(bai+","+shi+","+ge);
        oneRecord.setLmId(lmId);
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyMoney(buyMoney);
        list.add(oneRecord);
        return list;
    }

    public List<BuyRecord3DVO> zxhzBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] hzArr = codeRule.split(",|，");
        boolean hasError = false;
        for(String hz :hzArr){
            if(hz.contains(".")){
                hasError = true;
            }else{
                if(Integer.valueOf(hz)<0 || Integer.valueOf(hz)>27){
                    hasError = true;
                }
            }
            if(hasError){
//                ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//                toMsg.setSource(0);
//                dataDao.insert(toMsg);
//                simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//                if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                    wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//                }
                return null;
            }
        }
        List<String> hzList = Arrays.asList(hzArr);
        List<String> codeList = Code3DCreateUtils.hezhi(hzList);
        String hzname = "直选和值";
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setHzList(hzList);
        oneRecord.setValue(hzList.stream().collect(Collectors.joining(",")));
        oneRecord.setLmId(lmId);
        oneRecord.setLsTypeId("2");
        oneRecord.setBuyMoney(buyMoney);
        list.add(oneRecord);
        return list;
    }


    //组三普通
    public List<BuyRecord3DVO> z3Buy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        boolean hasError = false;
        if(codeRule.contains(",") || codeRule.contains("，")){
            hasError = true;
        }
        String[] numArr = codeRule.split("");
        Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
        if(nums.size()<2){
            hasError = true;
        }
        if(hasError){
//            ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//            toMsg.setSource(0);
//            dataDao.insert(toMsg);
//            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//            }
            return null;
        }

        String bai = codeRule;
        List<String> codeList = Code3DCreateUtils.z3Code(bai);

        for(String code : codeList){
            String[] arr = code.split("");
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName("组三单式");
            oneRecord.setBai(arr[0]);
            oneRecord.setShi(arr[1]);
            oneRecord.setGe(arr[2]);
            oneRecord.setBuyAmount(1);
            oneRecord.setBuyCode(code);
            oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
            oneRecord.setLmId(lmId);
            oneRecord.setLsTypeId("1");
            oneRecord.setBuyMoney(buyMoney);
            list.add(oneRecord);
        }
        return list;
    }

    //组三胆拖
    public List<BuyRecord3DVO> z3dtBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split(",|，");
        boolean hasError = false;
        if(numArr.length>2){
            hasError = true;
        }
        String tm = null;
        String dm = null;
        if(numArr.length==1){
            tm = numArr[0];
        }else{
            dm = numArr[0];
            tm = numArr[1];
        }
        int dmLen = 0;
        int tmLen = 0;
        if(StringUtil.isNull(tm)){
            hasError = true;
        }else{
            if(StringUtil.isNotNull(tm)){
                Set<String> tmCode = Arrays.asList(tm.split("")).stream().collect(Collectors.toSet());
                tm = tmCode.stream().collect(Collectors.joining());
                tmLen = tm.length();
            }
            if(StringUtil.isNotNull(dm)){
                Set<String> dmCode = Arrays.asList(dm.split("")).stream().collect(Collectors.toSet());
                dm = dmCode.stream().collect(Collectors.joining());
                if(dm.length()>1){
                    hasError = true;
                }else{
                    for(String c : dmCode){
                        if(tm.contains(c)){ //拖码中包含胆码号码，则删除胆码中对应的号码
                            dm = dm.replace(c,"");
                        }
                    }
//                    if(tm.contains(dm)){
//                        tm = tm.replace(dm,"");
//                    }
                    tmLen = tm.length();
                    dmLen = dm.length();
                }
            }
        }
        if(tmLen+dmLen<2){
            hasError = true;
        }
        if(hasError){
//            ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//            toMsg.setSource(0);
//            dataDao.insert(toMsg);
//            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//            }
            return null;
        }

        List<String> codeList = Code3DCreateUtils.z3DtCode(dm,tm);

        for(String code : codeList){
            String[] arr = code.split("");
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName("组三单式");
            oneRecord.setBai(arr[0]);
            oneRecord.setShi(arr[1]);
            oneRecord.setGe(arr[2]);
            oneRecord.setBuyAmount(1);
            oneRecord.setBuyCode(code);
            oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
            oneRecord.setLmId("3");
            oneRecord.setLsTypeId("2");
            oneRecord.setBuyMoney(buyMoney);
            list.add(oneRecord);
        }
        return list;
    }

    //组三和值
    public List<BuyRecord3DVO> z3hzBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split(",|，");
        boolean hasError = false;
        if(numArr.length==0){
            hasError = true;
        }
        String[] validHzArr = {"1","2","3","4","5","6","7","8","9","10","11",
                "12","13","14","15","16","17","18","19","20","21","22","23","24","25","26"};
        List<String> validHzList = Arrays.asList(validHzArr);
        List<String> hzList = Arrays.asList(numArr);
        for(String hz : hzList){
            if(!validHzList.contains(hz)){
                hasError = true;
                break;
            }
        }
        if(hasError){
//            ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//            toMsg.setSource(0);
//            dataDao.insert(toMsg);
//            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//            }
            return null;
        }

        List<String> codeList = Code3DCreateUtils.z3hezhi(hzList);
        String hzname = "组三和值";
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setValue(hzList.stream().collect(Collectors.joining(",")));
        oneRecord.setLmId("3");
        oneRecord.setLsTypeId("3");
        oneRecord.setHzList(hzList);
        oneRecord.setBuyMoney(buyMoney);
        list.add(oneRecord);
        return list;
    }



    //组六普通
    public List<BuyRecord3DVO> z6Buy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        boolean hasError = false;
        if(codeRule.contains(",") || codeRule.contains("，")){
            hasError = true;
        }
        String[] numArr = codeRule.split("");
        Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
        if(nums.size()<3){
            hasError = true;
        }
        if(hasError){
//            ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//            toMsg.setSource(0);
//            dataDao.insert(toMsg);
//            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//            }
            return null;
        }

        String bai = codeRule;
        List<String> codeList = Code3DCreateUtils.z6CodeBy(bai);

        for(String code : codeList){
            String[] arr = code.split("");
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName("组六单式");
            oneRecord.setBai(arr[0]);
            oneRecord.setShi(arr[1]);
            oneRecord.setGe(arr[2]);
            oneRecord.setBuyAmount(1);
            oneRecord.setBuyCode(code);
            oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
            oneRecord.setLmId("4");
            oneRecord.setLsTypeId("1");
            oneRecord.setBuyMoney(buyMoney);
            list.add(oneRecord);
        }
        return list;
    }

    //组六胆拖
    public List<BuyRecord3DVO> z6dtBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId ){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split(",|，");
        boolean hasError = false;
        if(numArr.length>2){
            hasError = true;
        }
        String tm = null;
        String dm = null;
        if(numArr.length==1){
            tm = numArr[0];
        }else{
            dm = numArr[0];
            tm = numArr[1];
        }
        int dmLen = 0;
        int tmLen = 0;
        if(StringUtil.isNull(tm)){
            hasError = true;
        }else{
            if(StringUtil.isNotNull(tm)){
                Set<String> tmCode = Arrays.asList(tm.split("")).stream().collect(Collectors.toSet());
                tm = tmCode.stream().collect(Collectors.joining());
                tmLen = tm.length();
            }
            if(StringUtil.isNotNull(dm)){
                Set<String> dmCode = Arrays.asList(dm.split("")).stream().collect(Collectors.toSet());
                dm = dmCode.stream().collect(Collectors.joining());
                if(dm.length()>2){
                    hasError = true;
                }else{
                    for(String c : dmCode){
                        if(tm.contains(c)){ //拖码中包含胆码号码，则删除胆码中对应的号码
                            dm = dm.replace(c,"");
                        }
                    }
//                    if(tm.contains(dm)){
//                        tm = tm.replace(dm,"");
//                    }
                    tmLen = tm.length();
                    dmLen = dm.length();
                }
            }
        }
        if(tmLen+dmLen<2){
            hasError = true;
        }
        if(hasError){
//            ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//            toMsg.setSource(0);
//            dataDao.insert(toMsg);
//            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//            }
            return null;
        }

        List<String> codeList = Code3DCreateUtils.z6DtCode(dm,tm);

        for(String code : codeList){
            String[] arr = code.split("");
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName("组六单式");
            oneRecord.setBai(arr[0]);
            oneRecord.setShi(arr[1]);
            oneRecord.setGe(arr[2]);
            oneRecord.setBuyAmount(1);
            oneRecord.setBuyCode(code);
            oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
            oneRecord.setLmId("4");
            oneRecord.setLsTypeId("2");
            oneRecord.setBuyMoney(buyMoney);
            list.add(oneRecord);
        }
        return list;
    }

    //组六和值
    public List<BuyRecord3DVO> z6hzBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split(",|，");
        boolean hasError = false;
        if(numArr.length==0){
            hasError = true;
        }
        String[] validHzArr = {"3","4","5","6","7","8","9","10","11",
                "12","13","14","15","16","17","18","19","20","21","22","23","24"};
        List<String> validHzList = Arrays.asList(validHzArr);
        List<String> hzList = Arrays.asList(numArr);
        for(String hz : hzList){
            if(!validHzList.contains(hz)){
                hasError = true;
                break;
            }
        }
        if(hasError){
//            ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//            toMsg.setSource(0);
//            dataDao.insert(toMsg);
//            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//            }
            return null;
        }

        List<String> codeList = Code3DCreateUtils.z3hezhi(hzList);
        String hzname = "组六和值";
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setValue(hzList.stream().collect(Collectors.joining(",")));
        oneRecord.setLmId("4");
        oneRecord.setLsTypeId("3");
        oneRecord.setHzList(hzList);
        oneRecord.setBuyMoney(buyMoney);
        list.add(oneRecord);
        return list;
    }


    public List<BuyRecord3DVO> hsBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split(",|，");
        boolean hasError = false;
        if(numArr.length==0){
            hasError = true;
        }
        String[] validHzArr = {"0","1","2","3","4","5","6","7","8","9","10","11",
                "12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27"};
        List<String> validHzList = Arrays.asList(validHzArr);
        List<String> hzList = Arrays.asList(numArr);
        for(String hz : hzList){
            if(!validHzList.contains(hz)){
                hasError = true;
                break;
            }
        }
        if(hasError){
//            ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//            toMsg.setSource(0);
//            dataDao.insert(toMsg);
//            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//            }
            return null;
        }

        String huizongName = hzList.size()==1?"和数单式":"和数复式";
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(huizongName);
        oneRecord.setBuyAmount(hzList.size());
        oneRecord.setHzList(hzList.stream().collect(Collectors.toList()));
        oneRecord.setValue(hzList.stream().collect(Collectors.joining(",")));
        oneRecord.setLmId("5");
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyMoney(buyMoney);
        list.add(oneRecord);
        return list;
    }


    public List<BuyRecord3DVO> kdBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split(",|，");
        boolean hasError = false;
        if(numArr.length==0){
            hasError = true;
        }
        String[] validHzArr = {"0","1","2","3","4","5","6","7","8","9"};
        List<String> validHzList = Arrays.asList(validHzArr);
        List<String> hzList = Arrays.asList(numArr);
        for(String hz : hzList){
            if(!validHzList.contains(hz)){
                hasError = true;
                break;
            }
        }
        if(hasError){
            return null;
        }

        String huizongName = "跨度";
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(huizongName);
        oneRecord.setBuyAmount(hzList.size());
        oneRecord.setHzList(hzList.stream().collect(Collectors.toList()));
        oneRecord.setValue(hzList.stream().collect(Collectors.joining(",")));
        oneRecord.setLmId(lmId);
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyMoney(buyMoney);
        list.add(oneRecord);
        return list;
    }


    //1D
    public List<BuyRecord3DVO> ding1Buy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split(",|，");
        String bai = "";
        String shi = "";
        String ge = "";
        boolean hasError = false;
        if(numArr.length!=3){
            hasError = true;
        }else{
            bai = numArr[0];
            shi = numArr[1];
            ge = numArr[2];
            if(StringUtil.isNotNull(bai) && !"-".equals(bai)){
                Set<String> codeSet = Arrays.asList(bai.split("")).stream().collect(Collectors.toSet());
                bai = codeSet.stream().collect(Collectors.joining()).replace("-","");
            }
            if(StringUtil.isNotNull(shi) && !"-".equals(shi)){
                Set<String> codeSet = Arrays.asList(shi.split("")).stream().collect(Collectors.toSet());
                shi = codeSet.stream().collect(Collectors.joining()).replace("-","");;
            }
            if(StringUtil.isNotNull(ge) && !"-".equals(ge)){
                Set<String> codeSet = Arrays.asList(ge.split("")).stream().collect(Collectors.toSet());
                ge = codeSet.stream().collect(Collectors.joining()).replace("-","");;
            }
            if("-".equals(bai) && "-".equals(shi) && "-".equals(ge)){
                hasError = true;
            }
        }
        if(hasError){
//            ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//            toMsg.setSource(0);
//            dataDao.insert(toMsg);
//            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//            }
            return null;
        }

        if(bai.length()>0) {
            int size = bai.length();
            String hzname = size == 1 ? "1D单式": "1D复式";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBuyAmount(size);
            oneRecord.setValue(bai + ",-,-");
            oneRecord.setBai(bai);
            oneRecord.setShi("-");
            oneRecord.setGe("-");
            oneRecord.setLmId("6");
            oneRecord.setLsTypeId("1");
            oneRecord.setBuyMoney(buyMoney);
            list.add(oneRecord);
        }

        if(shi.length()>0) {
            int size = shi.length();
            String hzname = size == 1 ? "1D单式": "1D复式";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBuyAmount(size);
            oneRecord.setValue("-,"+shi + ",-");
            oneRecord.setBai("-");
            oneRecord.setShi(shi);
            oneRecord.setGe("-");
            oneRecord.setLmId("6");
            oneRecord.setLsTypeId("1");
            oneRecord.setBuyMoney(buyMoney);
            list.add(oneRecord);
        }

        if(ge.length()>0) {
            int size = ge.length();
            String hzname = size == 1 ? "1D单式": "1D复式";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBuyAmount(size);
            oneRecord.setValue("-,-,"+ge);
            oneRecord.setBai("-");
            oneRecord.setShi("-");
            oneRecord.setGe(ge);
            oneRecord.setLmId("6");
            oneRecord.setLsTypeId("1");
            oneRecord.setBuyMoney(buyMoney);
            list.add(oneRecord);
        }
        return list;
    }


    //猜1D
    public List<BuyRecord3DVO> c1dBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split(",|，");
        boolean hasError = false;
        String bai = "";
        if(numArr.length==0){
            hasError = true;
        }else{
            Set<String> codeSet = Arrays.asList(numArr).stream().collect(Collectors.toSet());
            bai = codeSet.stream().collect(Collectors.joining()).replace("-","");
            codeSet = Arrays.asList(bai.split("")).stream().collect(Collectors.toSet());
            bai = codeSet.stream().collect(Collectors.joining()).replace("-","");
        }
        if(hasError){
//            ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//            toMsg.setSource(0);
//            dataDao.insert(toMsg);
//            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//            }
            return null;
        }

        int size = bai.length();
        String hzname = size == 1 ? "猜1D单式" : "猜1D复式";
        String value = bai;
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBuyAmount(size);
        oneRecord.setValue(value);
        oneRecord.setBuyCodes(value);
        oneRecord.setBuyCode(value);
        oneRecord.setLmId("6");
        oneRecord.setLsTypeId("2");
        oneRecord.setBuyMoney(buyMoney);
        list.add(oneRecord);
        return list;
    }



    //2D
    public List<BuyRecord3DVO> ding2Buy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split(",|，");
        String bai = "";
        String shi = "";
        String ge = "";
        boolean hasError = false;
        if(numArr.length!=3){
            hasError = true;
        }else{
            bai = numArr[0];
            shi = numArr[1];
            ge = numArr[2];
            if(StringUtil.isNotNull(bai) && !"-".equals(bai)){
                Set<String> codeSet = Arrays.asList(bai.split("")).stream().collect(Collectors.toSet());
                bai = codeSet.stream().collect(Collectors.joining()).replace("-","");
            }
            if(StringUtil.isNotNull(shi) && !"-".equals(shi)){
                Set<String> codeSet = Arrays.asList(shi.split("")).stream().collect(Collectors.toSet());
                shi = codeSet.stream().collect(Collectors.joining()).replace("-","");;
            }
            if(StringUtil.isNotNull(ge) && !"-".equals(ge)){
                Set<String> codeSet = Arrays.asList(ge.split("")).stream().collect(Collectors.toSet());
                ge = codeSet.stream().collect(Collectors.joining()).replace("-","");;
            }
            if("-".equals(bai) && "-".equals(shi) ){
                hasError = true;
            }else if("-".equals(bai) && "-".equals(ge)){
                hasError = true;
            }else if("-".equals(shi) && "-".equals(ge)){
                hasError = true;
            }
        }
        if(hasError){
//            ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//            toMsg.setSource(0);
//            dataDao.insert(toMsg);
//            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//            }
            return null;
        }

        List<String> codeList = Code3DCreateUtils.ding2Code(bai,shi,ge);
        String hzname = codeList.size() == 1 ? "2D单式" : "2D复式";

        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBai(bai);
        oneRecord.setShi(shi);
        oneRecord.setGe(ge);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setValue(bai+","+shi+","+ge);
        oneRecord.setLmId("7");
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyMoney(buyMoney);
        list.add(oneRecord);
        return list;
    }


    //猜2D
    public List<BuyRecord3DVO> c2dBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split(",|，");
        boolean hasError = false;
        String bai = "";
        if(numArr.length==0){
            hasError = true;
        }else{
            Set<String> codeSet = Arrays.asList(numArr).stream().collect(Collectors.toSet());
            bai = codeSet.stream().collect(Collectors.joining()).replace("-","");
            codeSet = Arrays.asList(bai.split("")).stream().collect(Collectors.toSet());
            bai = codeSet.stream().collect(Collectors.joining()).replace("-","");
        }
        if(hasError){
//            ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//            toMsg.setSource(0);
//            dataDao.insert(toMsg);
//            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//            }
            return null;
        }

        String hzname = "猜2D单式";
        List<String> codeList = Code3DCreateUtils.c2dCode(bai);
        codeList.forEach(code-> {
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setValue(code);
            oneRecord.setBuyAmount(1);
            oneRecord.setBuyCode(code);
            oneRecord.setBuyCodes(code);
            oneRecord.setLmId("7");
            oneRecord.setLsTypeId("2");
            oneRecord.setBuyMoney(buyMoney);
            list.add(oneRecord);
        });
        return list;
    }



    //b3
    public List<BuyRecord3DVO> b3Buy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split(",|，");
        String bai = "";
        String shi = "";
        String ge = "";
        boolean hasError = false;
        if(numArr.length!=3){
            hasError = true;
        }else {
            bai = numArr[0];
            shi = numArr[1];
            ge = numArr[2];
            if (StringUtil.isNull(bai) || "-".equals(bai)) {
                hasError = true;
            } else if (StringUtil.isNull(shi) || "-".equals(shi)) {
                hasError = true;
            } else if (StringUtil.isNull(ge) || "-".equals(ge)) {
                hasError = true;
            } else {
                Set<String> codeSet = Arrays.asList(bai.split("")).stream().collect(Collectors.toSet());
                bai = codeSet.stream().collect(Collectors.joining()).replace("-", "");
                String[] baiArr = bai.split("");
                codeSet = Arrays.asList(shi.split("")).stream().collect(Collectors.toSet());
                shi = codeSet.stream().collect(Collectors.joining()).replace("-", "");
                codeSet = Arrays.asList(ge.split("")).stream().collect(Collectors.toSet());
                ge = codeSet.stream().collect(Collectors.joining()).replace("-", "");
                for(String b : baiArr){
                    if(shi.contains(b) && ge.contains(b)){
                        ge = ge.replace(b,"");
                    }
                }
            }
        }
        if(hasError){
//            ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//            toMsg.setSource(0);
//            dataDao.insert(toMsg);
//            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//            }
            return null;
        }
        List<String> codeList = Code3DCreateUtils.createB3Code(bai,shi,ge);
        codeList.forEach(code -> {
            String[] arr = code.split("");
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName("包选三单式");
            oneRecord.setBai(arr[0]);
            oneRecord.setShi(arr[1]);
            oneRecord.setGe(arr[2]);
            oneRecord.setBuyAmount(1);
            oneRecord.setBuyCode(code);
            oneRecord.setBuyCodes(code);
            oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
            oneRecord.setLmId("8");
            oneRecord.setLsTypeId("1");
            oneRecord.setBuyMoney(buyMoney);
            list.add(oneRecord);
        });
        return list;
    }


    //b6
    public List<BuyRecord3DVO> b6Buy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split(",|，");
        boolean hasError = false;
        String bai = "";
        String shi = "";
        String ge = "";
        if(numArr.length!=3){
            hasError = true;
        }else {
            bai = numArr[0];
            shi = numArr[1];
            ge = numArr[2];
            if (StringUtil.isNull(bai) || "-".equals(bai)) {
                hasError = true;
            } else if (StringUtil.isNull(shi) || "-".equals(shi)) {
                hasError = true;
            } else if (StringUtil.isNull(ge) || "-".equals(ge)) {
                hasError = true;
            } else {
                Set<String> codeSet = Arrays.asList(bai.split("")).stream().collect(Collectors.toSet());
                bai = codeSet.stream().collect(Collectors.joining()).replace("-", "");
                String[] baiArr = bai.split("");
                codeSet = Arrays.asList(shi.split("")).stream().collect(Collectors.toSet());
                shi = codeSet.stream().collect(Collectors.joining()).replace("-", "");

                codeSet = Arrays.asList(ge.split("")).stream().collect(Collectors.toSet());
                ge = codeSet.stream().collect(Collectors.joining()).replace("-", "");

                for(String b : baiArr){
                    if(shi.contains(b)){
                        shi = shi.replace(b,"");
                    }
                    if(ge.contains(b)){
                        ge = ge.replace(b,"");
                    }
                }
                String[] shiArr = shi.split("");
                for(String s : shiArr){
                    if(bai.contains(s)){
                        bai = bai.replace(s,"");
                    }
                    if(ge.contains(s)){
                        ge = ge.replace(s,"");
                    }
                }
            }
        }
        if(hasError){
//            ChatRoomMsg toMsg = createMsg(botUser,player,"号码错误");
//            toMsg.setSource(0);
//            dataDao.insert(toMsg);
//            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
//            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
//            }
            return null;
        }

        List<String> codeList = Code3DCreateUtils.createB6Code(bai,shi,ge);
        codeList.forEach(code-> {
            String[] arr = code.split("");
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName("包选六单式");
            oneRecord.setBai(arr[0]);
            oneRecord.setShi(arr[1]);
            oneRecord.setGe(arr[2]);
            oneRecord.setBuyAmount(1);
            oneRecord.setBuyCode(code);
            oneRecord.setBuyCodes(code);
            oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
            oneRecord.setLmId("8");
            oneRecord.setLsTypeId("2");
            oneRecord.setBuyMoney(buyMoney);
            list.add(oneRecord);
        });
        return list;
    }


    /**
     * 适合关键词： 3D佰123拾234个456
     * @param botUser
     * @param player
     * @param buyMoney
     * @param codeRule
     * @param lmId
     * @return
     */
    public List<BuyRecord3DVO> D3Buy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        if(!codeRule.contains("百") || !codeRule.contains("十") || !codeRule.contains("个") ){
            return null;
        }
        String[] numArr = codeRule.split("百|十|个");
        boolean hasError = false;
        String bai = "";
        String shi = "";
        String ge = "";
        if(numArr.length!=4){
            hasError = true;
        }else {
            bai = numArr[1];
            shi = numArr[2];
            ge = numArr[3];
            if (StringUtil.isNull(bai)) {
                hasError = true;
            } else if (StringUtil.isNull(shi)) {
                hasError = true;
            } else if (StringUtil.isNull(ge)) {
                hasError = true;
            } else {
                Set<String> codeSet = Arrays.asList(bai.split("")).stream().collect(Collectors.toSet());
                bai = codeSet.stream().collect(Collectors.joining()).replace("-", "");
                codeSet = Arrays.asList(shi.split("")).stream().collect(Collectors.toSet());
                shi = codeSet.stream().collect(Collectors.joining()).replace("-", "");
                codeSet = Arrays.asList(ge.split("")).stream().collect(Collectors.toSet());
                ge = codeSet.stream().collect(Collectors.joining()).replace("-", "");
            }
        }
        if(hasError){
            return null;
        }

        String hzname = "";
        int sum = bai.length() * shi.length() * ge.length();
        if(sum==1){
            hzname = "直选单式";
        }else{
            hzname = "直选复式";
        }
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBai(bai);
        oneRecord.setShi(shi);
        oneRecord.setGe(ge);
        oneRecord.setBuyAmount(sum);
        oneRecord.setValue(bai+","+shi+","+ge);
        oneRecord.setLmId(lmId);
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyMoney(buyMoney);
        list.add(oneRecord);
        return list;
    }



    public List<BuyRecord3DVO> noCodesBuy(BotUser botUser,Player player,BigDecimal buyMoney,String codeRule,String lmId) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        BuyRecord3DVO oneRecord = null;
        switch(lmId){
            case "12":
                oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("拖拉机单式");
                oneRecord.setBuyAmount(1);
                oneRecord.setValue("拖拉机");
                oneRecord.setLmId(lmId);
                oneRecord.setLsTypeId("1");
                oneRecord.setBuyMoney(buyMoney);
                recordList.add(oneRecord);
                break;
            case "11":
                oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("猜三同单式");
                oneRecord.setBuyAmount(1);
                oneRecord.setValue("三同号");
                oneRecord.setLmId(lmId);
                oneRecord.setLsTypeId("1");
                oneRecord.setBuyMoney(buyMoney);
                recordList.add(oneRecord);
                break;
            case "10":
                if(codeRule.equals("奇偶")){
                    oneRecord = new BuyRecord3DVO();
                    oneRecord.setHuizongName("奇偶单式");
                    oneRecord.setBuyAmount(1);
                    oneRecord.setValue("奇");
                    oneRecord.setLmId(lmId);
                    oneRecord.setLsTypeId("1");
                    oneRecord.setBuyMoney(buyMoney);
                    recordList.add(oneRecord);
                    oneRecord = new BuyRecord3DVO();
                    oneRecord.setHuizongName("奇偶单式");
                    oneRecord.setBuyAmount(1);
                    oneRecord.setValue("偶");
                    oneRecord.setLmId(lmId);
                    oneRecord.setLsTypeId("1");
                    oneRecord.setBuyMoney(buyMoney);
                    recordList.add(oneRecord);
                }else{
                    oneRecord = new BuyRecord3DVO();
                    oneRecord.setHuizongName("奇偶单式");
                    oneRecord.setBuyAmount(1);
                    oneRecord.setValue(codeRule);
                    oneRecord.setLmId(lmId);
                    oneRecord.setLsTypeId("1");
                    oneRecord.setBuyMoney(buyMoney);
                    recordList.add(oneRecord);
                }
                break;
            case "9":
                if(codeRule.equals("大小")){
                    oneRecord = new BuyRecord3DVO();
                    oneRecord.setHuizongName("大小单式");
                    oneRecord.setBuyAmount(1);
                    oneRecord.setValue("大");
                    oneRecord.setLmId(lmId);
                    oneRecord.setLsTypeId("1");
                    oneRecord.setBuyMoney(buyMoney);
                    recordList.add(oneRecord);
                    oneRecord = new BuyRecord3DVO();
                    oneRecord.setHuizongName("大小单式");
                    oneRecord.setBuyAmount(1);
                    oneRecord.setValue("小");
                    oneRecord.setLmId(lmId);
                    oneRecord.setLsTypeId("1");
                    oneRecord.setBuyMoney(buyMoney);
                    recordList.add(oneRecord);
                }else{
                    oneRecord = new BuyRecord3DVO();
                    oneRecord.setHuizongName("大小单式");
                    oneRecord.setBuyAmount(1);
                    oneRecord.setValue(codeRule);
                    oneRecord.setLmId(lmId);
                    oneRecord.setLsTypeId("1");
                    oneRecord.setBuyMoney(buyMoney);
                    recordList.add(oneRecord);
                }

                break;
        }
        return recordList;
    }




}
