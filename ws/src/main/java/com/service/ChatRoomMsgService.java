package com.service;

import cn.hutool.core.date.DateUtil;
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
import com.dao.PlayerDAO;
import com.mysql.jdbc.StringUtils;
import com.util.Code3DCreateUtils;
import com.util.StringUtil;
import com.util.Tools;
import com.vo.BuyRecord3DVO;
import com.vo.WechatPushMsgVo;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Lazy
public class ChatRoomMsgService extends ServiceImpl<ChatRoomMsgDAO, ChatRoomMsg> {

    @Autowired
    private ChatRoomMsgDAO dataDao;

    @Autowired
    private PlayerPointsRecordService playerPointsRecordService;

    @Autowired
    private PlayerReturnPointsService playerReturnPointsService;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private BuyRecord3DServiceV2 buyRecord3DServiceV2;

    @Autowired
    private DrawService drawService;

    @Autowired
    private P3DrawService p3DrawService;

    @Autowired
    private ReportToPanService reportToPanService;

    @Autowired
    private ThreadPoolExecutor threadPool;

    @Autowired
    private WechatApiService wechatApiService;

    @Autowired
    private BotUserService botUserService;

    @Autowired
    private BotUserSettingService botUserSettingService;

    @Autowired
    private KuaidaBuyMsgServiceV2 kuaidaBuyMsgServiceV2;

    @Autowired
    private KuaidaMultiBuyMsgServiceV2 kuaidaMultiBuyMsgServiceV2;

    @Autowired
    private KuaixuanBuyMsgServiceV2 kuaixuanBuyMsgServiceV2;

    @Autowired
    private BotUserPanService botUserPanService;


    public void sendWechatBackMsg(String userId, ResponseBean toMsg){
        simpMessagingTemplate.convertAndSend("/topic/wechat/"+userId,toMsg);
    }


    public void sendbackMsg(String userId,ChatRoomMsg toMsg){

        simpMessagingTemplate.convertAndSend("/topic/room/"+userId,toMsg);
        BotUser botUser = botUserService.getById(userId);
        //列表中设置的类型对应的消息，是不会推送给微信玩家的。
        List<Integer> excludeTypes = Lists.newArrayList(10,11);
        if(!excludeTypes.contains(toMsg.getOptType())){
            if(toMsg.getToUserType()==1 && StringUtil.isNotNull(botUser.getWxId())){
                //发给所有人的消息
                List<Player> wxPlayerList = playerService.getWxPlayerListBy(userId);
                if(null!=wxPlayerList && wxPlayerList.size()>0){
                    for(Player player : wxPlayerList){
                        if(null!=player.getLotteryType() && (player.getLotteryType()==3 || player.getLotteryType()==1)){
                            threadPool.execute(()->{
                                ChatRoomMsg msg1 = toMsg;
                                msg1.setToUserNick(player.getNickname());
                                if(msg1.getMsgType()==0){
                                    if (player.getChatStatus() == 1){
                                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
                                    }else{
                                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                                    }
//                                    wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),msg1.getMsg());
                                }else{
                                    if (player.getChatStatus() == 1){
                                        wechatApiService.sendImageGroup(player.getWxFriendId(),botUser.getWxId(),msg1.getMsg(),player.getWxGroup(),player.getNickname());
                                    }else{
                                        wechatApiService.sendImage(player.getWxFriendId(),botUser.getWxId(),msg1.getMsg());
                                    }
//                                    wechatApiService.sendImage(player.getWxFriendId(),botUser.getWxId(),msg1.getMsg());
                                }
                            });
                        }
                    }
                }
            }
        }
    }


    public ChatRoomMsg createMsg(String botUserId,String msg,Integer msgType){
        ChatRoomMsg toMsg = new ChatRoomMsg();
        toMsg.setFromUserId(botUserId);
        toMsg.setFromUserNick("机器人");
        toMsg.setFromUserType(1);
        toMsg.setMsg(msg);
        toMsg.setMsgType(msgType);
        toMsg.setToUserNick("");
        toMsg.setToUserId("0");
        toMsg.setToUserType(1);
        toMsg.setCreateTime(new Date());
        toMsg.setBotUserId(botUserId);
        toMsg.setOptType(0);
        return toMsg;
    }

