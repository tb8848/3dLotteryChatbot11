package com.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import com.GlobalConst;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.beans.*;
import com.config.RequestDataHelper;
import com.dao.ChatRoomMsgDAO;
import com.dao.DrawBuyRecordDAO;
import com.google.common.collect.Maps;
import com.mysql.jdbc.StringUtils;
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
public class KuaidaBuyMsgServiceV2 {


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
    private BuyRecord3DForTextService buyRecord3DForTextService;

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

    //快打下注，固定文本格式
    public void kuaidaBuy(ChatRoomMsg fromMsg, BotUser botUser, Player player,Integer lotteryType, List<String> msgId, List<Integer> xzCount, List<BigDecimal> xzPoints){
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
//                    if (player.getChatStatus() == 1){
//                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                    }else{
//                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                }
                return;
            }
            String userId = botUser.getId();
//            System.out.println(">>>>>>>>>>>>>>有下注消息来了");
            String errmsg = null;
            List<BuyRecord3DVO> buyList = Lists.newArrayList();
            Map<String,Object> resMap = Maps.newHashMap();


            String content = "";
            if(fromMsg.getMsg().startsWith("P3") || fromMsg.getMsg().startsWith("3D")){
                content = fromMsg.getMsg().substring(2);
            }else{
                if (fromMsg.getMsg().startsWith("福") || fromMsg.getMsg().startsWith("体")) {
                    content = fromMsg.getMsg().substring(1);
                }else {
                    content = fromMsg.getMsg().replaceAll("福", "");
                    content = content.replaceAll("体", "");
                }
            }
            String[] arr = content.split("各");
            if (arr.length != 2) {
                toMsg = getErrorMsg(botUser, player);
                toMsg.setSource(0);
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
//                    if (player.getChatStatus() == 1){
//                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                    }else{
//                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                }
                return;
            }
            BigDecimal buyMoney = BigDecimal.ZERO;
            try {
                int len = arr[1].length();
                if (arr[1].contains("笔")){
                    String arrStr = arr[1].substring(0,len-1);
                    buyMoney = new BigDecimal(arrStr);
                }else{
                    buyMoney = new BigDecimal(arr[1]);
                }
            } catch (Exception e) {
                toMsg = createMsg(botUser, player, content+"\r\n金额错误");
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                    //wechatIpadTokenService.sendMsgToFriend(toMsg);
//                    if (player.getChatStatus() == 1){
//                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                    }else{
//                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                }
                return;
            }
            String text = arr[0].toUpperCase();
            if (text.equals("拖拉机") || text.equals("三同号") || text.equals("猜三同") || text.equals("大") || text.equals("小") ||
                    text.equals("奇") || text.equals("偶") || text.equals("大小") || text.equals("奇偶") || text.equals("豹子")) {
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
                    case "豹子":
                    case "猜三同":
                    case "三同号":
                        buyList = noCodesBuy(botUser, player, buyMoney, arr[0], "11");
                        break;

                }
                if (null != buyList && buyList.size()>0) {
                    xiazhu(botUser, player, fromMsg, buyList,lotteryType,draw, msgId, xzCount, xzPoints);
                }else{
                    toMsg = createMsg(botUser, player, "作业内容不符合【"+text+"】的要求");
                    dataDao.insert(toMsg);
                    simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                    if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
//                        if (player.getChatStatus() == 1){
//                            wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                        }else{
//                            wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                        }
//                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                    }
                }
            } else {

                boolean matchSucc = false;
                for (String word : GlobalConst.keywords1) {
                    if (text.startsWith(word) || text.contains(word)) {
                        String type = word;
                        String code = null;
                        matchSucc = true;

                        if (!type.equals(".") && !type.equals(",") && !type.equals("，") && !type.equals("XX") && !type.equals("**") &&
                                !type.equals("X") && !type.equals("*")){
                            text = text.replaceAll(word, "");
                            text = word + text;
                        }
                        String[] typeArr = new String[]{};
                        if (type.equals("**")){
                            typeArr = text.split("\\*\\*");
                        }else if (type.equals("*")){
                            typeArr = text.split("\\*");
                        }else{
                            typeArr = text.split(word);
                        }
                        if (typeArr.length != 2 && (!type.equals(".") && !type.equals(",") && !type.equals("，") && !type.equals("XX") &&
                                !type.equals("**") && !type.equals("X") && !type.equals("*"))) {
                            toMsg = getErrorMsg(botUser, player);
                            toMsg.setSource(0);
                            dataDao.insert(toMsg);
                            simpMessagingTemplate.convertAndSend("/topic/room/" + userId, toMsg);
                            if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                                //wechatIpadTokenService.sendMsgToFriend(toMsg);
//                                if (player.getChatStatus() == 1){
//                                    wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                                }else{
//                                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                                }
//                                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                            }
                            return;
                        }
                        if (text.contains("百") && text.contains("十") && !text.contains("直") && !text.contains("一定") && !text.contains("二定") &&
                                !text.contains("1D") && !text.contains("2D") && !text.contains("三定")){
                            code=text;
                        }else if (type.equals(".") || type.equals(",") || type.equals("，") || type.equals("XX") || type.equals("**") ||
                                type.equals("X") || type.equals("*")){
                            code=text;
                        }else{
                            code=typeArr[1];
                        }
                        String[] splitArr = code.split("\\.|,|，|/| |-|。|、");
                        for(String r : splitArr){
                            if(StringUtils.isNullOrEmpty(r)){

                            }else{
                                if(r.equals("全包")){
                                    continue;
                                }
                                if(!StringUtil.checkCodeFormat(r) && (!type.equals(".") && !type.equals(",") && !type.equals("，") && !type.equals("XX") &&
                                        !type.equals("**") && !type.equals("X") && !type.equals("*") && !type.equals("2D") && !type.equals("二定") && !type.equals("直组"))){
                                    toMsg = createMsg(botUser, player, content+"\r\n号码格式错误");
                                    toMsg.setSource(0);
                                    dataDao.insert(toMsg);
                                    simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                                    if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
//                                        if (player.getChatStatus() == 1){
//                                            wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                                        }else{
//                                            wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                                        }
//                                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                                    }
                                    return;
                                }else if (type.equals("双飞") && r.length() != 2){
                                    toMsg = createMsg(botUser, player, content+"\r\n号码格式错误");
                                    toMsg.setSource(0);
                                    dataDao.insert(toMsg);
                                    simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                                    if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {

                                    }
                                    return;
                                }
                            }
                        }

                        if(!code.contains(",") && !code.contains("，") && !code.contains(".") && !code.contains("XX") && !code.contains("**") &&
                                !code.contains("X") && !code.contains("*") && !type.equals("直组") && !StringUtil.checkCodeFormat(code) &&
                                !code.contains(" ") && !code.contains("/") && !code.contains("-") && !code.contains("。") && !code.contains("、")){
                                toMsg = createMsg(botUser, player, content+"\r\n号码格式错误");
                                toMsg.setSource(0);
                                dataDao.insert(toMsg);
                                simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
//                                    if (player.getChatStatus() == 1){
//                                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                                    }else{
//                                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                                    }
//                                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                                }
                                return;
                        }
                        switch (type) {
                            case "直":
                            case "单":
                            case "单选":
                                resMap = codeBuy(botUser, player, buyMoney, code, "1");
                                break;
                            case "三定":
                            case "直选":
                            case "普通直选":
                            case "直选普通":
                                resMap = zxtxBuy(botUser, player, buyMoney, code, "1");
                                break;
//                            case "和值直选":
//                            case "直选和值":
//                                resMap = zxhzBuy(botUser, player, buyMoney, code, "1");
//                                break;
                            case "通选":
                            case "复":
                            case "复式":
                                resMap = fsBaozuBuy(buyMoney, code, "300");
                                break;
                            case "分笔复式":
                                resMap = fsBuy(botUser, player, buyMoney, code, "2");
                                break;
                            case "分笔组三":
                                resMap = z3Buy(botUser, player, buyMoney, code, "3");
                                break;
                            case "组6组3":
                            case "组3组6":
                            case "组六组三":
                            case "组三组六":
                                Map<String,Object> zu3 = Maps.newHashMap();
                                if(code.contains("拖")){
                                    zu3 = z3dtBuy(botUser, player, buyMoney, code, "3");
                                }else{
                                    zu3 = z3BaozuBuy(buyMoney, code, "100");
                                }
                                Map<String,Object> zu6 = Maps.newHashMap();
                                if(code.contains("拖")){
                                    zu6 = z6dtBuy(botUser, player, buyMoney, code, "4");
                                }else{
                                    zu6 = z6BaozuBuy(buyMoney, code, "200");
                                }
                                List<BuyRecord3DVO> zu3List = (List<BuyRecord3DVO>) zu3.get("list");
                                List<BuyRecord3DVO> zu6List = (List<BuyRecord3DVO>) zu6.get("list");
                                zu3List.addAll(zu6List);
                                resMap.put("list",zu3List);
                                break;
                            case "组":
                                String[] multiArrr = code.split("\\.|,|，|/| |-|。|、");
                                List<BuyRecord3DVO> hmLists = Lists.newArrayList();
                                for(String ss : multiArrr) {
                                    if (StringUtil.isNull(ss)) {
                                        continue;
                                    }
                                    if (StringUtil.hasDuplicateChar(ss)) {
                                        Map<String,Object> z3Map = Maps.newHashMap();
                                        z3Map = zzBuy(buyMoney, ss, "3");
                                        List<BuyRecord3DVO> z3Lists = (List<BuyRecord3DVO>) z3Map.get("list");
                                        hmLists.addAll(z3Lists);
                                    }else{
                                        Map<String,Object> z6Map = Maps.newHashMap();
                                        z6Map = z6Buy(botUser, player, buyMoney, ss, "4");
                                        List<BuyRecord3DVO> z6Lists = (List<BuyRecord3DVO>) z6Map.get("list");
                                        hmLists.addAll(z6Lists);
                                    }
                                }
                                resMap.put("list",hmLists);
                                break;
                            case "直组":
                                String[] multiArr = code.split("\\.|,|，|/| |-|。|、");
                                List<BuyRecord3DVO> hmList = Lists.newArrayList();
                                for(String ss : multiArr){
                                    if (StringUtil.hasDuplicateChar(ss)){
                                        Map<String,Object> zx = codeBuy(botUser, player, buyMoney, ss, "1");
                                        Map<String,Object> z3 = Maps.newHashMap();
                                        if(ss.contains("拖")){
                                            z3 = z3dtBuy(botUser, player, buyMoney, ss, "3");
                                        }else{
//                                            z3 = z3Buy(botUser, player, buyMoney, ss, "3");
                                            z3 = zzBuy(buyMoney, ss, "3");
                                        }
                                        List<BuyRecord3DVO> zxList = (List<BuyRecord3DVO>) zx.get("list");
                                        List<BuyRecord3DVO> z3List = (List<BuyRecord3DVO>) z3.get("list");
                                        hmList.addAll(zxList);
                                        hmList.addAll(z3List);
                                    }else{
                                        Map<String,Object> zx = codeBuy(botUser, player, buyMoney, ss, "1");
                                        Map<String,Object> z6 = Maps.newHashMap();
                                        if(ss.contains("拖")){
                                            z6 = z6dtBuy(botUser, player, buyMoney, ss, "4");
                                        }else{
                                            z6 = z6BaozuBuy(buyMoney, ss, "200");
                                        }
                                        List<BuyRecord3DVO> zxList = (List<BuyRecord3DVO>) zx.get("list");
                                        List<BuyRecord3DVO> z6List = (List<BuyRecord3DVO>) z6.get("list");
                                        hmList.addAll(zxList);
                                        hmList.addAll(z6List);
                                    }
                                }
                                resMap.put("list",hmList);
                                break;
                            case "组3":
                            case "组三":
                            case "组三普通":
//                                if(code.contains(",") || code.contains("，")){
//                                    resMap = z3Z6OneCode(botUser, player, buyMoney, code, "3");
//                                }else
                                if(code.contains("拖")){
                                    resMap = z3dtBuy(botUser, player, buyMoney, code, "3");
                                }else{
                                    if (arr[1].contains("笔")){
                                        resMap = z3Buy(botUser, player, buyMoney, code, "3");
                                    }else{
                                        if (StringUtil.hasDuplicateChar(code) && code.length() == 3){
                                            resMap = z3BaozuBuy(buyMoney, code, "3");
                                        }else{
                                            resMap = z3BaozuBuy(buyMoney, code, "100");
                                        }
                                    }
                                }
                                break;
                            case "组3飞":
                            case "组三飞":
                            case "双飞组三":
                                resMap = z3SFBuy(botUser, player, buyMoney, code, "3");
                                break;
//                            case "和值组三":
//                            case "组三和值":
//                                resMap = z3hzBuy(botUser, player, buyMoney, code, "3");
//                                break;
                            case "分笔组六":
                                resMap = z6Buy(botUser, player, buyMoney, code, "4");
                                break;
                            case "组6":
                            case "组六":
                            case "组六普通":
//                                if(code.contains(",") || code.contains("，")){
//                                    resMap = z3Z6OneCode(botUser, player, buyMoney, code, "4");
//                                }else
                                if(code.contains("拖")){
                                    resMap = z6dtBuy(botUser, player, buyMoney, code, "4");
                                }else{
                                    if (arr[1].contains("笔")){
                                        resMap = z6Buy(botUser, player, buyMoney, code, "4");
                                    }else{
                                        resMap = z6BaozuBuy(buyMoney, code, "200");
                                    }
                                }
                                break;
                            case "组6飞":
                            case "组六飞":
                            case "双飞组六":
                                resMap = z6SFBuy(botUser, player, buyMoney, code, "4");
                                break;
//                            case "和值组六":
//                            case "组六和值":
//                                resMap = z6hzBuy(botUser, player, buyMoney, code, "4");
//                                break;
                            case "飞":
                            case "双飞":
                                List<BuyRecord3DVO> sfList = Lists.newArrayList();
                                String[] mulArr = code.split("\\.|,|，|/| |-|。|、");
                                for(String ss : mulArr){
                                    if (StringUtil.hasDuplicateChar(ss)){
                                        Map<String,Object> sf = z3SFBuyNew(botUser, player, buyMoney, ss, "3");
                                        List<BuyRecord3DVO> list = (List<BuyRecord3DVO>) sf.get("list");
                                        sfList.addAll(list);
                                    }else{
                                        Map<String,Object> sf = z6SFBuyNew(botUser, player, buyMoney, ss, "4");
                                        List<BuyRecord3DVO> list = (List<BuyRecord3DVO>) sf.get("list");
                                        sfList.addAll(list);
                                    }
                                }
                                resMap.put("list",sfList);
                                break;
                            case "和":
                            case "和数":
                                resMap = hsBuy(botUser, player, buyMoney, code, "5");
                                break;
                            case "跨":
                            case "跨度":
                                resMap = kdBuy(botUser, player, buyMoney, code, "13");
                                break;
                            case "独":
                            case "胆":
                            case "独胆":
                                resMap = ddBuy(botUser, player, buyMoney, code, "14");
                                break;
                            case "一定":
                            case "XX":
                            case "**":
                            case "1D":
                                resMap = ding1Buy(botUser, player, buyMoney, code, "6");
                                break;
//                            case "猜1D":
//                                resMap = c1dBuy(botUser, player, buyMoney, code, "6");
//                                break;
                            case "二定":
                            case "X":
                            case "*":
                            case "2D":
                                resMap = ding2Buy(botUser, player, buyMoney, code, "7");
                                break;
//                            case "猜2D":
//                                resMap = c2dBuy(botUser, player, buyMoney, code, "7");
//                                break;
//                            case "包选三":
//                                resMap = b3Buy(botUser, player, buyMoney, code, "8");
//                                break;
//                            case "包选六":
//                                resMap = b6Buy(botUser, player, buyMoney, code, "8");
//                                break;
                            case "百":
                                if (code.contains("十") && code.contains("个")){
                                    resMap = zxtxBuy(botUser, player, buyMoney, code, "1");
                                }else if (code.contains("十")){
                                    resMap = ding2Buy(botUser, player, buyMoney, code, "7");
                                }
                                break;
                            case ".":
                            case ",":
                            case "，":
                                resMap = codeBuy(botUser, player, buyMoney, code, "1");
                                break;
                            default:
                                toMsg = createMsg(botUser, player, "没有此玩法类别："+type);
                                toMsg.setSource(0);
                                dataDao.insert(toMsg);
                                simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
//                                    if (player.getChatStatus() == 1){
//                                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                                    }else{
//                                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                                    }
//                                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                                }
                                return;
                        }
                        if(resMap.containsKey("list")){
                            buyList = (List<BuyRecord3DVO>) resMap.get("list");
                        }else{
                            errmsg = (String)resMap.get("errmsg");
                        }

                        if(StringUtil.isNotNull(errmsg)){
                            toMsg = createMsg(botUser, player, content+"\r\n"+errmsg);
                            toMsg.setSource(0);
                            dataDao.insert(toMsg);
                            simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                            if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
//                                if (player.getChatStatus() == 1){
//                                    wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                                }else{
//                                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                                }
//                                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                            }
                        }else{
                            if (null != buyList && buyList.size() > 0) {
                                xiazhu(botUser, player, fromMsg, buyList,lotteryType,draw, msgId, xzCount, xzPoints);
                            }else{
                                toMsg = createMsg(botUser, player, content+"\r\n作业内容不符合【"+type+"】的要求");
                                toMsg.setSource(0);
                                dataDao.insert(toMsg);
                                simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
//                                    if (player.getChatStatus() == 1){
//                                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                                    }else{
//                                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                                    }
//                                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                                }
                            }
                        }
                        break;
                    }
                }

                if(!matchSucc){
                    toMsg = createMsg(botUser, player, content+"\r\n类别格式错误");
                    toMsg.setSource(0);
                    dataDao.insert(toMsg);
                    simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                    if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
//                        if (player.getChatStatus() == 1){
//                            wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                        }else{
//                            wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                        }
//                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
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
//                if (player.getChatStatus() == 1){
//                    wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                }else{
//                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                }
//                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
            }
        }
    }



    //复式包组
    public Map<String,Object> fsBaozuBuy(BigDecimal buyMoney,String codeRule,String lmId){
        List<LotterySetting> lsList = lotterySettingService.getListBy(lmId);
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] multiArr = codeRule.split("\\.|,|，|/| |-|。|、");
        for(String ss : multiArr){
            if(StringUtil.isNull(ss) || StringUtils.isNullOrEmpty(ss)){
                continue;
            }
            String newValue  = ss;
            if(ss.equals("全包")){
                ss = "0123456789";
            }
            //删除数字之外的任何字符
            newValue  = ss.replaceAll("[^0-9]","");
            String[] numArr = newValue.split("");
            //删除重复的数字
            Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
            if(nums.size()<3){
                resMap.put("errmsg","复式号码数量必须大于2:"+codeRule);
                return resMap;
            }
            String bai = nums.stream().collect(Collectors.joining());
            String lsName = "复式"+ StringUtil.changeDigitToChinese(bai.length(),"码");

            LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
            if(null == ls){
                resMap.put("errmsg","复式号码格式错误:"+codeRule);
                return resMap;
            }
            String buyDesc = lsName+"(组):"+bai;
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(buyDesc);
            oneRecord.setBuyCode(bai);
            oneRecord.setBuyAmount(1);
            oneRecord.setValue(bai);
            oneRecord.setLmId(lmId);
            oneRecord.setLsTypeId(ls.getTypeId()+"");
            oneRecord.setBuyMoney(buyMoney);
            oneRecord.setBuyDesc(buyDesc);
            oneRecord.setCodeList(Lists.newArrayList(bai));
            list.add(oneRecord);
        }
        resMap.put("list",list);
        return resMap;
    }


    //组三包组
    public Map<String,Object> z3BaozuBuy(BigDecimal buyMoney,String codeRule,String lmId){
        List<LotterySetting> lsList = lotterySettingService.getListBy(lmId);
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] multiArr = codeRule.split("\\.|,|，|/| |-|。|、");
        for(String ss : multiArr){
            if(StringUtil.isNull(ss) || StringUtils.isNullOrEmpty(ss)){
                continue;
            }
            String newValue  = ss;
            if(ss.equals("全包")){
                ss = "0123456789";
            }
            //删除数字之外的任何字符
            newValue  = ss.replaceAll("[^0-9]","");
            String[] numArr = newValue.split("");
            //删除重复的数字
            Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
            if(nums.size()<2){
                resMap.put("errmsg","组三号码数量必须大于2:"+codeRule);
                return resMap;
            }
            String bai = nums.stream().collect(Collectors.joining());
            String lsName = "组三"+ StringUtil.changeDigitToChinese(bai.length(),"码");

            LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
            if(null == ls){
                resMap.put("errmsg","组三号码格式错误:"+codeRule);
                return resMap;
            }
            String buyDesc = "";
            if (lmId.equals("3")){
                buyDesc = "组三组选"+"(组):"+newValue + "[" + 1 + "注]";
            }else{
                buyDesc = lsName+"(组):"+newValue + "[" + 1 + "注]";
            }
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(buyDesc);
            oneRecord.setBuyCode(newValue);
            oneRecord.setBuyAmount(1);
            oneRecord.setValue(newValue);
            oneRecord.setLmId(lmId);
            oneRecord.setLsTypeId(ls.getTypeId()+"");
            oneRecord.setBuyMoney(buyMoney);
            oneRecord.setBuyDesc(buyDesc);
            oneRecord.setCodeList(Lists.newArrayList(newValue));
            list.add(oneRecord);
        }
        resMap.put("list",list);
        return resMap;
    }


    //组三包组
    public Map<String,Object> z6BaozuBuy(BigDecimal buyMoney,String codeRule,String lmId){
        List<LotterySetting> lsList = lotterySettingService.getListBy(lmId);
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] multiArr = codeRule.split("\\.|,|，|/| |-|。|、");
        for(String ss : multiArr){
            if(StringUtil.isNull(ss) || StringUtils.isNullOrEmpty(ss)){
                continue;
            }
            String newValue  = ss;
            if(ss.equals("全包")){
                ss = "0123456789";
            }
            //删除数字之外的任何字符
            newValue  = ss.replaceAll("[^0-9]","");
            String[] numArr = newValue.split("");
            //删除重复的数字
            Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
            if(nums.size()<3){
                resMap.put("errmsg","组六号码数量必须大于3:"+codeRule);
                return resMap;
            }
            String bai = nums.stream().collect(Collectors.joining());
            String lsName = "组六"+ StringUtil.changeDigitToChinese(bai.length(),"码");

            LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
            if(null == ls){
                resMap.put("errmsg","组六号码格式错误:"+codeRule);
                return resMap;
            }
            String buyDesc = lsName+"(组):"+newValue + "[" + 1 + "注]";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(buyDesc);
            oneRecord.setBuyCode(newValue);
            oneRecord.setBuyAmount(1);
            oneRecord.setValue(newValue);
            oneRecord.setLmId(lmId);
            oneRecord.setLsTypeId(ls.getTypeId()+"");
            oneRecord.setBuyMoney(buyMoney);
            oneRecord.setBuyDesc(buyDesc);
            oneRecord.setCodeList(Lists.newArrayList(newValue));
            list.add(oneRecord);
        }
        resMap.put("list",list);
        return resMap;
    }


    //单选
    private Map<String,Object> codeBuy(BotUser botUser, Player player, BigDecimal buyMoney, String codeRule, String lmId) {
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] codeArr = codeRule.split("\\.|,|，|/| |-|。|、");
        if(codeArr.length==0){
            resMap.put("errmsg","号码格式错误");
            return resMap;
        }
        for(String code : codeArr){
            if(code.length()!=3){
                resMap.put("errmsg","单选号码["+code+"]错误");
                return resMap;
            }
            if (!code.matches("-?\\d+(\\.\\d+)?")){
                resMap.put("errmsg","号码格式错误");
                return resMap;
            }
        }

        List<String> codeList = Arrays.stream(codeArr).collect(Collectors.toList());
        String value = codeList.stream().collect(Collectors.joining(","));
        String hzname = "直选";
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setValue(value);
        oneRecord.setLmId(lmId);
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyMoney(buyMoney);
        oneRecord.setBuyDesc("直选："+value);
        oneRecord.setCodeList(codeList);
        list.add(oneRecord);
        resMap.put("list",list);
        return resMap;

    }





    public void reportToPan(BotUser botUser,Player player,ChatRoomMsg msg,List<BuyRecord3DVO> buyList,
                            BotUserPan botUserPan,Draw draw,Integer lotteryType,String lotteryName, List<String> msgId, List<Integer> xzCount, List<BigDecimal> xzPoints){
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
            reportRespData = reportToPanService.buyForText(reportToPanUrl,buyList,botUserPan.getLogin3dToken());
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
//                    if (player.getChatStatus() == 1){
//                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                    }else{
//                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                    }
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
//                    if (player.getChatStatus() == 1){
//                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                    }else{
//                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                    }
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
                    playerBuyRecord.setBuyDesc(msg.getMsg());
                    playerBuyRecord.setBuyFrom(0);
                    playerBuyRecord.setKuaixuanRule(msg.getKuaixuanRule());
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

                    if(drawBuyRecordDAO.batchAddBuyCode(dataList)>0){
                        playerBuyRecordService.save(playerBuyRecord);
                        playerService.updatePoint(player.getId(),totalPoints,false);
                        BigDecimal points = playerService.getPoints(player.getId());

                        xzCount.add(buyAmount);
                        xzPoints.add(totalPoints);

                        String newmsg = "["+lotteryName+"课号]"+draw.getDrawId()+"\r\n"+msg.getMsg()+"\r\n交作业成功√√\r\n【份数】："+buyAmount+"\r\n"
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
                        msgId.add(toMsg.getId());
                        //simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                        if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                            //wechatIpadTokenService.sendMsgToFriend(toMsg);
//                            if (player.getChatStatus() == 1){
//                                wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                            }else{
//                                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                            }
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
//                            if (player.getChatStatus() == 1){
//                                wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                            }else{
//                                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                            }
//                            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                        }
                    }
                }
                break;
        }

    }


    private void xiazhu(BotUser botUser, Player player,ChatRoomMsg fromMsg,
                        List<BuyRecord3DVO> buyList,
                        Integer lotteryType,Draw draw, List<String> msgId, List<Integer> xzCount, List<BigDecimal> xzPoints) {
        BigDecimal totalBuyPoints = buyList.stream().map(item->item.getBuyMoney().multiply(new BigDecimal(item.getBuyAmount()))).reduce(BigDecimal.ZERO,BigDecimal::add);
        if(totalBuyPoints.compareTo(player.getPoints())>0){
            //玩家积分不够
            ChatRoomMsg toMsg = createMsg(botUser,player,"面上不足");
            toMsg.setSource(0);
            dataDao.insert(toMsg);
            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                if (player.getChatStatus() == 1){
//                    wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                }else{
//                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                }
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

        if(player.getPretexting()==1 || player.getEatPrize()==1){
            String msg = fromMsg.getMsg();
            BotUserSetting botUserSetting = botUserSettingService.getByUserId(botUser.getId());
            String lmId = buyList.get(0).getLmId();
            Map<String,Object> buyResult = null;
//            System.out.println("=========本地下注====》"+JSON.toJSONString(buyList));
            if("5".equals(lmId) || "13".equals(lmId)){
                buyResult = buyRecord3DServiceV2.buy3dHs(player,buyList,draw,-2,null,msg,lotteryType);
            }else{
                buyResult = buyRecord3DForTextService.codesBatchBuy(player,buyList,draw,-2,null,msg,lotteryType);
            }
            if(buyResult.containsKey("playerBuyId")){
                String playerBuyId = (String)buyResult.get("playerBuyId");
                PlayerBuyRecord record = playerBuyRecordService.getById(playerBuyId);
                BigDecimal points = playerService.getPoints(player.getId());
                xzCount.add(record.getBuyAmount());
                xzPoints.add(record.getBuyPoints());
                String newmsg = "["+lotteryName+"课号]"+draw.getDrawId()+"\r\n"+msg+"\r\n交作业成功√√\r\n【份数】："+record.getBuyAmount()+"\r\n"
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
                msgId.add(toMsg.getId());
                //simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                    if (player.getChatStatus() == 1){
//                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                    }else{
//                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                }
            }else{
                String errmsg = (String)buyResult.get("errmsg");
                if(StringUtil.isNotNull(errmsg)){
                    ChatRoomMsg toMsg = createMsg(botUser,player,errmsg);
                    toMsg.setSource(0);
                    dataDao.insert(toMsg);
                    simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                    if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                        if (player.getChatStatus() == 1){
//                            wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                        }else{
//                            wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                        }
//                        wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                    }
                }
            }
        }else{
            if(player.getReportNet()==1){
                BotUserPan botUserPan = botUserPanService.getOneBy(botUser.getId(),lotteryType);
                if(null!=botUserPan && StringUtil.isNotNull(botUserPan.getLogin3dToken())){
                    //报网
                    reportToPan(botUser,player,fromMsg,buyList,botUserPan,draw,lotteryType,lotteryName, msgId, xzCount, xzPoints);
                }else{
                    ChatRoomMsg toMsg = createMsg(botUser,player,"机器人未登录"+lotteryName+"网盘");
                    toMsg.setSource(0);
                    dataDao.insert(toMsg);
                    simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                    if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
//                        if (player.getChatStatus() == 1){
//                            wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                        }else{
//                            wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                        }
//                        wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                    }
                }
            }
        }
    }


    public Map<String,Object> zxtxBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resultMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        if(codeRule.contains("百") && codeRule.contains("十") && codeRule.contains("个")){
            int shiIdx = codeRule.indexOf("十");
            int geIdx = codeRule.indexOf("个");
            int baiIdx = codeRule.indexOf("百");
            String bai = null;
            String shi = null;
            String ge = null;
            if(baiIdx>-1 && shiIdx>-1) bai = codeRule.substring(baiIdx+1,shiIdx);
            if(shiIdx>-1 && geIdx>-1)  shi = codeRule.substring(shiIdx+1,geIdx);
            if(geIdx>-1 && geIdx>-1) ge = codeRule.substring(geIdx+1);
            if(StringUtil.isNull(bai) || StringUtil.isNull(shi) || StringUtil.isNull(ge)){
                resultMap.put("errmsg","号码不能为空");
                return resultMap;
            }
            List<String> clist = Code3DCreateUtils.zxFushi(bai,shi,ge);
            String buyDesc = "直选"+codeRule;
            BuyRecord3DVO vo = new BuyRecord3DVO();
            vo.setBuyDesc(buyDesc);
            vo.setHuizongName(buyDesc);
            vo.setBai(bai);
            vo.setShi(shi);
            vo.setGe(ge);
            vo.setLmId(lmId);
            vo.setLsTypeId("1");
            vo.setValue(bai+","+shi+","+ge);
            vo.setBuyAmount(clist.size());
            vo.setCodeList(clist);
            vo.setBuyMoney(buyMoney);
            list.add(vo);
            resultMap.put("list",list);
            return resultMap;
        }
        resultMap.put("errmsg","号码格式错误："+codeRule);
        return resultMap;
    }


    //复式
    public Map<String,Object> fsBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        //删除数字之外的任何字符
        //codeRule = codeRule.replace("/[^[0-9]/g","");
        String newValue  = codeRule.replaceAll("[^0-9]","");
        String[] numArr = newValue.split("");
        //删除重复的数字
        Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
        if(nums.size()<3){
            resMap.put("errmsg","复式号码数量必须大于2:"+codeRule);
            return resMap;
        }

        String bai = nums.stream().collect(Collectors.joining());
        String shi = bai;
        String ge = bai;
        String lsName = "复式"+StringUtil.changeDigitToChinese(bai.length(),"码");
        List<LotterySetting> lsList = lotterySettingService.getListBy(lmId);
        LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
        if(null == ls){
            resMap.put("errmsg","复式号码格式错误:"+codeRule);
            return resMap;
        }
        List<String> codeList = Code3DCreateUtils.zxFushi(bai,shi,ge);
        String buyDesc = lsName+":"+bai;
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(buyDesc);
        oneRecord.setBai(bai);
        oneRecord.setShi(shi);
        oneRecord.setGe(ge);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setValue(bai+","+shi+","+ge);
        oneRecord.setLmId(lmId);
        oneRecord.setLsTypeId(String.valueOf(ls.getTypeId()));
        oneRecord.setBuyMoney(buyMoney);
        oneRecord.setBuyDesc(buyDesc);
        list.add(oneRecord);
        resMap.put("list",list);
        return resMap;
    }



    public Map<String,Object> zxhzBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String newValue = codeRule.replaceAll("[^0-9,，]","");
        String[] hzArr = newValue.split("\\.|,|，|/| |-|。|、");
        Set<String> nums = Arrays.asList(hzArr).stream().collect(Collectors.toSet());
        List<String> hzList = Lists.newArrayList();
        List<String> valueList = Lists.newArrayList();
        for(int i=0;i<=27;i++){
            valueList.add(String.valueOf(i));
        }
        for(String hz :nums){
            if(valueList.contains(hz)){
                hzList.add(hz);
            }
        }
        if(hzList.isEmpty()){
            resMap.put("errmsg","未找到直选和值号码");
            return resMap;
        }
        List<String> codeList = Code3DCreateUtils.hezhi(hzList);
        String value = hzList.stream().collect(Collectors.joining(","));
        String hzname = "直选和值";
        String buyDesc = hzname+codeRule;
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(buyDesc);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setHzList(hzList);
        oneRecord.setValue(value);
        oneRecord.setLmId("1");
        oneRecord.setLsTypeId("2");
        oneRecord.setBuyMoney(buyMoney);
        oneRecord.setBuyDesc(buyDesc);
        list.add(oneRecord);
        resMap.put("list",list);
        return resMap;
    }


    //双飞组三
    public Map<String,Object> z3SFBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        boolean hasError = false;
        if (codeRule.contains(",") || codeRule.contains("，") || codeRule.contains(".") || codeRule.contains("/") || codeRule.contains(" ")) {
            String[] splitArr = codeRule.split("\\.|,|，|/| |-|。|、");
            int num = 0;
            for (String arr : splitArr) {
                //删除数字之外的任何字符
                //codeRule = codeRule.replace("/[^[0-9]/g","");
                String newValue = arr.replaceAll("[^0-9]", "");
                String[] numArr = newValue.split("");
                //删除重复的数字
                Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
                if (nums.size() != 2) {
                    resMap.put("errmsg", "只能选2个号码:" + codeRule);
                    return resMap;
                }
                String bai = nums.stream().collect(Collectors.joining());
                List<String> codeList = Code3DCreateUtils.z3SFCode(bai);
                num = num + codeList.size();
            }
            for (String arr : splitArr) {
                //删除数字之外的任何字符
                //codeRule = codeRule.replace("/[^[0-9]/g","");
                String newValue  = arr.replaceAll("[^0-9]","");
                String[] numArr = newValue.split("");
                //删除重复的数字
                Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
                if(nums.size()!=2){
                    resMap.put("errmsg","只能选2个号码:"+codeRule);
                    return resMap;
                }

                String bai = nums.stream().collect(Collectors.joining());
                List<String> codeList = Code3DCreateUtils.z3SFCode(bai);

                String buyDesc = "双飞组三"+codeRule + "[" + num + "注]";
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName(buyDesc);
                oneRecord.setBai(bai);
                oneRecord.setBuyAmount(codeList.size());
                oneRecord.setBuyCodes(bai);
                oneRecord.setValue(bai);
                oneRecord.setLmId(lmId);
                oneRecord.setLsTypeId("2");
                oneRecord.setBuyMoney(buyMoney);
                oneRecord.setTypeFlag(4);
                oneRecord.setCodeList(codeList);
                list.add(oneRecord);
            }
        }else{
            //删除数字之外的任何字符
            //codeRule = codeRule.replace("/[^[0-9]/g","");
            String newValue  = codeRule.replaceAll("[^0-9]","");
            String[] numArr = newValue.split("");
            //删除重复的数字
            Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
            if(nums.size()!=2){
                resMap.put("errmsg","只能选2个号码:"+codeRule);
                return resMap;
            }

            String bai = nums.stream().collect(Collectors.joining());
            List<String> codeList = Code3DCreateUtils.z3SFCode(bai);

            String buyDesc = "双飞组三"+codeRule + "[" + codeList.size() + "注]";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(buyDesc);
            oneRecord.setBai(bai);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setBuyCodes(bai);
            oneRecord.setValue(bai);
            oneRecord.setLmId(lmId);
            oneRecord.setLsTypeId("2");
            oneRecord.setBuyMoney(buyMoney);
            oneRecord.setTypeFlag(4);
            oneRecord.setCodeList(codeList);
            list.add(oneRecord);
        }
        resMap.put("list",list);
        return resMap;
    }

    //双飞组三
    public Map<String,Object> z3SFBuyNew(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        boolean hasError = false;
        if (codeRule.contains(",") || codeRule.contains("，") || codeRule.contains(".") || codeRule.contains("/") || codeRule.contains(" ")) {
            String[] splitArr = codeRule.split("\\.|,|，|/| |-|。|、");
            int num = 0;
            for (String arr : splitArr) {
                //删除数字之外的任何字符
                //codeRule = codeRule.replace("/[^[0-9]/g","");
                String newValue = arr.replaceAll("[^0-9]", "");
                String[] numArr = newValue.split("");
                List<String> codeList = new ArrayList<>();
                codeList.add(arr);
                num = num + codeList.size();
            }
            for (String arr : splitArr) {
                //删除数字之外的任何字符
                //codeRule = codeRule.replace("/[^[0-9]/g","");
                String newValue  = arr.replaceAll("[^0-9]","");
                String[] numArr = newValue.split("");
                List<String> codeList = new ArrayList<>();
                codeList.add(arr);

                String buyDesc = "双飞组三(组):"+codeRule + "[" + num + "注]";
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName(buyDesc);
                oneRecord.setBai(codeRule);
                oneRecord.setBuyAmount(codeList.size());
                oneRecord.setBuyCodes(codeRule);
                oneRecord.setValue(codeRule);
                oneRecord.setLmId(lmId);
                oneRecord.setLsTypeId("2");
                oneRecord.setBuyMoney(buyMoney);
                oneRecord.setTypeFlag(4);
                oneRecord.setCodeList(codeList);
                list.add(oneRecord);
            }
        }else{
            //删除数字之外的任何字符
            //codeRule = codeRule.replace("/[^[0-9]/g","");
            String newValue  = codeRule.replaceAll("[^0-9]","");
            String[] numArr = newValue.split("");
            List<String> codeList = new ArrayList<>();
            codeList.add(codeRule);

            String buyDesc = "双飞组三(组):"+codeRule + "[" + codeList.size() + "注]";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(buyDesc);
            oneRecord.setBai(codeRule);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setBuyCodes(codeRule);
            oneRecord.setValue(codeRule);
            oneRecord.setLmId(lmId);
            oneRecord.setLsTypeId("2");
            oneRecord.setBuyMoney(buyMoney);
            oneRecord.setTypeFlag(4);
            oneRecord.setCodeList(codeList);
            list.add(oneRecord);
        }
        resMap.put("list",list);
        return resMap;
    }


    //组六普通
    public Map<String,Object> z6Buy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        if (codeRule.contains(",") || codeRule.contains("，") || codeRule.contains(".") || codeRule.contains("/") || codeRule.contains(" ")) {
            String[] splitArr = codeRule.split("\\.|,|，|/| |-|。|、");
            int num = 0;
            for (String arr : splitArr) {
                //codeRule = codeRule.replace("/[^[0-9]/g","");
                String newValue = arr.replaceAll("[^0-9]", "");
                String[] numArr = newValue.split("");
                Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
                if (nums.size() < 3) {
                    resMap.put("errmsg", "号码数量不能小于3");
                    return resMap;
                }

                String bai = nums.stream().collect(Collectors.joining());
                List<String> codeList = Code3DCreateUtils.z6CodeBy(bai);
                num = num + codeList.size();
            }
            for (String arr : splitArr) {
                //codeRule = codeRule.replace("/[^[0-9]/g","");
                String newValue  = arr.replaceAll("[^0-9]","");
                String[] numArr = newValue.split("");
                Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
                if(nums.size()<3){
                    resMap.put("errmsg","号码数量不能小于3");
                    return resMap;
                }

                String bai = nums.stream().collect(Collectors.joining());
                List<String> codeList = Code3DCreateUtils.z6CodeBy(bai);

                String lsName = "组六"+StringUtil.changeDigitToChinese(bai.length(),"码");

                List<LotterySetting> lsList = lotterySettingService.getListBy(lmId);
                LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
                if(null == ls){
                    resMap.put("errmsg","组六号码格式不正确："+codeRule);
                    return resMap;
                }
                String buyDesc = lsName+codeRule + "[" + num + "注]";
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName(buyDesc);
                oneRecord.setBai(bai);
                oneRecord.setBuyAmount(codeList.size());
                oneRecord.setBuyCodes(bai);
                oneRecord.setValue(bai);
                oneRecord.setLmId(lmId);
                oneRecord.setLsTypeId(ls.getTypeId()+"");
                oneRecord.setBuyMoney(buyMoney);
                oneRecord.setTypeFlag(1);
                oneRecord.setCodeList(codeList);
                list.add(oneRecord);
            }
        }else{
            String txt = codeRule;
            //codeRule = codeRule.replace("/[^[0-9]/g","");
            String newValue  = codeRule.replaceAll("[^0-9]","");
            String[] numArr = newValue.split("");
            Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
            if(nums.size()<3){
                resMap.put("errmsg","号码数量不能小于3");
                return resMap;
            }

            String bai = nums.stream().collect(Collectors.joining());
            List<String> codeList = Code3DCreateUtils.z6CodeBy(bai);

            String lsName = "组六"+StringUtil.changeDigitToChinese(bai.length(),"码");

            List<LotterySetting> lsList = lotterySettingService.getListBy(lmId);
            LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
            if(null == ls){
                resMap.put("errmsg","组六号码格式不正确："+txt);
                return resMap;
            }
            String buyDesc = lsName+txt + "[" + 1 + "注]";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(buyDesc);
            oneRecord.setBai(bai);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setBuyCodes(bai);
            oneRecord.setValue(bai);
            oneRecord.setLmId(lmId);
            oneRecord.setLsTypeId(ls.getTypeId()+"");
            oneRecord.setBuyMoney(buyMoney);
            oneRecord.setTypeFlag(1);
            oneRecord.setCodeList(codeList);
            list.add(oneRecord);
        }
        resMap.put("list",list);
        return resMap;
    }

    public Map<String,Object> zzBuy(BigDecimal buyMoney,String codeRule,String lmId){
        List<LotterySetting> lsList = lotterySettingService.getListBy(lmId);
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] multiArr = codeRule.split("\\.|,|，|/| |-|。|、");
        for(String ss : multiArr){
            if(StringUtil.isNull(ss) || StringUtils.isNullOrEmpty(ss)){
                continue;
            }
            String newValue  = ss;
            if(ss.equals("全包")){
                ss = "0123456789";
            }
            //删除数字之外的任何字符
            newValue  = ss.replaceAll("[^0-9]","");
            String[] numArr = newValue.split("");
            //删除重复的数字
            Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
            if(nums.size()<2){
                resMap.put("errmsg","组三号码数量必须大于2:"+codeRule);
                return resMap;
            }
            if (getCountByCode(newValue) != 2) {
                continue;
            }
            String bai = nums.stream().collect(Collectors.joining());
            String lsName = "组三"+ StringUtil.changeDigitToChinese(bai.length(),"码");

            LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
            if(null == ls){
                resMap.put("errmsg","组三号码格式错误:"+codeRule);
                return resMap;
            }
            String buyDesc = "组三组选"+"(组):"+codeRule + "[" + 1 + "注]";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(buyDesc);
            oneRecord.setBuyCode(codeRule);
            oneRecord.setBuyAmount(1);
            oneRecord.setValue(codeRule);
            oneRecord.setLmId(lmId);
            oneRecord.setLsTypeId(ls.getTypeId()+"");
            oneRecord.setBuyMoney(buyMoney);
            oneRecord.setBuyDesc(buyDesc);
            oneRecord.setCodeList(Lists.newArrayList(codeRule));
            list.add(oneRecord);
        }
        resMap.put("list",list);
        return resMap;
    }

    public static Integer getCountByCode(String buyCode) {
        List list = new ArrayList();
        char c[] = buyCode.toCharArray();
        list.add(c[0]);
        for(int i = 0;i < c.length;i++){
            for(int j = 0;j < list.size();j++){
                if((char)list.get(j) == c[i]){
                    break;
                }else{
                    if(j == list.size()-1){
                        list.add(c[i]);
                    }
                }
            }
        }
        List<Integer> list2 = Lists.newArrayList();
        for(int j = 0;j < list.size();j++){  //拿出集合中每一个跟数组中元素比，如果一样则m++
            int m=0;
            for(int i = 0;i < c.length;i++){  //遍历数组中的每一个字符
                if(list.get(j).equals(c[i])){
                    m++;
                }
            }
            list2.add(m);
        }

        return list2.stream().max(Integer::compareTo).get();
    }


    //组三普通
    public Map<String,Object> z3Buy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        if (codeRule.contains(",") || codeRule.contains("，") || codeRule.contains(".") || codeRule.contains("/") || codeRule.contains(" ")) {
            String[] splitArr = codeRule.split("\\.|,|，|/| |-|。|、");
            int num = 0;
            for (String arr : splitArr) {
                //删除数字之外的任何字符
                //codeRule = codeRule.replace("/[^[0-9]/g","");
                String newValue = arr.replaceAll("[^0-9]", "");
                String[] numArr = newValue.split("");
                Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
                if (nums.size() < 2) {
                    resMap.put("errmsg", "组三号码格式不正确：" + codeRule);
                    return resMap;
                }

                String bai = nums.stream().collect(Collectors.joining());
                List<String> codeList = Code3DCreateUtils.z3Code(bai);
                num = num + codeList.size();
            }
            for (String arr : splitArr) {
                String txt = codeRule;
                //删除数字之外的任何字符
                //codeRule = codeRule.replace("/[^[0-9]/g","");
                String newValue  = arr.replaceAll("[^0-9]","");
                String[] numArr = newValue.split("");
                Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
                if(nums.size()<2){
                    resMap.put("errmsg","组三号码格式不正确："+txt);
                    return resMap;
                }

                String bai = nums.stream().collect(Collectors.joining());
                List<String> codeList = Code3DCreateUtils.z3Code(bai);
                String lsName = "组三"+StringUtil.changeDigitToChinese(bai.length(),"码");

                List<LotterySetting> lsList = lotterySettingService.getListBy("3");
                LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
                if(null != ls){
                    String buyDesc = lsName+txt + "[" + num + "注]";
                    BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                    oneRecord.setHuizongName(buyDesc);
                    oneRecord.setBai(bai);
                    oneRecord.setBuyAmount(codeList.size());
                    oneRecord.setBuyCodes(bai);
                    oneRecord.setValue(bai);
                    oneRecord.setLmId(lmId);
                    oneRecord.setLsTypeId(ls.getTypeId()+"");
                    oneRecord.setBuyMoney(buyMoney);
                    oneRecord.setTypeFlag(1);
                    oneRecord.setCodeList(codeList);
                    list.add(oneRecord);
                }
            }
            resMap.put("list",list);
            return resMap;
        }else{
            String txt = codeRule;
            //删除数字之外的任何字符
            //codeRule = codeRule.replace("/[^[0-9]/g","");
            String newValue  = codeRule.replaceAll("[^0-9]","");
            String[] numArr = newValue.split("");
            Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
            if(nums.size()<2){
                resMap.put("errmsg","组三号码格式不正确："+txt);
                return resMap;
            }

            String bai = nums.stream().collect(Collectors.joining());
            List<String> codeList = Code3DCreateUtils.z3Code(bai);
            String lsName = "组三"+StringUtil.changeDigitToChinese(bai.length(),"码");

            List<LotterySetting> lsList = lotterySettingService.getListBy("3");
            LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
            if(null != ls){
                String buyDesc = lsName+txt + "[" + 1 + "注]";
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName(buyDesc);
                oneRecord.setBai(bai);
                oneRecord.setBuyAmount(codeList.size());
                oneRecord.setBuyCodes(bai);
                oneRecord.setValue(bai);
                oneRecord.setLmId(lmId);
                oneRecord.setLsTypeId(ls.getTypeId()+"");
                oneRecord.setBuyMoney(buyMoney);
                oneRecord.setTypeFlag(1);
                oneRecord.setCodeList(codeList);
                list.add(oneRecord);
                resMap.put("list",list);
                return resMap;
            }
        }
        resMap.put("errmsg","组三号码格式不正确："+codeRule);
        return resMap;
    }

    //组三胆拖
    public Map<String,Object> z3dtBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        if (codeRule.contains(",") || codeRule.contains("，") || codeRule.contains(".") || codeRule.contains("/") || codeRule.contains(" ")) {
            String[] splitArr = codeRule.split("\\.|,|，|/| |-|。|、");
            int num = 0;
            for (String arr : splitArr) {
                String[] numArr = arr.split("拖");
                if (numArr.length != 2) {
                    resMap.put("errmsg", "号码格式不正确：" + codeRule);
                    return resMap;
                }
                String tm = null;
                String dm = null;
                dm = numArr[0];
                tm = numArr[1];
                if (StringUtil.isNull(dm) || StringUtil.isNull(tm)) {
                    resMap.put("errmsg", "号码格式不正确：" + codeRule);
                    return resMap;
                }

                Set<String> dmCode = Arrays.asList(dm.split("")).stream().collect(Collectors.toSet());
                dm = dmCode.stream().collect(Collectors.joining());
                if (dm.length() != 1) {
                    resMap.put("errmsg", "只能选择1个胆码：" + codeRule);
                    return resMap;
                }

                Set<String> tmCode = Arrays.asList(tm.split("")).stream().collect(Collectors.toSet());
                tm = tmCode.stream().collect(Collectors.joining());
                for (String c : dmCode) {
                    if (tm.contains(c)) { //拖码中包含胆码号码，则删除拖码中对应的号码
                        tm = tm.replace(c, "");
                    }
                }

                if (tm.length() < 2) {
                    resMap.put("errmsg", "至少选择2个拖码：" + codeRule);
                    return resMap;
                }

                String lsName = dm.length() + "码" + "拖" + tm.length();
                List<LotterySetting> lsList = lotterySettingService.getListBy("3");
                LotterySetting ls = lsList.stream().filter(item -> item.getBettingRule().equals(lsName)).findFirst().orElse(null);
                if (null == ls) {
                    resMap.put("errmsg", "号码格式错误：" + codeRule);
                    return resMap;
                }

                List<String> codeList = Code3DCreateUtils.z3DtCode(dm, tm);
                num = num + codeList.size();
            }
            for (String arr : splitArr) {
                String[] numArr = arr.split("拖");
                if(numArr.length!=2){
                    resMap.put("errmsg","号码格式不正确："+codeRule);
                    return resMap;
                }
                String tm = null;
                String dm = null;
                dm = numArr[0];
                tm = numArr[1];
                if(StringUtil.isNull(dm) || StringUtil.isNull(tm)){
                    resMap.put("errmsg","号码格式不正确："+codeRule);
                    return resMap;
                }

                Set<String> dmCode = Arrays.asList(dm.split("")).stream().collect(Collectors.toSet());
                dm = dmCode.stream().collect(Collectors.joining());
                if(dm.length()!=1){
                    resMap.put("errmsg","只能选择1个胆码："+codeRule);
                    return resMap;
                }

                Set<String> tmCode = Arrays.asList(tm.split("")).stream().collect(Collectors.toSet());
                tm = tmCode.stream().collect(Collectors.joining());
                for(String c : dmCode){
                    if(tm.contains(c)){ //拖码中包含胆码号码，则删除拖码中对应的号码
                        tm = tm.replace(c,"");
                    }
                }

                if(tm.length()<2){
                    resMap.put("errmsg","至少选择2个拖码："+codeRule);
                    return resMap;
                }

                String lsName = dm.length()+"码"+"拖"+tm.length();
                List<LotterySetting> lsList = lotterySettingService.getListBy("3");
                LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
                if(null == ls){
                    resMap.put("errmsg","号码格式错误："+codeRule);
                    return resMap;
                }

                List<String> codeList = Code3DCreateUtils.z3DtCode(dm,tm);
//                String buyDesc = "组三"+dm+"拖"+tm;
                String buyDesc = "组三"+codeRule + "[" + num + "注]";
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName(buyDesc);
                oneRecord.setBai(dm);
                oneRecord.setShi(tm);
                oneRecord.setBuyAmount(codeList.size());
                oneRecord.setValue(dm+","+tm);
                oneRecord.setLmId(lmId);
                oneRecord.setLsTypeId(ls.getTypeId()+"");
                oneRecord.setBuyMoney(buyMoney);
                oneRecord.setTypeFlag(2);
                oneRecord.setCodeList(codeList);
                list.add(oneRecord);
            }
        }else{
            String[] numArr = codeRule.split("拖");
            if(numArr.length!=2){
                resMap.put("errmsg","号码格式不正确："+codeRule);
                return resMap;
            }
            String tm = null;
            String dm = null;
            dm = numArr[0];
            tm = numArr[1];
            if(StringUtil.isNull(dm) || StringUtil.isNull(tm)){
                resMap.put("errmsg","号码格式不正确："+codeRule);
                return resMap;
            }

            Set<String> dmCode = Arrays.asList(dm.split("")).stream().collect(Collectors.toSet());
            dm = dmCode.stream().collect(Collectors.joining());
            if(dm.length()!=1){
                resMap.put("errmsg","只能选择1个胆码："+codeRule);
                return resMap;
            }

            Set<String> tmCode = Arrays.asList(tm.split("")).stream().collect(Collectors.toSet());
            tm = tmCode.stream().collect(Collectors.joining());
            for(String c : dmCode){
                if(tm.contains(c)){ //拖码中包含胆码号码，则删除拖码中对应的号码
                    tm = tm.replace(c,"");
                }
            }

            if(tm.length()<2){
                resMap.put("errmsg","至少选择2个拖码："+codeRule);
                return resMap;
            }

            String lsName = dm.length()+"码"+"拖"+tm.length();
            List<LotterySetting> lsList = lotterySettingService.getListBy("3");
            LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
            if(null == ls){
                resMap.put("errmsg","号码格式错误："+codeRule);
                return resMap;
            }

            List<String> codeList = Code3DCreateUtils.z3DtCode(dm,tm);
            String buyDesc = "组三"+dm+"拖"+tm + "[" + codeList.size() + "注]";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(buyDesc);
            oneRecord.setBai(dm);
            oneRecord.setShi(tm);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setValue(dm+","+tm);
            oneRecord.setLmId(lmId);
            oneRecord.setLsTypeId(ls.getTypeId()+"");
            oneRecord.setBuyMoney(buyMoney);
            oneRecord.setTypeFlag(2);
            oneRecord.setCodeList(codeList);
            list.add(oneRecord);
        }
        resMap.put("list",list);
        return resMap;
    }

    //组三和值
    public  Map<String,Object> z3hzBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split("\\.|,|，|/| |-|。|、");
        Set<String> hzSet = Arrays.asList(numArr).stream().collect(Collectors.toSet());
        if(hzSet.size()==0){
            resMap.put("errmsg","号码格式不正确");
            return resMap;
        }
        String[] hzArr = {"1","2","3","4","5","6","7","8","9","10","11",
                "12","13","14","15","16","17","18","19","20","21","22","23","24","25","26"};
        List<String> validHzList = Arrays.asList(hzArr);
        List<String> hzList = Lists.newArrayList();
        for(String hz : hzSet){
            if(validHzList.contains(hz)){
                hzList.add(hz);
            }
        }
        if(hzList.isEmpty()){
            resMap.put("errmsg","未找到组三和值号码");
            return resMap;
        }
        Collections.sort(hzList);
        String hzname = "组三和值";
        String buyDesc = hzname+codeRule;
        List<String> codeList = Code3DCreateUtils.z3hezhi(hzList);

        String value = hzList.stream().collect(Collectors.joining(","));
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(buyDesc);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setValue(value);
        oneRecord.setLmId(lmId);
        oneRecord.setLsTypeId("3");
        oneRecord.setHzList(hzList);
        oneRecord.setBuyMoney(buyMoney);
        oneRecord.setBuyDesc(buyDesc);
        oneRecord.setCodeList(codeList);
        oneRecord.setTypeFlag(3);
        list.add(oneRecord);
        resMap.put("list",list);
        return resMap;
    }


    public  Map<String,Object> z3Z6OneCode(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        String lmName = "";
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] arr = codeRule.split("\\.|,|，|/| |-|。|、");
        if(arr.length<2){
            resMap.put("errmsg","号码格式错误："+codeRule);
            return resMap;
        }
        Set<String> noRepestSet = Arrays.asList(arr).stream().collect(Collectors.toSet());
        List<String> numList = noRepestSet.stream().collect(Collectors.toList());
        List<String> validCodeList = Lists.newArrayList();
        //和值
        List<String> z3CodeList = null;
        if(lmId.equals("3")){
            lmName = "组三";
            z3CodeList = Code3DCreateUtils.z3Code();
            for(String code : numList){
                code = Arrays.asList(code.split("")).stream().sorted().collect(Collectors.joining());
                if(z3CodeList.contains(code)){
                    validCodeList.add(code);
                }
            }
        }else{
            lmName = "组六";
            z3CodeList = Code3DCreateUtils.z6Code();
            for(String code : numList){
                code = Arrays.asList(code.split("")).stream().sorted().collect(Collectors.joining());
                if(z3CodeList.contains(code)){
                    validCodeList.add(code);
                }
            }
        }
        if(validCodeList.isEmpty()){
            resMap.put("errmsg","未找到"+lmName+"号码");
            return resMap;
        }

        String value = validCodeList.stream().collect(Collectors.joining(","));
        String buyDesc = lmName+codeRule;
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(buyDesc);
        oneRecord.setBuyAmount(validCodeList.size());
        oneRecord.setValue(value);
        oneRecord.setLmId(lmId);
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyMoney(buyMoney);
        oneRecord.setTypeFlag(5);
        oneRecord.setCodeList(validCodeList);
        oneRecord.setBuyDesc(buyDesc);
        list.add(oneRecord);
        resMap.put("list",list);
        return resMap;
    }

    //组六胆拖
    public Map<String,Object> z6dtBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId ){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        if (codeRule.contains(",") || codeRule.contains("，") || codeRule.contains(".") || codeRule.contains("/") || codeRule.contains(" ")) {
            String[] splitArr = codeRule.split("\\.|,|，|/| |-|。|、");
            int num = 0;
            for (String arr : splitArr) {
                String[] numArr = arr.split("拖");
                if (numArr.length != 2) {
                    resMap.put("errmsg", "号码格式不正确：" + arr);
                    return resMap;
                }
                String dm = numArr[0];
                String tm = numArr[1];
                if (StringUtil.isNull(dm) || StringUtil.isNull(tm)) {
                    resMap.put("errmsg", "号码格式不正确：" + arr);
                    return resMap;
                }

                Set<String> dmCode = Arrays.asList(dm.split("")).stream().collect(Collectors.toSet());
                dm = dmCode.stream().collect(Collectors.joining());
                if (dm.length() != 1 && dm.length() != 2) {
                    resMap.put("errmsg", "只能选择1~2个胆码：" + arr);
                    return resMap;
                }

                Set<String> tmCode = Arrays.asList(tm.split("")).stream().collect(Collectors.toSet());
                tm = tmCode.stream().collect(Collectors.joining());
                for (String c : dmCode) {
                    if (tm.contains(c)) { //拖码中包含胆码号码，则删除拖码中对应的号码
                        tm = tm.replace(c, "");
                    }
                }
                if (tm.length() < 1) {
                    resMap.put("errmsg", "拖码数量不能小于1：" + arr);
                    return resMap;
                }

                String lsName = dm.length() + "码" + "拖" + tm.length();
                List<LotterySetting> lsList = lotterySettingService.getListBy(lmId);
                LotterySetting ls = lsList.stream().filter(item -> item.getBettingRule().equals(lsName)).findFirst().orElse(null);
                if (null == ls) {
                    resMap.put("errmsg", "号码格式错误：" + arr);
                    return resMap;
                }

                List<String> codeList = Code3DCreateUtils.z6DtCode(dm, tm);
                num = num + codeList.size();
            }
            for (String arr : splitArr) {
                String[] numArr = arr.split("拖");
                if(numArr.length!=2){
                    resMap.put("errmsg","号码格式不正确："+arr);
                    return resMap;
                }
                String dm = numArr[0];
                String tm = numArr[1];
                if(StringUtil.isNull(dm) || StringUtil.isNull(tm)){
                    resMap.put("errmsg","号码格式不正确："+arr);
                    return resMap;
                }

                Set<String> dmCode = Arrays.asList(dm.split("")).stream().collect(Collectors.toSet());
                dm = dmCode.stream().collect(Collectors.joining());
                if(dm.length()!=1 && dm.length()!=2){
                    resMap.put("errmsg","只能选择1~2个胆码："+arr);
                    return resMap;
                }

                Set<String> tmCode = Arrays.asList(tm.split("")).stream().collect(Collectors.toSet());
                tm = tmCode.stream().collect(Collectors.joining());
                for(String c : dmCode){
                    if(tm.contains(c)){ //拖码中包含胆码号码，则删除拖码中对应的号码
                        tm = tm.replace(c,"");
                    }
                }
                if(tm.length()<1){
                    resMap.put("errmsg","拖码数量不能小于1："+arr);
                    return resMap;
                }

                String lsName = dm.length()+"码"+"拖"+tm.length();
                List<LotterySetting> lsList = lotterySettingService.getListBy(lmId);
                LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
                if(null == ls){
                    resMap.put("errmsg","号码格式错误："+arr);
                    return resMap;
                }

                List<String> codeList = Code3DCreateUtils.z6DtCode(dm,tm);
                String buyDesc = "组六"+arr + "[" + num + "注]";
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName(buyDesc);
                oneRecord.setBai(dm);
                oneRecord.setShi(tm);
                oneRecord.setBuyAmount(codeList.size());
                oneRecord.setValue(dm+","+tm);
                oneRecord.setLmId(lmId);
                oneRecord.setLsTypeId(ls.getTypeId()+"");
                oneRecord.setBuyMoney(buyMoney);
                oneRecord.setTypeFlag(2);
                oneRecord.setCodeList(codeList);
                list.add(oneRecord);
            }
        }else {
            String[] numArr = codeRule.split("拖");
            if(numArr.length!=2){
                resMap.put("errmsg","号码格式不正确："+codeRule);
                return resMap;
            }
            String dm = numArr[0];
            String tm = numArr[1];
            if(StringUtil.isNull(dm) || StringUtil.isNull(tm)){
                resMap.put("errmsg","号码格式不正确："+codeRule);
                return resMap;
            }

            Set<String> dmCode = Arrays.asList(dm.split("")).stream().collect(Collectors.toSet());
            dm = dmCode.stream().collect(Collectors.joining());
            if(dm.length()!=1 && dm.length()!=2){
                resMap.put("errmsg","只能选择1~2个胆码："+codeRule);
                return resMap;
            }

            Set<String> tmCode = Arrays.asList(tm.split("")).stream().collect(Collectors.toSet());
            tm = tmCode.stream().collect(Collectors.joining());
            for(String c : dmCode){
                if(tm.contains(c)){ //拖码中包含胆码号码，则删除拖码中对应的号码
                    tm = tm.replace(c,"");
                }
            }
            if(tm.length()<1){
                resMap.put("errmsg","拖码数量不能小于1："+codeRule);
                return resMap;
            }

            String lsName = dm.length()+"码"+"拖"+tm.length();
            List<LotterySetting> lsList = lotterySettingService.getListBy(lmId);
            LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
            if(null == ls){
                resMap.put("errmsg","号码格式错误："+codeRule);
                return resMap;
            }

            List<String> codeList = Code3DCreateUtils.z6DtCode(dm,tm);
            String buyDesc = "组六"+codeRule + "[" + codeList.size() + "注]";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(buyDesc);
            oneRecord.setBai(dm);
            oneRecord.setShi(tm);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setValue(dm+","+tm);
            oneRecord.setLmId(lmId);
            oneRecord.setLsTypeId(ls.getTypeId()+"");
            oneRecord.setBuyMoney(buyMoney);
            oneRecord.setTypeFlag(2);
            oneRecord.setCodeList(codeList);
            list.add(oneRecord);
        }

        resMap.put("list",list);
        return resMap;
    }

    //组六和值
    public Map<String,Object> z6hzBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split("\\.|,|，|/| |-|。|、");
        if(numArr.length==0){
            resMap.put("errmsg","号码格式不正确");
            return resMap;
        }
        Set<String> hzSet = Arrays.asList(numArr).stream().collect(Collectors.toSet());
        String[] validHzArr = {"3","4","5","6","7","8","9","10","11",
                "12","13","14","15","16","17","18","19","20","21","22","23","24"};
        List<String> validHzList = Arrays.asList(validHzArr);
        List<String> hzList = Lists.newArrayList();
        for(String hz : hzSet){
            if(validHzList.contains(hz)){
                hzList.add(hz);
            }
        }
        List<String> codeList = Code3DCreateUtils.z6hezhi(hzList);
        String buyDesc = "组六和值"+codeRule;
        String value = hzList.stream().collect(Collectors.joining(","));
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(buyDesc);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setValue(value);
        oneRecord.setLmId(lmId);
        oneRecord.setLsTypeId("3");
        oneRecord.setHzList(hzList);
        oneRecord.setCodeList(codeList);
        oneRecord.setBuyMoney(buyMoney);
        oneRecord.setBuyDesc(buyDesc);
        oneRecord.setTypeFlag(3);
        list.add(oneRecord);
        resMap.put("list",list);
        return resMap;
    }

    //双飞组六
    public Map<String,Object> z6SFBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        if (codeRule.contains(",") || codeRule.contains("，") || codeRule.contains(".") || codeRule.contains("/") || codeRule.contains(" ")) {
            String[] splitArr = codeRule.split("\\.|,|，|/| |-|。|、");
            int num = 0;
            for (String arr : splitArr) {
                String newValue  = arr.replaceAll("[^0-9]","");
                String[] numArr = newValue.split("");
                Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
                if(nums.size()!=2){
                    resMap.put("errmsg","请选择2个号码："+codeRule);
                    return resMap;
                }
                String bai = nums.stream().collect(Collectors.joining());
                List<String> codeList = Code3DCreateUtils.z6SFCode(bai);
                num = num + codeList.size();
            }
            for (String arr : splitArr) {
                //删除数字之外的任何字符
                //codeRule = codeRule.replace("/[^[0-9]/g","");
                String newValue  = arr.replaceAll("[^0-9]","");
                String[] numArr = newValue.split("");
                //删除重复的数字
                Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
                if(nums.size()!=2){
                    resMap.put("errmsg","请选择2个号码："+codeRule);
                    return resMap;
                }
                String bai = nums.stream().collect(Collectors.joining());
                List<String> codeList = Code3DCreateUtils.z6SFCode(bai);

                String buyDesc = "双飞组六"+codeRule + "[" + num + "注]";
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName(buyDesc);
                oneRecord.setBai(bai);
                oneRecord.setBuyAmount(codeList.size());
                oneRecord.setBuyCodes(bai);
                oneRecord.setValue(bai);
                oneRecord.setLmId(lmId);
                oneRecord.setLsTypeId("2");
                oneRecord.setBuyMoney(buyMoney);
                oneRecord.setTypeFlag(4);
                oneRecord.setCodeList(codeList);
                list.add(oneRecord);
            }
        }else{
            //删除数字之外的任何字符
            //codeRule = codeRule.replace("/[^[0-9]/g","");
            String newValue  = codeRule.replaceAll("[^0-9]","");
            String[] numArr = newValue.split("");
            //删除重复的数字
            Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
            if(nums.size()!=2){
                resMap.put("errmsg","请选择2个号码："+codeRule);
                return resMap;
            }
            String bai = nums.stream().collect(Collectors.joining());
            List<String> codeList = Code3DCreateUtils.z6SFCode(bai);

            String buyDesc = "双飞组六"+codeRule + "[" + codeList.size() + "注]";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(buyDesc);
            oneRecord.setBai(bai);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setBuyCodes(bai);
            oneRecord.setValue(bai);
            oneRecord.setLmId(lmId);
            oneRecord.setLsTypeId("2");
            oneRecord.setBuyMoney(buyMoney);
            oneRecord.setTypeFlag(4);
            oneRecord.setCodeList(codeList);
            list.add(oneRecord);
        }
        resMap.put("list",list);
        return resMap;
    }

    //双飞组六
    public Map<String,Object> z6SFBuyNew(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        if (codeRule.contains(",") || codeRule.contains("，") || codeRule.contains(".") || codeRule.contains("/") || codeRule.contains(" ")) {
            String[] splitArr = codeRule.split("\\.|,|，|/| |-|。|、");
            int num = 0;
            for (String arr : splitArr) {
                String newValue  = arr.replaceAll("[^0-9]","");
                String[] numArr = newValue.split("");
                Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
                if(nums.size()!=2){
                    resMap.put("errmsg","请选择2个号码："+codeRule);
                    return resMap;
                }
                String bai = nums.stream().collect(Collectors.joining());
                List<String> codeList = new ArrayList<>();
                codeList.add(arr);
                num = num + codeList.size();
            }
            for (String arr : splitArr) {
                //删除数字之外的任何字符
                //codeRule = codeRule.replace("/[^[0-9]/g","");
                String newValue  = arr.replaceAll("[^0-9]","");
                String[] numArr = newValue.split("");
                //删除重复的数字
                Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
                if(nums.size()!=2){
                    resMap.put("errmsg","请选择2个号码："+codeRule);
                    return resMap;
                }
                String bai = nums.stream().collect(Collectors.joining());
                List<String> codeList = new ArrayList<>();
                codeList.add(arr);

                String buyDesc = "双飞组六(组):"+codeRule + "[" + num + "注]";
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName(buyDesc);
                oneRecord.setBai(bai);
                oneRecord.setBuyAmount(codeList.size());
                oneRecord.setBuyCodes(bai);
                oneRecord.setValue(bai);
                oneRecord.setLmId(lmId);
                oneRecord.setLsTypeId("2");
                oneRecord.setBuyMoney(buyMoney);
                oneRecord.setTypeFlag(4);
                oneRecord.setCodeList(codeList);
                list.add(oneRecord);
            }
        }else{
            //删除数字之外的任何字符
            //codeRule = codeRule.replace("/[^[0-9]/g","");
            String newValue  = codeRule.replaceAll("[^0-9]","");
            String[] numArr = newValue.split("");
            //删除重复的数字
            Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
            if(nums.size()!=2){
                resMap.put("errmsg","请选择2个号码："+codeRule);
                return resMap;
            }
            String bai = nums.stream().collect(Collectors.joining());
            List<String> codeList = new ArrayList<>();
            codeList.add(codeRule);

            String buyDesc = "双飞组六(组):"+codeRule + "[" + codeList.size() + "注]";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(buyDesc);
            oneRecord.setBai(bai);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setBuyCodes(bai);
            oneRecord.setValue(bai);
            oneRecord.setLmId(lmId);
            oneRecord.setLsTypeId("2");
            oneRecord.setBuyMoney(buyMoney);
            oneRecord.setTypeFlag(4);
            oneRecord.setCodeList(codeList);
            list.add(oneRecord);
        }
        resMap.put("list",list);
        return resMap;
    }

    public Map<String,Object> hsBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split("\\.|,|，|/| |-|。|、");
        if(numArr.length==0){
            resMap.put("errmsg","号码格式错误："+codeRule);
            return resMap;
        }
        Set<String> hzSet = Arrays.asList(numArr).stream().collect(Collectors.toSet());
        List<String> validHzList = Arrays.asList("0","1","2","3","4","5","6","7","8","9","10","11",
                "12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27");
        List<String> hzList = Lists.newArrayList();
        for(String hz : hzSet){
            if(validHzList.contains(hz)){
                hzList.add(hz);
            }
        }
        if(hzList.isEmpty()){
            resMap.put("errmsg","未找到和数号码："+codeRule);
            return resMap;
        }
        String huizongName = "和数"+codeRule;
        String value = hzList.stream().collect(Collectors.joining(","));
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(huizongName);
        oneRecord.setBuyAmount(hzList.size());
        oneRecord.setHzList(hzList.stream().collect(Collectors.toList()));
        oneRecord.setValue(value);
        oneRecord.setLmId("5");
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyDesc(huizongName);
        oneRecord.setBuyMoney(buyMoney);
        list.add(oneRecord);
        resMap.put("list",list);
        return resMap;
    }


    public Map<String,Object> kdBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        //codeRule = codeRule.replace("/[^[0-9]/g","");
        String newValue  = codeRule.replaceAll("[^0-9]","");
        String[] numArr = newValue.split("");
        //删除重复的数字
        Set<String> numsSet = Arrays.asList(numArr).stream().collect(Collectors.toSet());
        if(numsSet.size()==0){
            resMap.put("errmsg","号码格式错误："+codeRule);
            return resMap;
        }
        List<String> validHzList = Arrays.asList("0","1","2","3","4","5","6","7","8","9");
        List<String> hzList = Lists.newArrayList();
        for(String hz : numsSet){
            if(validHzList.contains(hz)){
                hzList.add(hz);

            }
        }
        if(hzList.isEmpty()){
            resMap.put("errmsg","未找到跨度号码");
            return resMap;
        }
        Collections.sort(hzList);
        String huizongName = "跨度";
        String buyDesc = huizongName+codeRule;
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(huizongName);
        oneRecord.setBuyAmount(hzList.size());
        oneRecord.setHzList(hzList);
        oneRecord.setValue(hzList.stream().collect(Collectors.joining(",")));
        oneRecord.setLmId(lmId);
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyMoney(buyMoney);
        oneRecord.setBuyDesc(buyDesc);
        list.add(oneRecord);
        resMap.put("list",list);
        return resMap;
    }


    //独胆
    public Map<String,Object> ddBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        //codeRule = codeRule.replace("/[^[0-9]/g","");
        String newValue  = codeRule.replaceAll("[^0-9]","");
        String[] numArr = newValue.split("");
        //删除重复的数字
        Set<String> numsSet = Arrays.asList(numArr).stream().collect(Collectors.toSet());
        if(numsSet.size()==0){
            resMap.put("errmsg","号码格式错误："+codeRule);
            return resMap;
        }
        List<String> validHzList = Arrays.asList("0","1","2","3","4","5","6","7","8","9");
        List<String> hzList = Lists.newArrayList();
        for(String hz : numsSet){
            if(validHzList.contains(hz)){
                hzList.add(hz);

            }
        }
        if(hzList.isEmpty()){
            resMap.put("errmsg","未找到独胆号码");
            return resMap;
        }
        Collections.sort(hzList);
        String huizongName = "独胆"+codeRule;
        String value = hzList.stream().collect(Collectors.joining(","));
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(huizongName);
        oneRecord.setBuyDesc(huizongName);
        oneRecord.setBuyAmount(hzList.size());
        oneRecord.setHzList(hzList);
        oneRecord.setValue(value);
        oneRecord.setLmId(lmId);
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyMoney(buyMoney);
        list.add(oneRecord);
        resMap.put("list",list);
        return resMap;
    }


    //1D
    public  Map<String,Object> ding1Buy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        Map<String,Object> resultMap = new HashMap<>();
        if (codeRule.contains(",") || codeRule.contains("，") || codeRule.contains(".") || codeRule.contains("/") || codeRule.contains(" ")) {
            String[] splitArr = codeRule.split("\\.|,|，|/| |-|。|、");
            for (String arr : splitArr) {
                if (arr.contains("XX") || arr.contains("**")){
                    arr = "百"+arr.replaceAll("XX","").replaceAll("\\**","");
                }
                if(arr.contains("百") || arr.contains("十") || arr.contains("个")) {
                    int shiIdx = arr.indexOf("十");
                    int geIdx = arr.indexOf("个");
                    int baiIdx = arr.indexOf("百");
                    String bai = null;
                    String shi = null;
                    String ge = null;
                    if (baiIdx > -1) {
                        if (shiIdx > -1) {
                            bai = arr.substring(baiIdx + 1, shiIdx);
                        } else if (geIdx > -1) {
                            bai = arr.substring(baiIdx + 1, geIdx);
                        } else {
                            bai = arr.substring(baiIdx + 1);
                        }
                    }
                    if (shiIdx > -1) {
                        if (geIdx > -1) {
                            shi = arr.substring(shiIdx + 1, geIdx);
                        } else {
                            shi = arr.substring(shiIdx + 1);
                        }
                    }
                    if (geIdx > -1) ge = arr.substring(geIdx + 1);

                    if (StringUtil.isNull(bai) && StringUtil.isNull(shi) && StringUtil.isNull(ge)) {
                        resultMap.put("errmsg", "号码格式错误：" + codeRule);
                        return resultMap;
                    }
                    if (StringUtil.isNull(bai)) bai = "-";
                    if (StringUtil.isNull(shi)) shi = "-";
                    if (StringUtil.isNull(ge)) ge = "-";
                    List<String> codeList = Code3DCreateUtils.ding1Code(bai, shi, ge);
                    BuyRecord3DVO vo = new BuyRecord3DVO();
                    String buyDesc = "1D" + arr;
                    vo.setHuizongName(buyDesc);
                    vo.setBuyDesc(buyDesc);
                    vo.setHuizongName(buyDesc);
                    vo.setBai(bai);
                    vo.setShi(shi);
                    vo.setGe(ge);
                    vo.setLmId("6");
                    vo.setLsTypeId("1");
                    vo.setValue(codeRule);
                    vo.setBuyAmount(codeList.size());
                    vo.setCodeList(codeList);
                    vo.setBuyMoney(buyMoney);
                    list.add(vo);
                }else{
                    resultMap.put("errmsg", "号码格式错误：" + codeRule);
                    return resultMap;
                }
            }
            resultMap.put("list", list);
            return resultMap;
        }else{
            if (codeRule.contains("XX") || codeRule.contains("**")){
                codeRule = "百"+codeRule.replaceAll("XX","").replaceAll("\\**","");
            }
            if(codeRule.contains("百") || codeRule.contains("十") || codeRule.contains("个")) {
                int shiIdx = codeRule.indexOf("十");
                int geIdx = codeRule.indexOf("个");
                int baiIdx = codeRule.indexOf("百");
                String bai = null;
                String shi = null;
                String ge = null;
                if (baiIdx > -1) {
                    if (shiIdx > -1) {
                        bai = codeRule.substring(baiIdx + 1, shiIdx);
                    } else if (geIdx > -1) {
                        bai = codeRule.substring(baiIdx + 1, geIdx);
                    } else {
                        bai = codeRule.substring(baiIdx + 1);
                    }
                }
                if (shiIdx > -1) {
                    if (geIdx > -1) {
                        shi = codeRule.substring(shiIdx + 1, geIdx);
                    } else {
                        shi = codeRule.substring(shiIdx + 1);
                    }
                }
                if (geIdx > -1) ge = codeRule.substring(geIdx + 1);

                if (StringUtil.isNull(bai) && StringUtil.isNull(shi) && StringUtil.isNull(ge)) {
                    resultMap.put("errmsg", "号码格式错误：" + codeRule);
                    return resultMap;
                }
                if (StringUtil.isNull(bai)) bai = "-";
                if (StringUtil.isNull(shi)) shi = "-";
                if (StringUtil.isNull(ge)) ge = "-";
                List<String> codeList = Code3DCreateUtils.ding1Code(bai, shi, ge);
                BuyRecord3DVO vo = new BuyRecord3DVO();
                String buyDesc = "1D" + codeRule;
                vo.setHuizongName(buyDesc);
                vo.setBuyDesc(buyDesc);
                vo.setHuizongName(buyDesc);
                vo.setBai(bai);
                vo.setShi(shi);
                vo.setGe(ge);
                vo.setLmId("6");
                vo.setLsTypeId("1");
                vo.setValue(codeRule);
                vo.setBuyAmount(codeList.size());
                vo.setCodeList(codeList);
                vo.setBuyMoney(buyMoney);
                list.add(vo);
                resultMap.put("list", list);
                return resultMap;
            }
        }
        resultMap.put("errmsg", "号码格式错误：" + codeRule);
        return resultMap;
    }


    //猜1D
    public Map<String,Object> c1dBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resultMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        //codeRule = codeRule.replace("/[^[0-9]/g","");
        String newValue  = codeRule.replaceAll("[^0-9]","");
        String[] numArr = newValue.split("");
        //删除重复的数字
        Set<String> numsSet = Arrays.asList(numArr).stream().collect(Collectors.toSet());
        if(numsSet.size()==0){
            resultMap.put("errmsg","号码格式错误："+codeRule);
            return resultMap;
        }

        String nums = numsSet.stream().collect(Collectors.joining());
        BuyRecord3DVO vo = new BuyRecord3DVO();
        String buyDesc = "猜1D"+codeRule;
        vo.setHuizongName(buyDesc);
        vo.setBuyDesc(buyDesc);
        vo.setBai(nums);
        vo.setValue(nums);
        vo.setLmId("6");
        vo.setLsTypeId("2");
        vo.setBuyCodes(nums);
        vo.setBuyAmount(nums.length());
        vo.setBuyMoney(buyMoney);
        list.add(vo);
        resultMap.put("list",list);
        return resultMap;
    }



    //2D
    public Map<String,Object> ding2Buy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        Map<String,Object> resultMap = new HashMap<>();
        if (codeRule.contains(",") || codeRule.contains("，") || codeRule.contains(".") || codeRule.contains("/") || codeRule.contains(" ")) {
            String[] splitArr = codeRule.split("\\.|,|，|/| |-|。|、");
            for (String arr : splitArr) {
                if (arr.contains("X") || arr.contains("*")) {
                    arr = arr.replaceAll("\\*", "X");
                    List<String> codeList = new ArrayList<>();
                    codeList.add(arr);
                    if(null==codeList || codeList.size()<1){
                        resultMap.put("errmsg", "号码格式错误：" + codeRule);
                        return resultMap;
                    }
                    String bai = arr.charAt(0)+"";
                    String shi = arr.charAt(1)+"";
                    String ge = arr.charAt(2)+"";
                    BuyRecord3DVO vo = new BuyRecord3DVO();
                    String buyDesc = "2D" + arr;
                    vo.setHuizongName(buyDesc);
                    vo.setBuyDesc(buyDesc);
                    vo.setHuizongName(buyDesc);
                    vo.setBai(bai.equals("X") ? "-" : bai);
                    vo.setShi(shi.equals("X") ? "-" : shi);
                    vo.setGe(ge.equals("X") ? "-" : ge);
                    vo.setLmId(lmId);
                    vo.setLsTypeId("1");
                    vo.setValue("");
                    vo.setBuyAmount(codeList.size());
                    vo.setCodeList(codeList);
                    vo.setBuyMoney(buyMoney);
                    list.add(vo);
                }else{
                    if(arr.contains("百") || arr.contains("十") || arr.contains("个")) {
                        int shiIdx = arr.indexOf("十");
                        int geIdx = arr.indexOf("个");
                        int baiIdx = arr.indexOf("百");
                        String bai = null;
                        String shi = null;
                        String ge = null;
                        if (baiIdx > -1) {
                            if (shiIdx > -1) {
                                bai = arr.substring(baiIdx + 1, shiIdx);
                            } else if (geIdx > -1) {
                                bai = arr.substring(baiIdx + 1, geIdx);
                            } else {
                                bai = arr.substring(baiIdx + 1);
                            }
                        }
                        if (shiIdx > -1) {
                            if (geIdx > -1) {
                                shi = arr.substring(shiIdx + 1, geIdx);
                            } else {
                                shi = arr.substring(shiIdx + 1);
                            }
                        }
                        if (geIdx > -1) ge = arr.substring(geIdx + 1);

                        if (StringUtil.isNull(bai) && StringUtil.isNull(shi) && StringUtil.isNull(ge)) {
                            resultMap.put("errmsg", "号码格式错误：" + codeRule);
                            return resultMap;
                        }
                        if (StringUtil.isNull(bai)) bai = "-";
                        if (StringUtil.isNull(shi)) shi = "-";
                        if (StringUtil.isNull(ge)) ge = "-";
                        List<String> codeList = Code3DCreateUtils.ding2Code(bai, shi, ge);
                        if(null==codeList || codeList.size()<1){
                            resultMap.put("errmsg", "号码格式错误：" + codeRule);
                            return resultMap;
                        }
                        BuyRecord3DVO vo = new BuyRecord3DVO();
                        String buyDesc = "2D" + arr;
                        vo.setHuizongName(buyDesc);
                        vo.setBuyDesc(buyDesc);
                        vo.setHuizongName(buyDesc);
                        vo.setBai(bai);
                        vo.setShi(shi);
                        vo.setGe(ge);
                        vo.setLmId(lmId);
                        vo.setLsTypeId("1");
                        vo.setValue(codeRule);
                        vo.setBuyAmount(codeList.size());
                        vo.setCodeList(codeList);
                        vo.setBuyMoney(buyMoney);
                        list.add(vo);
                    }else{
                        resultMap.put("errmsg", "号码格式错误：" + codeRule);
                        return resultMap;
                    }
                }
            }
            resultMap.put("list", list);
            return resultMap;
        }else{
            if (codeRule.contains("X") || codeRule.contains("*")) {
                codeRule = codeRule.replaceAll("\\*", "X");
                List<String> codeList = new ArrayList<>();
                codeList.add(codeRule);
                if(null==codeList || codeList.size()<1){
                    resultMap.put("errmsg", "号码格式错误：" + codeRule);
                    return resultMap;
                }
                String bai = codeRule.charAt(0)+"";
                String shi = codeRule.charAt(1)+"";
                String ge = codeRule.charAt(2)+"";
                BuyRecord3DVO vo = new BuyRecord3DVO();
                String buyDesc = "2D" + codeRule;
                vo.setHuizongName(buyDesc);
                vo.setBuyDesc(buyDesc);
                vo.setHuizongName(buyDesc);
                vo.setBai(bai.equals("X") ? "-" : bai);
                vo.setShi(shi.equals("X") ? "-" : shi);
                vo.setGe(ge.equals("X") ? "-" : ge);
                vo.setLmId(lmId);
                vo.setLsTypeId("1");
                vo.setValue("");
                vo.setBuyAmount(codeList.size());
                vo.setCodeList(codeList);
                vo.setBuyMoney(buyMoney);
                list.add(vo);
                resultMap.put("list", list);
                return resultMap;
            }else{
                if(codeRule.contains("百") || codeRule.contains("十") || codeRule.contains("个")) {
                    int shiIdx = codeRule.indexOf("十");
                    int geIdx = codeRule.indexOf("个");
                    int baiIdx = codeRule.indexOf("百");
                    String bai = null;
                    String shi = null;
                    String ge = null;
                    if (baiIdx > -1) {
                        if (shiIdx > -1) {
                            bai = codeRule.substring(baiIdx + 1, shiIdx);
                        } else if (geIdx > -1) {
                            bai = codeRule.substring(baiIdx + 1, geIdx);
                        } else {
                            bai = codeRule.substring(baiIdx + 1);
                        }
                    }
                    if (shiIdx > -1) {
                        if (geIdx > -1) {
                            shi = codeRule.substring(shiIdx + 1, geIdx);
                        } else {
                            shi = codeRule.substring(shiIdx + 1);
                        }
                    }
                    if (geIdx > -1) ge = codeRule.substring(geIdx + 1);

                    if (StringUtil.isNull(bai) && StringUtil.isNull(shi) && StringUtil.isNull(ge)) {
                        resultMap.put("errmsg", "号码格式错误：" + codeRule);
                        return resultMap;
                    }
                    if (StringUtil.isNull(bai)) bai = "-";
                    if (StringUtil.isNull(shi)) shi = "-";
                    if (StringUtil.isNull(ge)) ge = "-";
                    List<String> codeList = Code3DCreateUtils.ding2Code(bai, shi, ge);
                    if(null==codeList || codeList.size()<1){
                        resultMap.put("errmsg", "号码格式错误：" + codeRule);
                        return resultMap;
                    }
                    BuyRecord3DVO vo = new BuyRecord3DVO();
                    String buyDesc = "2D" + codeRule;
                    vo.setHuizongName(buyDesc);
                    vo.setBuyDesc(buyDesc);
                    vo.setHuizongName(buyDesc);
                    vo.setBai(bai);
                    vo.setShi(shi);
                    vo.setGe(ge);
                    vo.setLmId(lmId);
                    vo.setLsTypeId("1");
                    vo.setValue(codeRule);
                    vo.setBuyAmount(codeList.size());
                    vo.setCodeList(codeList);
                    vo.setBuyMoney(buyMoney);
                    list.add(vo);
                    resultMap.put("list", list);
                    return resultMap;
                }
            }
        }
        resultMap.put("errmsg", "号码格式错误：" + codeRule);
        return resultMap;
    }


    //猜2D
    public Map<String,Object>  c2dBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){


        Map<String,Object> resultMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        //codeRule = codeRule.replace("/[^[0-9]/g","");
        String newValue  = codeRule.replaceAll("[^0-9]","");
        String[] numArr = newValue.split("");
        //删除重复的数字
        Set<String> numsSet = Arrays.asList(numArr).stream().collect(Collectors.toSet());
        if(numsSet.size()==0){
            resultMap.put("errmsg","号码格式错误："+codeRule);
            return resultMap;
        }
        String nums = numsSet.stream().collect(Collectors.joining());
        List<String> codeList = Code3DCreateUtils.c2dCode(nums);

        BuyRecord3DVO vo = new BuyRecord3DVO();
        String buyDesc = "猜2D"+codeRule;
        vo.setHuizongName(buyDesc);
        vo.setBuyDesc(buyDesc);
        vo.setBai(nums);
        vo.setValue(nums);
        vo.setLmId(lmId);
        vo.setLsTypeId("2");
        vo.setBuyCodes(nums);
        vo.setBuyAmount(codeList.size());
        vo.setCodeList(codeList);
        vo.setBuyMoney(buyMoney);
        list.add(vo);
        resultMap.put("list",list);
        return resultMap;
    }



    //b3
    public Map<String,Object> b3Buy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){

        List<BuyRecord3DVO> buyList = Lists.newArrayList();
        Map<String,Object> resultMap = new HashMap<>();
        if(codeRule.contains("百") && codeRule.contains("十") && codeRule.contains("个")) {
            int shiIdx = codeRule.indexOf("十");
            int geIdx = codeRule.indexOf("个");
            int baiIdx = codeRule.indexOf("百");
            String bai = null;
            String shi = null;
            String ge = null;
            if (baiIdx > -1) {
                if (shiIdx > -1) {
                    bai = codeRule.substring(baiIdx + 1, shiIdx);
                } else if (geIdx > -1) {
                    bai = codeRule.substring(baiIdx + 1, geIdx);
                } else {
                    bai = codeRule.substring(baiIdx + 1);
                }
            }
            if (shiIdx > -1) {
                if (geIdx > -1) {
                    shi = codeRule.substring(shiIdx + 1, geIdx);
                } else {
                    shi = codeRule.substring(shiIdx + 1);
                }
            }
            if (geIdx > -1) ge = codeRule.substring(geIdx + 1);

            if (StringUtil.isNull(bai) && StringUtil.isNull(shi) && StringUtil.isNull(ge)) {
                resultMap.put("errmsg", "号码不能为空");
                return resultMap;
            }
            Set<String> baiSet = Arrays.asList(bai.split("")).stream().collect(Collectors.toSet());
            Set<String> shiSet = Arrays.asList(shi.split("")).stream().collect(Collectors.toSet());
            Set<String> geSet = Arrays.asList(ge.split("")).stream().collect(Collectors.toSet());
            for (String b : baiSet) {
                if (shiSet.contains(b) && geSet.contains(b)) {
                    ge = ge.replace(b, "");
                }
            }
            List<String> allB3Codes = Code3DCreateUtils.b3Code();
            List<String> b3CodeList = Code3DCreateUtils.b3Code(bai, shi, ge);
            List<String> validCodeList = Lists.newArrayList();
            b3CodeList.forEach(cc -> {
                if (allB3Codes.contains(cc)) {
                    validCodeList.add(cc);
                }
            });
            if(validCodeList.isEmpty()){
                resultMap.put("errmsg","未找到包选三号码");
                return resultMap;
            }
            BuyRecord3DVO vo = new BuyRecord3DVO();
            String buyDesc = "包选三" + codeRule;
            vo.setBuyDesc(buyDesc);
            vo.setHuizongName(buyDesc);
            vo.setBai(bai);
            vo.setShi(shi);
            vo.setGe(ge);
            vo.setCodeList(validCodeList);
            vo.setBuyAmount(validCodeList.size());
            vo.setValue(codeRule);
            vo.setLmId(lmId);
            vo.setLsTypeId("1");
            vo.setBuyMoney(buyMoney);
            buyList.add(vo);
            resultMap.put("list", buyList);

            return resultMap;
        }
        resultMap.put("errmsg","包选三号码格式错误："+codeRule);
        return resultMap;
    }


    //b6
    public Map<String,Object> b6Buy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> buyList = Lists.newArrayList();
        Map<String,Object> resultMap = new HashMap<>();

        if(codeRule.contains("百") && codeRule.contains("十") && codeRule.contains("个")){
            int shiIdx = codeRule.indexOf("十");
            int geIdx = codeRule.indexOf("个");
            int baiIdx = codeRule.indexOf("百");
            String bai = null;
            String shi = null;
            String ge = null;
            if(baiIdx>-1){
                if(shiIdx>-1){
                    bai = codeRule.substring(baiIdx+1,shiIdx);
                }else if(geIdx>-1){
                    bai = codeRule.substring(baiIdx+1,geIdx);
                }else{
                    bai = codeRule.substring(baiIdx+1);
                }
            }
            if(shiIdx>-1){
                if(geIdx>-1){
                    shi = codeRule.substring(shiIdx+1,geIdx);
                }else{
                    shi = codeRule.substring(shiIdx+1);
                }
            }
            if(geIdx>-1) ge = codeRule.substring(geIdx+1);

            if(StringUtil.isNull(bai) && StringUtil.isNull(shi) && StringUtil.isNull(ge)){
                resultMap.put("errmsg","号码不能为空");
                return resultMap;
            }
            Set<String> baiSet = Arrays.asList(bai.split("")).stream().collect(Collectors.toSet());
            Set<String> shiSet = Arrays.asList(shi.split("")).stream().collect(Collectors.toSet());
            Set<String> geSet = Arrays.asList(ge.split("")).stream().collect(Collectors.toSet());
            for(String b : baiSet){
                if(shi.contains(b)){
                    shi = shi.replace(b,"");
                }
                if(geSet.contains(b)){
                    ge = ge.replace(b,"");
                }
            }
            for(String s : shiSet){
                if(bai.contains(s)){
                    bai = bai.replace(s,"");
                }
                if(ge.contains(s)){
                    ge = ge.replace(s,"");
                }
            }
            List<String> allB6Codes = Code3DCreateUtils.b6Code();

            List<String> b6CodeList = Code3DCreateUtils.b6Code(bai,shi,ge);
            List<String> validCodeList = Lists.newArrayList();
            b6CodeList.forEach(cc->{
                if(allB6Codes.contains(cc)){
                    validCodeList.add(cc);
                }
            });
            if(validCodeList.isEmpty()){
                resultMap.put("errmsg","未找到包选六号码");
                return resultMap;
            }
            BuyRecord3DVO vo = new BuyRecord3DVO();
            String buyDesc = "包选六"+codeRule;
            vo.setBuyDesc(buyDesc);
            vo.setHuizongName(buyDesc);
            vo.setCodeList(validCodeList);
            vo.setBuyAmount(validCodeList.size());
            vo.setValue(codeRule);
            vo.setBai(bai);
            vo.setShi(shi);
            vo.setGe(ge);
            vo.setLmId(lmId);
            vo.setLsTypeId("2");
            vo.setBuyMoney(buyMoney);
            buyList.add(vo);
            resultMap.put("list",buyList);
            return resultMap;

        }

        resultMap.put("errmsg","包选六号码格式错误");
        return resultMap;
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
