package com.service;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.beans.*;
import com.task.HeatBeatTask;
import com.util.Code3DCreateUtils;
import com.util.StringUtil;
import com.vo.BuyRecord3DVO;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DummyService {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private DrawService drawService;

    @Autowired
    private P3DrawService p3DrawService;

    @Autowired
    private ChatRoomMsgService chatRoomMsgService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private BuyRecord3DService buyRecord3DService;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    private Logger logger = LoggerFactory.getLogger(DummyService.class);

    public void dummyBuy(BotUser botUser,List<LotteryMethod> lmList,List<LotterySetting> lsList){
        logger.info(">>>>>>>>>>"+botUser.getLoginName()+":执行假人投注");
        Random random = new Random();
        try {
            List<Player> dummyList = playerService.dummyList(botUser.getId());
            if (dummyList.size() > 0) {
                int idx = random.nextInt(dummyList.size());

                Player player = dummyList.get(idx);

                //BotUserSetting botUserSetting = botUserSettingService.getByUserId(botUser.getId());
                if (player.getPoints().compareTo(BigDecimal.valueOf(1000)) < 0) {//上分
                    logger.info(">>>>>>>>>>>>>>>>>>>>>"+String.format("%s的假人玩家:%s 上分1000....",botUser.getLoginName(),player.getNickname()));
                    int points = 1000;
                    ChatRoomMsg fromMsg = chatRoomMsgService.createFromWxMsg(botUser,player,"上分1000");
                    fromMsg.setSource(0);
                    chatRoomMsgService.save(fromMsg);
                    rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(fromMsg));

                    playerService.updatePoint(player.getId(), BigDecimal.valueOf(points), true);

                    BigDecimal totalPoints = player.getPoints().add(BigDecimal.valueOf(points));

                    String tlp = "【%s】：%s\r\n【当前盛鱼】：%s";
                    String msgtxt = "后台上分";
                    msgtxt = String.format(tlp, msgtxt, points, totalPoints.stripTrailingZeros().toPlainString());
                    ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser,player,msgtxt);
                    chatRoomMsgService.save(toMsg);
                    rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(toMsg));

                } else if (player.getPoints().compareTo(BigDecimal.valueOf(10000)) > 0) { //下分
                    logger.info(">>>>>>>>>>>>>>>>>>>>>"+String.format("%s的假人玩家:%s 下分5000....",botUser.getLoginName(),player.getNickname()));
                    int points = 5000;
                    ChatRoomMsg fromMsg = chatRoomMsgService.createFromWxMsg(botUser,player,"下分5000");
                    fromMsg.setSource(0);
                    //小于1000分
//                    ChatRoomMsg chatRoomMsg = new ChatRoomMsg();
//                    chatRoomMsg.setMsg("下分5000");
//                    chatRoomMsg.setMsgType(0);
//                    chatRoomMsg.setFromUserId(player.getId());
//                    chatRoomMsg.setFromUserNick(player.getNickname());
//                    chatRoomMsg.setFromUserImg(player.getHeadimg());
//                    chatRoomMsg.setFromUserType(0);
//                    chatRoomMsg.setToUserId(player.getBotUserId());
//                    chatRoomMsg.setOptType(0);
//                    chatRoomMsg.setCreateTime(new Date());
//                    chatRoomMsg.setBotUserId(botUser.getId());
//                    chatRoomMsg.setToUserType(2);
                    chatRoomMsgService.save(fromMsg);
                    rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(fromMsg));

                    playerService.updatePoint(player.getId(), BigDecimal.valueOf(points), false);
                    BigDecimal totalPoints = player.getPoints().subtract(BigDecimal.valueOf(points));

                    String tlp = "【%s】：%s\r\n【当前盛鱼】：%s";
                    String msgtxt = "后台下分";
                    msgtxt = String.format(tlp, msgtxt, points, totalPoints.stripTrailingZeros().toPlainString());
//                    ChatRoomMsg toMsg = new ChatRoomMsg();
//                    toMsg.setFromUserId(player.getBotUserId());
//                    toMsg.setFromUserNick("机器人");
//                    toMsg.setFromUserType(1);
//                    toMsg.setMsgType(0);
//                    toMsg.setMsg(msgtxt);
//                    toMsg.setToUserNick(player.getNickname());
//                    toMsg.setToUserId(player.getId());
//                    toMsg.setCreateTime(new Date());
//                    toMsg.setBotUserId(player.getBotUserId());
//                    toMsg.setToUserType(0);
//                    toMsg.setOptType(0);
                    ChatRoomMsg toMsg = chatRoomMsgService.createMsg(botUser,player,msgtxt);
                    chatRoomMsgService.save(toMsg);
                    rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(toMsg));

                } else {
                    if(null == player.getLotteryType()){
                        player.setLotteryType(botUser.getLotteryType());
                        playerService.updateById(player);
                    }
                    Draw draw = null;
                    int lottype = 0;
                    String lotteryName="3D";
                    if(player.getLotteryType()==1){
                        draw = drawService.getLastDrawInfo();
                        lottype = 1;
                        lotteryName="3D";
                    }else if(player.getLotteryType()==2){
                        draw = p3DrawService.getLastDrawInfo();
                        lottype = 2;
                        lotteryName="P3";
                    }else{
                        int tt = random.nextInt(10);
                        if(tt%2==0){
                            draw = p3DrawService.getLastDrawInfo();
                            lottype = 2;
                            lotteryName="P3";
                        }else{
                            draw = drawService.getLastDrawInfo();
                            lottype = 1;
                            lotteryName="3D";
                        }
                    }
                    if (draw.getOpenStatus() == 1) {
                        //已开盘
                        LotteryMethod lm = lmList.get(random.nextInt(lmList.size()));
                        List<LotterySetting> lotterySettingList = lsList.stream().filter(item -> item.getLotteryMethodId().equals(lm.getId())).collect(Collectors.toList());

                        LotterySetting ls = null;
                        if(lotterySettingList.size()>0){
                            if(lotterySettingList.size()==1){
                                ls = lotterySettingList.get(0);
                            }else{
                                ls = lotterySettingList.get(random.nextInt(lotterySettingList.size()));
                            }
                        }

                        if(null == ls){
                            return;
                        }

                        int count = 1;
                        List<BuyRecord3DVO> list = Lists.newArrayList();
                        for (int i = 0; i < count; i++) {
                            List<BuyRecord3DVO> records = createDummyBuyRecords(lm, ls);
                            list.addAll(records);
                        }

                        if (list.size() > 0) {
                            String info = String.format("[%s]的假人玩家:%s 购买[%s][%s][%s]",botUser.getLoginName(),player.getNickname(),lotteryName,lm.getBettingMethod(),ls.getBettingRule());
                            logger.info(">>>>>>>>>>>>>>>>>>>>>"+info);

                            String buyDesc = (lottype==2?"P3":"3D") + list.get(0).getBuyDesc()+"各"+list.get(0).getBuyMoney();

                            ChatRoomMsg chatRoomMsg = new ChatRoomMsg();
                            chatRoomMsg.setMsg(buyDesc);
                            chatRoomMsg.setMsgType(0);
                            chatRoomMsg.setFromUserId(player.getId());
                            chatRoomMsg.setFromUserNick(player.getNickname());
                            chatRoomMsg.setFromUserImg(player.getHeadimg());
                            chatRoomMsg.setFromUserType(0);
                            chatRoomMsg.setToUserId(player.getBotUserId());
                            chatRoomMsg.setOptType(1);
                            chatRoomMsg.setCreateTime(new Date());
                            chatRoomMsg.setBotUserId(botUser.getId());
                            chatRoomMsg.setToUserType(2);
                            chatRoomMsg.setKuaixuanRule(JSON.toJSONString(list));
                            chatRoomMsgService.save(chatRoomMsg);
                            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(chatRoomMsg));
                            Map<String, Object> buyResult = null;
                            if ("5".equals(lm.getId()) || "13".equals(lm.getId())) {
                                buyResult = buyRecord3DService.buy3dHs(player, list, draw, -10, null, chatRoomMsg.getMsg(),lottype);
                            } else {
                                buyResult = buyRecord3DService.codesBatchBuy(player, list, draw, -10, null, chatRoomMsg.getMsg(),lottype);
                            }
                            if (buyResult.containsKey("playerBuyId")) {
                                String playerBuyId = (String) buyResult.get("playerBuyId");
                                PlayerBuyRecord record = playerBuyRecordService.getById(playerBuyId);
                                record.setBuyType(2);
                                record.setBuyFrom(-1);
                                playerBuyRecordService.updateById(record);
                                BigDecimal points = playerService.getPoints(player.getId());
                                String newmsg = "["+lotteryName+"课号]" + draw.getDrawId() + "\r\n" + chatRoomMsg.getMsg() + "\r\n交作业成功√√\r\n【份数】：" + record.getBuyAmount() + "\r\n"
                                        + "【扣面】：" + record.getBuyPoints().stripTrailingZeros().toPlainString() + "\r\n" + "【盛鱼】：" + points.stripTrailingZeros().toPlainString();
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
                                toMsg.setPlayerBuyId(null);
                                toMsg.setOptType(1);
                                toMsg.setMsgType(0);
                                chatRoomMsgService.save(toMsg);
                                rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(toMsg));
                            } else {
                                String errmsg = (String) buyResult.get("errmsg");
                                if (StringUtil.isNotNull(errmsg)) {
                                    ChatRoomMsg toMsg = new ChatRoomMsg();
                                    toMsg.setFromUserId(botUser.getId());
                                    toMsg.setFromUserNick("机器人");
                                    toMsg.setFromUserType(1);
                                    toMsg.setMsg(errmsg);
                                    toMsg.setMsgType(0);
                                    toMsg.setToUserType(0);
                                    toMsg.setToUserNick(player.getNickname());
                                    toMsg.setToUserId(player.getId());
                                    toMsg.setCreateTime(new Date());
                                    toMsg.setBotUserId(botUser.getId());
                                    toMsg.setOptType(0);
                                    chatRoomMsgService.save(toMsg);
                                    rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(toMsg));
                                }
                            }
                        }else{
                            String info = String.format("[%s]的假人玩家:%s 未找到号码[%s][%s][%s]",botUser.getLoginName(),player.getNickname(),lotteryName,lm.getBettingMethod(),ls.getBettingRule());
                            logger.info(">>>>>>>>>>"+info);
                        }

                    }else{
                        String info = String.format("[%s]的假人玩家:%s 已停止购买",botUser.getLoginName(),player.getNickname());
                        logger.info(">>>>>>>>>>"+info);
                    }

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private  List<String> getNums(int len,String[] numsArr){
        if(len>0){
            Random random = new Random();
            Set<String> numSet = Sets.newHashSet();
            while(true){
                String num = numsArr[random.nextInt(numsArr.length)];
                numSet.add(num);
                if(numSet.size()>=len){
                    break;
                }
            }
            List<String> numList = numSet.stream().collect(Collectors.toList());
            Collections.sort(numList);
            return numList;
        }else{
            return Lists.newArrayList();
        }
    }


    public List<BuyRecord3DVO> randomZx(LotteryMethod lm , LotterySetting ls){
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String[] numsArr = {"0","1","2","3","4","5","6","7","8","9"};
        String[] hzArr = {"0","1","2","3","4","5","6","7","8","9","10","11",
                "12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27"};
        if(lsType==1){
            int numCount = random.nextInt(5);
            if(numCount==0){
                numCount=1;
            }
            List<String> numList = getNums(numCount,numsArr);
            //普通
            String bai = numList.stream().collect(Collectors.joining());

            numList.clear();
            numCount = random.nextInt(5);
            if(numCount==0){
                numCount=1;
            }
            numList = getNums(numCount,numsArr);
            String shi = numList.stream().collect(Collectors.joining());

            numList.clear();
            numCount = random.nextInt(5);
            if(numCount==0){
                numCount=1;
            }
            numList = getNums(numCount,numsArr);
            String ge = numList.stream().collect(Collectors.joining());

            String hzname = "";
            int sum = bai.length() * shi.length() * ge.length();
            if(sum==1){
                hzname = "直选单式";
            }else{
                hzname = "直选复式";
            }
            String buyDesc = "直选普通"+bai+","+shi+","+ge;
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBai(bai);
            oneRecord.setShi(shi);
            oneRecord.setGe(ge);
            oneRecord.setBuyAmount(sum);
            oneRecord.setValue(bai+","+shi+","+ge);
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(String.valueOf(lsType));
            oneRecord.setBuyDesc(buyDesc);
            oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
            recordList.add(oneRecord);

        }else if(lsType==2){
            int numCount = random.nextInt(8);
            if(numCount==0){
                numCount=1;
            }

            List<String> hzList = getNums(numCount,numsArr);
            List<String> codeList = Code3DCreateUtils.hezhi(hzList);
            String hzname = "直选和值";
            String value = hzList.stream().collect(Collectors.joining(","));
            String buyDesc = "直选和值"+value;
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setHzList(hzList);
            oneRecord.setValue(value);
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(String.valueOf(lsType));
            oneRecord.setBuyDesc(buyDesc);
            oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
            recordList.add(oneRecord);
            //和值
        }
        return recordList;
    }


    public List<BuyRecord3DVO> randomTx(LotteryMethod lm ,LotterySetting ls){
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String[] numsArr = {"0","1","2","3","4","5","6","7","8","9"};
        int numCount = random.nextInt(5);
        if(numCount==0){
            numCount=1;
        }
        List<String> numList = getNums(numCount,numsArr);
        //普通
        String bai = numList.stream().collect(Collectors.joining());

        numList.clear();
        numCount = random.nextInt(5);
        if(numCount==0){
            numCount=1;
        }
        numList = getNums(numCount,numsArr);
        String shi = numList.stream().collect(Collectors.joining());

        numList.clear();
        numCount = random.nextInt(5);
        if(numCount==0){
            numCount=1;
        }
        numList = getNums(numCount,numsArr);
        String ge = numList.stream().collect(Collectors.joining());

        String hzname = "";
        int sum = bai.length() * shi.length() * ge.length();
        if(sum==1){
            hzname = "通选单式";
        }else{
            hzname = "通选复式";
        }
        String buyDesc = "复式"+bai+","+shi+","+ge;
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBai(bai);
        oneRecord.setShi(shi);
        oneRecord.setGe(ge);
        oneRecord.setBuyAmount(sum);
        oneRecord.setValue(bai+","+shi+","+ge);
        oneRecord.setLmId(lm.getId());
        oneRecord.setBuyDesc(buyDesc);
        oneRecord.setLsTypeId(String.valueOf(lsType));
        oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
        recordList.add(oneRecord);
        return recordList;
    }


    public List<BuyRecord3DVO> randomZ3(LotteryMethod lm ,LotterySetting ls) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String[] numsArr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        if (lsType == 1) {
            //组三普通
            int numCount = random.nextInt(5);
            if (numCount < 2) {
                numCount = 2;
            }
            List<String> numList = getNums(numCount,numsArr);
            //普通
            String bai = numList.stream().collect(Collectors.joining());
            List<String> codeList = Code3DCreateUtils.z3Code(bai);
            BigDecimal buyMoney = BigDecimal.valueOf(1+random.nextInt(10));
            String buyDesc = "组三普通"+bai;
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
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyDesc(buyDesc);
                oneRecord.setBuyMoney(buyMoney);
                recordList.add(oneRecord);
            }
            return recordList;
        }else if(lsType==2){
            //胆拖
            int numCount = 1;
            if (numCount==0) {
                numCount = 1;
            }
            List<String> numList = getNums(numCount,numsArr);
            String dm = numList.stream().collect(Collectors.joining());

            numList.clear();
            numCount = random.nextInt(5);
            if (numCount==0) {
                numCount = 1;
            }
            while (true) {
                String num = numsArr[random.nextInt(numsArr.length)];
                if(!dm.contains(num) && !numList.contains(num)){
                    numList.add(num);
                }
                if (numList.size() >= numCount) {
                    break;
                }
            }
            String tm = numList.stream().collect(Collectors.joining());

            List<String> codeList = Code3DCreateUtils.z3DtCode(dm,tm);
            String buyDesc = "组三胆拖"+dm+","+tm;
            BigDecimal buyMoney = BigDecimal.valueOf(1+random.nextInt(10));
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
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyDesc(buyDesc);
                oneRecord.setBuyMoney(buyMoney);
                recordList.add(oneRecord);
            }
        }else if(lsType==3){
            //和值
            String[] hzArr = {"1","2","3","4","5","6","7","8","9","10","11",
                    "12","13","14","15","16","17","18","19","20","21","22","23","24","25","26"};

            int numCount = random.nextInt(8);
            if(numCount==0){
                numCount=1;
            }
            List<String> hzList = getNums(numCount,hzArr);
            List<String> codeList = Code3DCreateUtils.z3hezhi(hzList);
            String value = hzList.stream().collect(Collectors.joining(","));
            String hzname = "组三和值";
            String buyDesc = "组三和值"+value;
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setValue(value);
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(String.valueOf(lsType));
            oneRecord.setBuyDesc(buyDesc);
            oneRecord.setHzList(hzList);
            oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
            recordList.add(oneRecord);
        }
        return recordList;
    }

    public List<BuyRecord3DVO> randomZ6(LotteryMethod lm ,LotterySetting ls) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String[] numsArr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        if (lsType == 1) {
            //组三普通
            int numCount = random.nextInt(5);
            if (numCount < 3) {
                numCount = 3;
            }
            List<String> numList = getNums(numCount,numsArr);
            //普通
            String bai = numList.stream().collect(Collectors.joining());

            String buyDesc = "组六普通"+bai;
            List<String> codeList = Code3DCreateUtils.z6CodeBy(bai);
            BigDecimal buyMoney = BigDecimal.valueOf(1+random.nextInt(10));
            codeList.forEach(code -> {
                String[] arr = code.split("");
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("组六单式");
                oneRecord.setBai(arr[0]);
                oneRecord.setShi(arr[1]);
                oneRecord.setGe(arr[2]);
                oneRecord.setBuyAmount(1);
                oneRecord.setBuyCode(code);
                oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
                oneRecord.setLmId(lm.getId());
                oneRecord.setBuyDesc(buyDesc);
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(buyMoney);
                recordList.add(oneRecord);
            });

            return recordList;
        }else if(lsType==2){
            //胆拖
            int numCount = random.nextInt(3);
            if (numCount<2) {
                numCount = 2;
            }
            List<String> numList = getNums(numCount,numsArr);
            String dm = numList.stream().collect(Collectors.joining());

            numList.clear();
            numCount = random.nextInt(5);
            if (numCount==0) {
                numCount = 1;
            }
            while (true) {
                String num = numsArr[random.nextInt(numsArr.length)];
                if(!dm.contains(num) && !numList.contains(num)){
                    numList.add(num);
                }
                if (numList.size() >= numCount) {
                    break;
                }
            }
            String tm = numList.stream().collect(Collectors.joining());

            List<String> codeList = Code3DCreateUtils.z6DtCode(dm,tm);

            String buyDesc = "组六胆拖"+dm+","+tm;
            BigDecimal buyMoney = BigDecimal.valueOf(1+random.nextInt(10));
            codeList.forEach(code-> {
                String[] arr = code.split("");
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("组六单式");
                oneRecord.setBai(arr[0]);
                oneRecord.setShi(arr[1]);
                oneRecord.setGe(arr[2]);
                oneRecord.setBuyAmount(1);
                oneRecord.setBuyCode(code);
                oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
                oneRecord.setBuyDesc(buyDesc);
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(buyMoney);
                recordList.add(oneRecord);
            });
        }else if(lsType==3){
            //和值
            String[] hzArr = {"3","4","5","6","7","8","9","10","11",
                    "12","13","14","15","16","17","18","19","20","21","22","23","24"};
            int numCount = random.nextInt(8);
            if(numCount==0){
                numCount=1;
            }
            List<String> hzList = getNums(numCount,hzArr);
            List<String> codeList = Code3DCreateUtils.z3hezhi(hzList);
            String value = hzList.stream().collect(Collectors.joining(","));
            String buyDesc = "组六和值"+value;
            String hzname = "组六和值";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setHzList(hzList);
            oneRecord.setValue(value);
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(String.valueOf(lsType));
            oneRecord.setBuyDesc(buyDesc);
            oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
            recordList.add(oneRecord);
        }
        return recordList;
    }

    public List<BuyRecord3DVO> randomBx(LotteryMethod lm ,LotterySetting ls) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String[] numsArr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        if (lsType == 1) {
            //包选3
            int numCount = random.nextInt(3);
            if (numCount==0) {
                numCount=1;
            }
            List<String> numList = getNums(numCount,numsArr);
            String bai = numList.stream().collect(Collectors.joining());

            numCount = random.nextInt(3);
            if (numCount==0) {
                numCount=1;
            }
            numList.clear();
            while(true){
                String num = numsArr[random.nextInt(numsArr.length)];
                if(!numList.contains(num)){
                    numList.add(num);
                }
                if(numList.size()>=numCount){
                    break;
                }
            }
            String shi = numList.stream().collect(Collectors.joining());

            numCount = random.nextInt(3);
            if (numCount==0) {
                numCount=1;
            }
            numList.clear();
            while(true){
                String num = numsArr[random.nextInt(numsArr.length)];
                if(!bai.contains(num) && !shi.contains(num) && !numList.contains(num)){
                    numList.add(num);
                }
                if(numList.size()>=numCount){
                    break;
                }
            }
            String ge = numList.stream().collect(Collectors.joining());
            List<String> codeList = Code3DCreateUtils.createB3Code(bai,shi,ge);
            String buyDesc = "包选三"+bai+","+shi+","+ge;
            BigDecimal buyMoney = BigDecimal.valueOf(1+random.nextInt(10));
            codeList.forEach(code -> {
                String[] arr = code.split("");
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("包选三单式");
                oneRecord.setBai(arr[0]);
                oneRecord.setShi(arr[1]);
                oneRecord.setGe(arr[2]);
                oneRecord.setBuyAmount(1);
                oneRecord.setBuyCode(code);
                oneRecord.setBuyDesc(buyDesc);
                oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(buyMoney);
                recordList.add(oneRecord);
            });
            return recordList;
        }else if(lsType==2){
            //胆拖
            int numCount = random.nextInt(3);
            if (numCount==0) {
                numCount = 1;
            }
            List<String> numList = getNums(numCount,numsArr);
            String bai = numList.stream().collect(Collectors.joining());

            numList.clear();
            numCount = random.nextInt(3);
            if (numCount==0) {
                numCount = 1;
            }
            while (true) {
                String num = numsArr[random.nextInt(numsArr.length)];
                if(!bai.contains(num) && !numList.contains(num)){
                    numList.add(num);
                }
                if (numList.size() >= numCount) {
                    break;
                }
            }
            String shi = numList.stream().collect(Collectors.joining());

            numList.clear();
            numCount = random.nextInt(3);
            if (numCount==0) {
                numCount = 1;
            }
            while (true) {
                String num = numsArr[random.nextInt(numsArr.length)];
                if(!bai.contains(num) && !shi.contains(num) && !numList.contains(num)){
                    numList.add(num);
                }
                if (numList.size() >= numCount) {
                    break;
                }
            }
            String ge = numList.stream().collect(Collectors.joining());
            List<String> codeList = Code3DCreateUtils.createB6Code(bai,shi,ge);
            String buyDesc = "包选六"+bai+","+shi+","+ge;

            BigDecimal buyMoney = BigDecimal.valueOf(1+random.nextInt(10));
            codeList.forEach(code-> {
                String[] arr = code.split("");
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("包选六单式");
                oneRecord.setBai(arr[0]);
                oneRecord.setShi(arr[1]);
                oneRecord.setGe(arr[2]);
                oneRecord.setBuyAmount(1);
                oneRecord.setBuyCode(code);
                oneRecord.setBuyDesc(buyDesc);
                oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(buyMoney);
                recordList.add(oneRecord);
            });
        }
        return recordList;
    }

    public List<BuyRecord3DVO> random1D(LotteryMethod lm ,LotterySetting ls) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String[] numsArr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        if (lsType == 1) {
            //1D
            int numCount = random.nextInt(5);
            if(numCount==0){
                numCount = 1;
            }
            List<String> numList = getNums(numCount,numsArr);
            //普通
            String bai = "";
            String shi = "";
            String ge = "";
            if(numList.size()>0){
                bai = numList.stream().collect(Collectors.joining());
            }

            numCount = random.nextInt(5);
            if(numCount==0){
                numCount = 1;
            }
            numList = getNums(numCount,numsArr);
            if(numList.size()>0){
                shi = numList.stream().collect(Collectors.joining());
            }

            numCount = random.nextInt(5);
            if(numCount==0){
                numCount = 1;
            }
            numList = getNums(numCount,numsArr);
            if(numList.size()>0){
                ge = numList.stream().collect(Collectors.joining());
            }
            String buyDesc = "1D"+bai+","+shi+","+ge;

            BigDecimal buyMoney = BigDecimal.valueOf(1+random.nextInt(10));
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
                oneRecord.setLmId(lm.getId());
                oneRecord.setBuyDesc(buyDesc);
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(buyMoney);
                recordList.add(oneRecord);
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
                oneRecord.setLmId(lm.getId());
                oneRecord.setBuyDesc(buyDesc);
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(buyMoney);
                recordList.add(oneRecord);
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
                oneRecord.setLmId(lm.getId());
                oneRecord.setBuyDesc(buyDesc);
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(buyMoney);
                recordList.add(oneRecord);
            }
            return recordList;
        }else if(lsType==2){
            //猜1D
            int numCount = random.nextInt(5);
            if (numCount==0) {
                numCount=1;
            }
            List<String> numList = getNums(numCount,numsArr);
            int size = numList.size();
            String hzname = size == 1 ? "猜1D单式" : "猜1D复式";
            String value = numList.stream().collect(Collectors.joining());
            String buyDesc = "猜1D"+value;
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBuyAmount(size);
            oneRecord.setValue(value);
            oneRecord.setBuyCode(value);
            oneRecord.setBuyCodes(value);
            oneRecord.setBuyDesc(buyDesc);
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(String.valueOf(lsType));
            oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
            recordList.add(oneRecord);
            return recordList;
        }
        return recordList;
    }

    public List<BuyRecord3DVO> random2D(LotteryMethod lm ,LotterySetting ls) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String[] numsArr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        if (lsType == 1) {
            //2D
            int numCount = random.nextInt(5);
            if (numCount==0) {
                numCount=1;
            }
            List<String> numList = getNums(numCount,numsArr);
            String bai = numList.stream().collect(Collectors.joining());

            numCount = random.nextInt(5);
            if (numCount==0) {
                numCount=1;
            }
            numList = getNums(numCount,numsArr);
            String shi = numList.stream().collect(Collectors.joining());

            numCount = random.nextInt(5);
            if (numCount==0) {
                numCount=1;
            }
            numList = getNums(numCount,numsArr);
            String ge = numList.stream().collect(Collectors.joining());

            List<String> codeList = Code3DCreateUtils.ding2Code(bai,shi,ge);
            String hzname = codeList.size() == 1 ? "2D单式" : "2D复式";
            String buyDesc = "2D"+bai+","+shi+","+ge;
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBai(bai);
            oneRecord.setShi(shi);
            oneRecord.setGe(ge);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setValue(bai+","+shi+","+ge);
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(String.valueOf(lsType));
            oneRecord.setBuyDesc(buyDesc);
            oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
            recordList.add(oneRecord);
            return recordList;
        }else if(lsType==2){
            String hzname = "猜2D单式";
            //c2d
            int numCount = random.nextInt(5);
            if (numCount<2) {
                numCount = 2;
            }

            List<String> numList = getNums(numCount,numsArr);
            List<String> codeList = Code3DCreateUtils.c2dCode(numList.stream().collect(Collectors.joining()));
            String buyDesc = "猜2D"+numList.stream().collect(Collectors.joining());
            BigDecimal buyMoney = BigDecimal.valueOf(1+random.nextInt(10));
            codeList.forEach(code-> {
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName(hzname);
                oneRecord.setValue(code);
                oneRecord.setBuyAmount(1);
                oneRecord.setBuyCode(code);
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(buyMoney);
                oneRecord.setBuyDesc(buyDesc);
                recordList.add(oneRecord);
            });
        }
        return recordList;
    }

    //大小，奇偶，猜三同，拖拉机
    public List<BuyRecord3DVO> randomNoCodes(LotteryMethod lm ,LotterySetting ls) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String lmId = lm.getId();
        BuyRecord3DVO oneRecord = null;
        switch(lmId){
            case "12":
                oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("拖拉机单式");
                oneRecord.setBuyAmount(1);
                oneRecord.setValue("拖拉机");
                oneRecord.setBuyDesc("拖拉机");
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
                break;
            case "11":
                oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("猜三同单式");
                oneRecord.setBuyAmount(1);
                oneRecord.setValue("三同号");
                oneRecord.setBuyDesc("三同号");
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
                break;
            case "10":
                int num = random.nextInt(10);
                String value = "奇";
                if(num%2==0){
                    value = "偶";
                }
                String buyDesc = value;
                oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("奇偶单式");
                oneRecord.setBuyAmount(1);
                oneRecord.setValue(value);
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                oneRecord.setBuyDesc(buyDesc);
                recordList.add(oneRecord);
                break;
            case "9":
                int num1 = random.nextInt(10);
                String value1 = "大";
                if(num1<5){
                    value1 = "小";
                }
                String buyDesc1 = value1;
                oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("大小单式");
                oneRecord.setBuyAmount(1);
                oneRecord.setValue(value1);
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                oneRecord.setBuyDesc(buyDesc1);
                recordList.add(oneRecord);
                break;
        }
        return recordList;
    }

    public List<BuyRecord3DVO> randomHs(LotteryMethod lm) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        //和值
        String[] hzArr = {"0","1","2","3","4","5","6","7","8","9","10","11",
                "12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27"};
        Set<String> numList = Sets.newHashSet();
        int numCount = random.nextInt(5);
        if(numCount==0){
            numCount=1;
        }
        while(true){
            numList.add(hzArr[random.nextInt(hzArr.length)]);
            if(numList.size()>=numCount){
                break;
            }
        }

        String value = numList.stream().collect(Collectors.joining(","));
        String buyDesc = "和数"+value;
        String huizongName = numList.size()==1?"和数单式":"和数复式";
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(huizongName);
        oneRecord.setBuyAmount(numList.size());
        oneRecord.setHzList(numList.stream().collect(Collectors.toList()));
        oneRecord.setValue(value);
        oneRecord.setLmId(lm.getId());
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
        oneRecord.setBuyDesc(buyDesc);
        recordList.add(oneRecord);

        return recordList;
    }


    public List<BuyRecord3DVO> randomKd(LotteryMethod lm) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        //和值
        String[] hzArr = {"0","1","2","3","4","5","6","7","8","9"};
        Set<String> numList = Sets.newHashSet();
        int numCount = random.nextInt(5);
        if(numCount==0){
            numCount=1;
        }
        while(true){
            numList.add(hzArr[random.nextInt(hzArr.length)]);
            if(numList.size()>=numCount){
                break;
            }
        }

        String value = numList.stream().collect(Collectors.joining(","));
        String buyDesc = "跨度"+value;
        String huizongName = buyDesc;
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(huizongName);
        oneRecord.setBuyAmount(numList.size());
        oneRecord.setHzList(numList.stream().collect(Collectors.toList()));
        oneRecord.setValue(value);
        oneRecord.setLmId(lm.getId());
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
        oneRecord.setBuyDesc(buyDesc);
        recordList.add(oneRecord);

        return recordList;
    }


    public List<BuyRecord3DVO> createDummyBuyRecords(LotteryMethod lm ,LotterySetting ls){
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        String lmId = lm.getId();
        switch(lmId){
            case "1": //直选
                recordList = randomZx(lm,ls);
                break;
            case "2":
                //通选
                recordList = randomTx(lm,ls);
                break;
            case "3":
                recordList = randomZ3(lm,ls);
                break;
            case "4":
                recordList = randomZ6(lm,ls);
                break;
            case "5":
                recordList = randomHs(lm);
                break;
            case "13":
                recordList = randomKd(lm);
                break;
            case "9":
            case "10":
            case "11":
            case "12":
                recordList = randomNoCodes(lm,ls);
                break;
            case "6":
                recordList = random1D(lm,ls);
                break;
            case "7":
                recordList = random2D(lm,ls);
                break;
            case "8":
                recordList = randomBx(lm,ls);
                break;
        }
        return recordList;
    }



}