    public ChatRoomMsg createMsg(String botUserId,String msg){
        ChatRoomMsg toMsg = new ChatRoomMsg();
        toMsg.setFromUserId(botUserId);
        toMsg.setFromUserNick("机器人");
        toMsg.setFromUserType(1);
        toMsg.setMsg(msg);
        toMsg.setMsgType(0);
        toMsg.setToUserNick("");
        toMsg.setToUserId("0");
        toMsg.setToUserType(1);
        toMsg.setCreateTime(new Date());
        toMsg.setBotUserId(botUserId);
        toMsg.setOptType(0);
        return toMsg;
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
        return toMsg;
    }


    public List<ChatRoomMsg> listLastN(String botUserId,Integer size) {
        LambdaQueryWrapper<ChatRoomMsg> qw = new LambdaQueryWrapper<>();
        qw.eq(ChatRoomMsg::getBotUserId,botUserId);
        qw.eq(ChatRoomMsg::getIsShow,1);
        qw.orderByDesc(ChatRoomMsg::getId);
        qw.last("limit "+size);
        return dataDao.selectList(qw);
    }


    public void handleMsg(BotUser botUser, ChatRoomMsg msg,Player player){
        try {
            Draw draw = drawService.getLastDrawInfo();
            Draw p3Draw = p3DrawService.getLastDrawInfo();
            String userId = botUser.getId();
            String content = msg.getMsg();
            msg.setCreateTime(new Date());
            msg.setBotUserId(botUser.getId());
            if (msg.getFromUserType() == 0) {
                dataDao.insert(msg);
                simpMessagingTemplate.convertAndSend("/topic/room/" + userId, msg);
                int lottype = -1;

                Integer optType = msg.getOptType();
                if(optType==0){
                    boolean isCommonCmd = false;
                    for(String cmd : GlobalConst.commonCmd){ //判断是否为通用指令，如上分
                        if(content.startsWith(cmd)){
                            isCommonCmd = true;
                            break;
                        }
                    }
                    if (isCommonCmd) {
                        handleCommonMsg(botUser,msg,player);
                        return;
                    }
                }

                if ((content.contains("3D") && content.contains("P3")) || (content.contains("福") && content.contains("体"))){
                    String fcContent = content.replaceAll("P3","").replaceAll("体","");
                    String tcContent = content.replaceAll("3D","").replaceAll("福","");
                    String[] cp = new String[]{fcContent,tcContent};
                    for (String con : cp){
                        msg.setMsg(con);
                        if (extracted(botUser, msg, player, optType, draw, p3Draw, lottype)) return;
                    }
                }else{
                    if (extracted(botUser, msg, player, optType, draw, p3Draw, lottype)) return;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            ChatRoomMsg toMsg = createMsg(botUser,player,"系统繁忙，请稍后重试");
            dataDao.insert(toMsg);
            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                //wechatIpadTokenService.sendMsgToFriend(toMsg);
//                if (player.getChatStatus() == 1){
//                    wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                }else{
//                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                }
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
            }
        }
    }

    private boolean extracted(BotUser botUser, ChatRoomMsg msg, Player player, Integer optType, Draw draw, Draw p3Draw, int lottype) {
        if(optType ==4){
            if (draw.getOpenStatus() == 1 || p3Draw.getOpenStatus()==1) {
                tuima(msg, botUser, player);
                return true;
            }
        }

        String text = msg.getMsg().toUpperCase();
        Boolean checkTxtResult = true;
        String[] multiArr = text.split("\n|\r");
        for(String cmdText : multiArr){
            if(StringUtils.isNullOrEmpty(cmdText)){
                continue;
            }
            String cmdPrefix = cmdText.toUpperCase();
            if(cmdPrefix.contains("P3") || cmdPrefix.contains("3D")
                    || cmdPrefix.contains("福")
                    || cmdPrefix.contains("体")){
                if (null == player.getLotteryType()) {
                    ChatRoomMsg toMsg = createMsg(botUser, player, "请先开通排列三或福彩3D服务");
                    dataDao.insert(toMsg);
                    simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                    return true;
                }
                if(cmdPrefix.contains("P3") || cmdPrefix.contains("体")){
                    lottype = 2;
                }else{
                    lottype = 1;
                }
                //lottype = cmdText.startsWith("P3") ? 2 : 1;
                if(lottype ==2){
                    if(p3Draw.getOpenStatus()!=1){
                        ChatRoomMsg toMsg = createMsg(botUser, player, "【P3】^^★★★停止-上课★★★");
                        dataDao.insert(toMsg);
                        simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                        return true;
                    }
                }else{
                    if(draw.getOpenStatus()!=1){
                        ChatRoomMsg toMsg = createMsg(botUser, player, "【3D】^^★★★停止-上课★★★");
                        dataDao.insert(toMsg);
                        simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                        return true;
                    }
                }

                if (player.getLotteryType() != 3) {
                    if (player.getLotteryType() != lottype) {
                        ChatRoomMsg toMsg = createMsg(botUser, player, "哦噢，您无提交" + (lottype == 2 ? "P3" : "3D") + "作业的权限");
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
                        return true;
                    }
                }
            }else{
                checkTxtResult = false;
                break;
            }
        }

        if(checkTxtResult){
            switch (optType) {
                case 0:
                    for(String cmdText : multiArr){
                        String text1 = cmdText.toUpperCase();
                        String txt = "";
                        if(text1.contains("P3") || text1.contains("3D")
                                || text1.contains("福")
                                || text1.contains("体") ){
                            ChatRoomMsg childMsg = createMsg(botUser, player,text1);
                            if(text1.contains("P3") || text1.contains("体")){
                                lottype = 2;
                            }else{
                                lottype = 1;
                            }
                            //lottype = text1.startsWith("P3")?2:1;
                            String lotName = lottype ==2?"P3":"3D";

                            if(text1.startsWith("P3") || text1.startsWith("3D")){
                                txt = text1.substring(2);
                            }else{
                                if (text1.startsWith("福") || text1.startsWith("体")) {
                                    txt = text1.substring(1);
                                }else {
                                    txt = text1.replaceAll("福", "");
                                    txt = txt.replaceAll("体", "");
                                }
                            }

                            if(player.getLotteryType()==3 || player.getLotteryType()== lottype){

                                //String txt = text1.substring(2);
                                boolean isBuy = false;
                                for(String word : GlobalConst.keywords2){
                                    if(txt.startsWith(word) || txt.contains(word)){
                                        kuaidaBuyMsgServiceV2.kuaidaBuy(childMsg, botUser, player, lottype);
                                        isBuy = true;
                                        break;
                                    }
                                }
                                if (!isBuy) {
                                    ChatRoomMsg toMsg = createMsg(botUser, player, "格式错误");
                                    dataDao.insert(toMsg);
                                    simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                                    if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
//                                                if (player.getChatStatus() == 1){
//                                                    wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                                                }else{
//                                                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                                                }
//                                                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                                    }
                                }
                            }else{
                                ChatRoomMsg toMsg = createMsg(botUser, player,"哦噢，您无提交"+lotName+"作业的权限");
                                dataDao.insert(toMsg);
                                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
//                                            if (player.getChatStatus() == 1){
//                                                wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                                            }else{
//                                                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                                            }
//                                            wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                                }
                            }
                        }
                    }
                    break;
                case 1:
                case 90:
                    if(lottype >0){
                        kuaixuanBuyMsgServiceV2.handleMsg(msg, botUser, player, lottype);
                    }
                    break;
                case 91: //多组下注，不进行文本解析
                    if(lottype >0){
                        kuaidaMultiBuyMsgServiceV2.kuaidaBuyForMultiGroup(msg, botUser, player, lottype);
                    }
                    break;


            }
        }
        return false;
    }


    //处理通用指令消息
    public void handleCommonMsg(BotUser botUser, ChatRoomMsg msg,Player player){
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

        String content = msg.getMsg();
        if (content.startsWith("上分") || content.startsWith("下分")) {
            upDownPoints(msg, botUser, player);
            return;
        }

        if (content.startsWith("作业格式")) {
            String url = player.getChaturl();
            if(url.endsWith("/")){
                url = player.getChaturl() + "sendFormat.html";
            }else{
                url = player.getChaturl() + "/sendFormat.html";
            }

            String text = "点此查看\n\r" + url;
            ChatRoomMsg toMsg = createMsg(botUser, player, text);
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
            return;
        }

        if (content.startsWith("流水")) {
            //返回当期流水
            BigDecimal totalBuyPoints = BigDecimal.ZERO;
            BigDecimal p1 = playerBuyRecordService.sumTotalBuy(player.getId(),draw.getDrawId(),1);
            if(null !=p1){
                totalBuyPoints = totalBuyPoints.add(p1);
            }
            BigDecimal p2 = playerBuyRecordService.sumTotalBuy(player.getId(),p3Draw.getDrawId(),2);
            if(null !=p2){
                totalBuyPoints = totalBuyPoints.add(p2);
            }
            String text = "【累计流水】" + totalBuyPoints.stripTrailingZeros().toPlainString();
            ChatRoomMsg toMsg = createMsg(botUser, player, text);
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
            return;
        }

        if (content.startsWith("盈亏")) {
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
            String text = "YK：" + totalEarnPoints.stripTrailingZeros().toPlainString();
            ChatRoomMsg toMsg = createMsg(botUser, player, text);
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
            return;
        }


        if (content.startsWith("盛鱼")) {
            String text = "【当前盛鱼】" +player.getPoints().stripTrailingZeros().toPlainString();
            ChatRoomMsg toMsg = createMsg(botUser, player, text);
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
            return;
        }

        if (content.startsWith("查")) {
            List<PlayerBuyRecord> allList = Lists.newArrayList();
            List<PlayerBuyRecord> buyList = playerBuyRecordService.getListBy(player.getId(),draw.getDrawId(),1);
            List<PlayerBuyRecord> buyList2 = playerBuyRecordService.getListBy(player.getId(),p3Draw.getDrawId(),2);
            String text = null;
            if(null!=buyList && buyList.size()>0) {
                allList.addAll(buyList);
            }
            if(null!=buyList2 && buyList2.size()>0) {
                allList.addAll(buyList2);
            }
            if(allList.size()>0){
                StringBuffer sb = new StringBuffer();
                for (PlayerBuyRecord playerBuyRecord : allList) {
                    sb.append(playerBuyRecord.getBuyDesc().replace("\r\n","")).append("\r\n");
                }
                text = "【当前作业】\r\n" + sb.toString() + "【状态】交作业成功√√\r\n"
                        + "【当前盛鱼】" + player.getPoints().stripTrailingZeros().toPlainString();
            }else{
                text = "【当前作业】未提交任何作业哦\r\n" + "【当前盛鱼】" + player.getPoints().stripTrailingZeros().toPlainString();

            }
            ChatRoomMsg toMsg = createMsg(botUser, player, text);
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
            return;
        }

        if (content.startsWith("返")) {
            //机器人开启了玩家自助返水
            if(null!=botUserSetting && null!=botUserSetting.getHsHelpBack() && botUserSetting.getHsHelpBack()==1){

                switch(player.getLotteryType()){
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
                ChatRoomMsg toMsg = createMsg(botUser, player, "回水功能未开启");
                simpMessagingTemplate.convertAndSend("/topic/room/" + botUser.getId(), toMsg);
                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
//                    if (player.getChatStatus() == 1){
//                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                    }else{
//                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                }
            }
        }

    }

    //退码
    public void tuima(ChatRoomMsg msg,BotUser botUser,Player player){
        ChatRoomMsg toMsg = null;
        Draw draw = null;
        String playerBuyId = msg.getMsg();
        PlayerBuyRecord record = playerBuyRecordService.getById(playerBuyId);
        if(null == record){
            toMsg = createMsg(botUser,player,"退米失败:订单不存在");
            dataDao.insert(toMsg);
            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                //wechatIpadTokenService.sendMsgToFriend(toMsg);
//                if (player.getChatStatus() == 1){
//                    wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                }else{
//                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                }
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
            }
            return;
        }
        if(record.getBuyStatus()==-1){
            toMsg = createMsg(botUser,player,"此订单已退米");
            dataDao.insert(toMsg);
            simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
            if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                //wechatIpadTokenService.sendMsgToFriend(toMsg);
//                if (player.getChatStatus() == 1){
//                    wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                }else{
//                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                }
//                wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
            }
            return;
        }
        int lotteryType = record.getLotteryType();
        if(lotteryType == 2){
            draw = p3DrawService.getLastDrawInfo();
        }else{
            draw = drawService.getLastDrawInfo();
        }
        String lotName = lotteryType==2?"P3":"3D";
        Map<String,Object> buyResult = null;
        if(record.getBuyType()==0){
            BotUserPan botUserPan = botUserPanService.getOneBy(botUser.getId(),lotteryType);
            if(null == botUserPan){
                toMsg = createMsg(botUser,player,"退米失败:参数错误");
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
                return;
            }
            if(StringUtil.isNull(botUserPan.getLogin3dToken())){
                toMsg = createMsg(botUser,player,"机器人未登录"+lotName+"网盘");
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
                return;
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

            switch (reportRespData.getCode()){
                case 403: //盘口登录token已失效
                    botUserPanService.clearPanInfo(botUser.getId(),lotteryType);
                    toMsg = createMsg(botUser,player,"机器人未登录"+lotName+"网盘");
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
                    break;
                case -1:
                case 500:
                    String errmsg = reportRespData.getMsg();
                    toMsg = createMsg(botUser,player,errmsg);
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
                    break;
                case 0:
                    //操作成功
                    buyResult = buyRecord3DServiceV2.tuima(player,playerBuyId,draw);
                    break;
            }

        }else{
            buyResult = buyRecord3DServiceV2.tuima(player,playerBuyId,draw);
        }
        if(null!=buyResult){
            Integer errcode = (Integer)buyResult.get("errcode");
            String errmsg = (String)buyResult.get("errmsg");
            if(-1==errcode){
                toMsg = createMsg(botUser,player,errmsg);
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                    //wechatIpadTokenService.sendMsgToFriend(toMsg);
//                    if (player.getChatStatus() == 1){
//                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                    }else{
//                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                }
            }else{
                BigDecimal points = playerService.getPoints(player.getId());
                BigDecimal backPoints = record.getBuyPoints();
                String newmsg = "["+record.getBuyDesc()+"]"+"\r\n[此单已退码]\r\n【退面款】："+backPoints.stripTrailingZeros().toPlainString()+"\r\n"+"【当前盛鱼】："+points.stripTrailingZeros().toPlainString()+"\r\n";
                toMsg = new ChatRoomMsg();
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
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/"+botUser.getId(),toMsg);
                if(player.getUserType()==2  && StringUtil.isNotNull(botUser.getWxId())){
                    //wechatIpadTokenService.sendMsgToFriend(toMsg);
//                    if (player.getChatStatus() == 1){
//                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                    }else{
//                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
                }
            }
        }

    }

    //上下分
    public void upDownPoints(ChatRoomMsg msg,BotUser botUser,Player player){
        String userId = botUser.getId();
        try {

            String content = msg.getMsg();
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
                ChatRoomMsg toMsg = getErrorMsg(botUser, player);
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/" + userId, toMsg);
                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                    //wechatIpadTokenService.sendMsgToFriend(toMsg);
//                    if (player.getChatStatus() == 1){
//                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                    }else{
//                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                }
            } else {
                Long dotCount = Arrays.stream(points.split("")).filter(c->c.equals(".")).count();
                if(null!=dotCount && dotCount>1){
                    ChatRoomMsg toMsg = createMsg(botUser, player, "分值格式错误");
                    dataDao.insert(toMsg);
                    simpMessagingTemplate.convertAndSend("/topic/room/" + userId, toMsg);
                    if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                        //wechatIpadTokenService.sendMsgToFriend(toMsg);
//                        if (player.getChatStatus() == 1){
//                            wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                        }else{
//                            wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                        }
//                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                    }
                    return;
                }
                if (optType == 1) {
                    if (player.getPoints().compareTo(new BigDecimal(points)) < 0) {
                        //可用积分小于下分值
                        ChatRoomMsg toMsg = createMsg(botUser, player, "面上不足");
                        dataDao.insert(toMsg);
                        simpMessagingTemplate.convertAndSend("/topic/room/" + userId, toMsg);
                        if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                            //wechatIpadTokenService.sendMsgToFriend(toMsg);
//                            if (player.getChatStatus() == 1){
//                                wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                            }else{
//                                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                            }
//                            wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                        }
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
//            if(optType==0){
//                playerService.updatePoint(player.getId(),new BigDecimal(points),true);
//            }else{
//                playerService.updatePoint(player.getId(),new BigDecimal(points),false);
//            }
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
                dataDao.insert(toMsg);
                simpMessagingTemplate.convertAndSend("/topic/room/" + userId, toMsg);
                if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                    //wechatIpadTokenService.sendMsgToFriend(toMsg);
//                    if (player.getChatStatus() == 1){
//                        wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                    }else{
//                        wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                    }
//                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            ChatRoomMsg toMsg = createMsg(botUser,player,"系统繁忙，请稍后重试");
            dataDao.insert(toMsg);
            simpMessagingTemplate.convertAndSend("/topic/room/" + userId, toMsg);
            if (player.getUserType() == 2 && StringUtil.isNotNull(botUser.getWxId())) {
                //wechatIpadTokenService.sendMsgToFriend(toMsg);
//                if (player.getChatStatus() == 1){
//                    wechatApiService.sendMsgGroup(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg(),player.getWxGroup(),player.getNickname());
//                }else{
//                    wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
//                }
//                wechatApiService.sendMsg(player.getWxFriendId(), botUser.getWxId(), toMsg.getMsg());
            }
        }
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
            ChatRoomMsg toMsg = createMsg(botUser, player, buffer.toString());
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


}
