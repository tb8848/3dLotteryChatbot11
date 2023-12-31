package com.service;

import com.alibaba.fastjson.JSON;
import com.beans.*;
import com.google.common.collect.Maps;
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

import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 假人下注，
 * 支持独胆
 * 采用新的下注规则指令
 */
@Service
public class DummyServiceV2 {

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

    @Autowired
    private LotterySettingService lotterySettingService;


    private Logger logger = LoggerFactory.getLogger(DummyServiceV2.class);

    public void dummyBuy(BotUser botUser,List<LotteryMethod> lmList,List<LotterySetting> lsList){
//        logger.info(">>>>>>>>>>"+botUser.getLoginName()+":执行假人投注");
        Random random = new Random();
        try {
            List<Player> dummyList = playerService.dummyList(botUser.getId());
            if (dummyList.size() > 0) {
                int idx = random.nextInt(dummyList.size());

                Player player = dummyList.get(idx);

                if (player.getPoints().compareTo(BigDecimal.valueOf(1000)) < 0) {//上分
//                    logger.info(">>>>>>>>>>>>>>>>>>>>>"+String.format("%s的假人玩家:%s 上分1000....",botUser.getLoginName(),player.getNickname()));
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
//                    logger.info(">>>>>>>>>>>>>>>>>>>>>"+String.format("%s的假人玩家:%s 下分5000....",botUser.getLoginName(),player.getNickname()));
                    int points = 5000;
                    ChatRoomMsg fromMsg = chatRoomMsgService.createFromWxMsg(botUser,player,"下分5000");
                    fromMsg.setSource(0);
                    chatRoomMsgService.save(fromMsg);
                    rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d", "botChatMsg", JSON.toJSONString(fromMsg));

                    playerService.updatePoint(player.getId(), BigDecimal.valueOf(points), false);
                    BigDecimal totalPoints = player.getPoints().subtract(BigDecimal.valueOf(points));

                    String tlp = "【%s】：%s\r\n【当前盛鱼】：%s";
                    String msgtxt = "后台下分";
                    msgtxt = String.format(tlp, msgtxt, points, totalPoints.stripTrailingZeros().toPlainString());
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
//                        logger.info(">>>>>>>>>>>>>>>>>>>>>"+String.format("%s的假人玩家:%s 开始下注....",botUser.getLoginName(),player.getNickname()));
                        //已开盘
                        LotteryMethod lm = lmList.get(random.nextInt(lmList.size()));
                        List<LotterySetting> lotterySettingList = lsList.stream().filter(item -> item.getLotteryMethodId().equals(lm.getId())).collect(Collectors.toList());

                        List<BuyRecord3DVO> list = Lists.newArrayList();
                        List<BuyRecord3DVO> records = createDummyBuyRecords(lm, lotterySettingList);
                        list.addAll(records);
                        if (null!=list && list.size() > 0) {
                            String info = String.format("[%s]的假人玩家:%s 购买[%s][%s]",botUser.getLoginName(),player.getNickname(),
                                    lotteryName,lm.getBettingMethod());
//                            logger.info(">>>>>>>>>>>>>>>>>>>>>"+info);

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
                        }

                    }else{
                        String info = String.format("[%s]的假人玩家:%s 已停止购买",botUser.getLoginName(),player.getNickname());
//                        logger.info(">>>>>>>>>>"+info);
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

            List<String> clist = Code3DCreateUtils.zxFushi(bai,shi,ge);
            String buyDesc = String.format("%s百%s十%s个%s","直选",bai,shi,ge);
            BuyRecord3DVO vo = new BuyRecord3DVO();
            vo.setBuyDesc(buyDesc);
            vo.setHuizongName(buyDesc);
            vo.setBai(bai);
            vo.setShi(shi);
            vo.setGe(ge);
            vo.setLmId(lm.getId());
            vo.setLsTypeId(String.valueOf(lsType));
            vo.setValue(bai+","+shi+","+ge);
            vo.setBuyAmount(clist.size());
            vo.setCodeList(clist);
            vo.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
            recordList.add(vo);
        }else if(lsType==2){
            int numCount = random.nextInt(8);
            if(numCount==0){
                numCount=1;
            }
            List<String> hzList = getNums(numCount,hzArr);
            List<String> codeList = Code3DCreateUtils.hezhi(hzList);
            String value = hzList.stream().collect(Collectors.joining(","));
            String hzname = "直选和值";
            String buyDesc = hzname+value;
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(buyDesc);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setHzList(hzList);
            oneRecord.setValue(hzList.stream().collect(Collectors.joining(",")));
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(String.valueOf(lsType));
            oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
            oneRecord.setBuyDesc(buyDesc);
            recordList.add(oneRecord);
        }
        return recordList;
    }


    public List<BuyRecord3DVO> randomTx(LotteryMethod lm,List<LotterySetting> lsList){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        Random random = new Random();
        String[] numsArr = {"0","1","2","3","4","5","6","7","8","9"};
        int numCount = random.nextInt(numsArr.length);
        if(numCount==0){
            numCount=1;
        }
        List<String> numList = getNums(numCount,numsArr);
        //普通
        String bai = numList.stream().collect(Collectors.joining());
        String shi = bai;
        String ge = bai;
        int sum = bai.length() * shi.length() * ge.length();
        String buyDesc = "复式"+bai;

        String lsName = "复式"+StringUtil.changeDigitToChinese(bai.length(),"码");
        LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
        if(null != ls){
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(buyDesc);
            oneRecord.setBai(bai);
            oneRecord.setShi(shi);
            oneRecord.setGe(ge);
            oneRecord.setBuyAmount(sum);
            oneRecord.setValue(bai+","+shi+","+ge);
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(""+ls.getTypeId());
            oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
            oneRecord.setBuyDesc(buyDesc);
            list.add(oneRecord);
        }
        return list;
    }


    public List<BuyRecord3DVO> randomZ3Z6Dt(LotteryMethod lm ,List<LotterySetting> lsList) {
        List<BuyRecord3DVO> list = Lists.newArrayList();
        Random random = new Random();
        BigDecimal buyMoney = BigDecimal.valueOf(1+random.nextInt(10));
        String[] numsArr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        int numCount = 1;
        List<String> numList = null;
        numList = getNums(numCount,numsArr);
        String dm = numList.stream().collect(Collectors.joining());

        numList.clear();
        numCount = random.nextInt(5);
        if (numCount==0) {
            numCount = 2;
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

        String lsName1 = dm.length()+"码"+"拖"+tm.length();
        LotterySetting ls1 = lsList.stream().filter(item->item.getBettingRule().equals(lsName1)).findFirst().orElse(null);
        if(null != ls1){
            List<String> codeList1 = null;
            if("3".equals(lm.getId())){
                codeList1 = Code3DCreateUtils.z3DtCode(dm,tm);
            }else{
                codeList1 = Code3DCreateUtils.z6DtCode(dm,tm);
            }
            String buyDesc = lm.getBettingMethod()+dm+"拖"+tm;
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(buyDesc);
            oneRecord.setBai(dm);
            oneRecord.setShi(tm);
            oneRecord.setBuyAmount(codeList1.size());
            oneRecord.setValue(dm+","+tm);
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(ls1.getTypeId()+"");
            oneRecord.setBuyMoney(buyMoney);
            oneRecord.setTypeFlag(2);
            oneRecord.setCodeList(codeList1);
            oneRecord.setBuyDesc(buyDesc);
            list.add(oneRecord);
        }
        return list;
    }


    public  List<BuyRecord3DVO> randomZ3Z6Hz(LotteryMethod lm ,List<LotterySetting> lsList){
        Random random = new Random();
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        //和值
        String[] z3hzArr = {"1","2","3","4","5","6","7","8","9","10","11",
                "12","13","14","15","16","17","18","19","20","21","22","23","24","25","26"};

        String[] z6hzArr =  {"3","4","5","6","7","8","9","10","11",
                "12","13","14","15","16","17","18","19","20","21","22","23","24"};
        int numCount = random.nextInt(8);
        if(numCount==0){
            numCount=2;
        }

        List<String> codeList = null;
        List<String> hzList = null;
        if("3".equals(lm.getId())){
            hzList = getNums(numCount,z3hzArr);
            codeList =  Code3DCreateUtils.z3hezhi(hzList);
        }else{
            hzList = getNums(numCount,z6hzArr);
            codeList =  Code3DCreateUtils.z6hezhi(hzList);
        }

        String value = hzList.stream().collect(Collectors.joining(","));
        String hzname = lm.getBettingMethod()+"和值";
        String buyDesc = lm.getBettingMethod()+"和值"+value;
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setValue(value);
        oneRecord.setLmId(lm.getId());
        oneRecord.setLsTypeId("3");
        oneRecord.setBuyDesc(buyDesc);
        oneRecord.setHzList(hzList);
        oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
        oneRecord.setBuyDesc(buyDesc);
        list.add(oneRecord);
        return list;
    }


    public  List<BuyRecord3DVO> randomZ3Z6Sf(LotteryMethod lm ,List<LotterySetting> lsList){
        Random random = new Random();
        BigDecimal buyMoney = BigDecimal.valueOf(1+random.nextInt(10));
        List<BuyRecord3DVO> list = Lists.newArrayList();
        //和值
        String[] hzArr = {"0","1","2","3","4","5","6","7","8","9"};
        int numCount = 2;
        List<String> numList = getNums(numCount,hzArr);
        String bai = numList.stream().collect(Collectors.joining());
        List<String> codeList = null;
        String buyDesc = null;
        if(lm.getId().equals("3")){
            buyDesc = "双飞组三"+bai;
            codeList = Code3DCreateUtils.z3SFCode(bai);
        }else{
            buyDesc = "双飞组六"+bai;
            codeList = Code3DCreateUtils.z6SFCode(bai);
        }

        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(buyDesc);
        oneRecord.setBai(bai);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setBuyCodes(bai);
        oneRecord.setValue(bai);
        oneRecord.setLmId(lm.getId());
        oneRecord.setLsTypeId("2");
        oneRecord.setBuyMoney(buyMoney);
        oneRecord.setTypeFlag(4);
        oneRecord.setBuyDesc(buyDesc);
        oneRecord.setCodeList(codeList);
        list.add(oneRecord);
        return list;
    }

    public  List<BuyRecord3DVO> randomZ3Z6OneCode(LotteryMethod lm ,List<LotterySetting> lsList){
        Random random = new Random();
        BigDecimal buyMoney = BigDecimal.valueOf(1+random.nextInt(10));
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        //和值
        List<String> z3CodeList = null;
        if(lm.getId().equals("3")){
            z3CodeList = Code3DCreateUtils.z3Code();
        }else{
            z3CodeList = Code3DCreateUtils.z6Code();
        }
        Collections.shuffle(z3CodeList);
        List<String> numList = z3CodeList.subList(0,5); //5个号码
        String value = numList.stream().collect(Collectors.joining(","));
        String buyDesc = lm.getBettingMethod()+value;
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(buyDesc);
        oneRecord.setBuyAmount(numList.size());
        oneRecord.setValue(value);
        oneRecord.setLmId(lm.getId());
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyMoney(buyMoney);
        oneRecord.setTypeFlag(5);
        oneRecord.setCodeList(numList);
        oneRecord.setBuyDesc(buyDesc);
        list.add(oneRecord);
        return list;
    }

    public  List<BuyRecord3DVO> randomZ3Z6Fs(LotteryMethod lm ,List<LotterySetting> lsList){
        Random random = new Random();
        BigDecimal buyMoney = BigDecimal.valueOf(1+random.nextInt(10));
        List<BuyRecord3DVO> list = Lists.newArrayList();
        int numCount = random.nextInt(10);
        String[] numsArr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        if (numCount < 2) {
            numCount = 3;
        }
        List<String> numList = getNums(numCount,numsArr);
        String bai = numList.stream().collect(Collectors.joining());
        List<String> codeList = null;
        String lsName = null;
        if(lm.getId().equals("3")){
            lsName = "组三"+StringUtil.changeDigitToChinese(bai.length(),"码");
            codeList = Code3DCreateUtils.z3Code(bai);
        }else{
            lsName = "组六"+StringUtil.changeDigitToChinese(bai.length(),"码");
            codeList = Code3DCreateUtils.z6CodeBy(bai);
        }
        String lsName1 = lsName;
        LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName1)).findFirst().orElse(null);
        if(null != ls){
            String buyDesc = lm.getBettingMethod()+bai;
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(buyDesc);
            oneRecord.setBai(bai);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setBuyCodes(bai);
            oneRecord.setValue(bai);
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(ls.getTypeId()+"");
            oneRecord.setBuyMoney(buyMoney);
            oneRecord.setTypeFlag(1);
            oneRecord.setBuyDesc(buyDesc);
            oneRecord.setCodeList(codeList);
            list.add(oneRecord);
        }
        return list;
    }



    //组三
    public List<BuyRecord3DVO> randomZ3(LotteryMethod lm ,List<LotterySetting> lsList) {
        List<BuyRecord3DVO> list = Lists.newArrayList();
        Random random = new Random();
        int[] typeArr = {1,2,3,4}; //1:组三N码；2：组三胆拖；3：和值；4：双飞；5：单式
        int typeFlag = typeArr[random.nextInt(typeArr.length)];
        switch(typeFlag){
            case 1:
                return randomZ3Z6Fs(lm,lsList);
            case 2:
                return randomZ3Z6Dt(lm ,lsList);
            case 3:
                return randomZ3Z6Hz(lm ,lsList);
            case 4:
                return randomZ3Z6Sf(lm,lsList);
//            case 5:
//                return randomZ3Z6OneCode(lm,lsList);
        }
        return null;
    }


    public List<BuyRecord3DVO> randomZ6(LotteryMethod lm ,List<LotterySetting> lsList) {
        Random random = new Random();
        int[] typeArr = {1,2,3,4}; //1:组三N码；2：组三胆拖；3：和值；4：双飞；5：单式
        int typeFlag = typeArr[random.nextInt(typeArr.length)];
        switch(typeFlag){
            case 1:
                return randomZ3Z6Fs(lm,lsList);
            case 2:
                return randomZ3Z6Dt(lm ,lsList);
            case 3:
                return randomZ3Z6Hz(lm ,lsList);
            case 4:
                return randomZ3Z6Sf(lm,lsList);
//            case 5:
//                return randomZ3Z6OneCode(lm,lsList);
        }
        return null;
    }

    public List<BuyRecord3DVO> randomBx(LotteryMethod lm ,LotterySetting ls) {
        List<BuyRecord3DVO> list = Lists.newArrayList();
        Random random = new Random();
        BigDecimal buyMoney = BigDecimal.valueOf(1+random.nextInt(10));
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
            List<String> allB3Codes = Code3DCreateUtils.b3Code();
            List<String> b3CodeList = Code3DCreateUtils.b3Code(bai, shi, ge);
            List<String> validCodeList = Lists.newArrayList();
            b3CodeList.forEach(cc -> {
                if (allB3Codes.contains(cc)) {
                    validCodeList.add(cc);
                }
            });

            if (!validCodeList.isEmpty()) {
                BuyRecord3DVO vo = new BuyRecord3DVO();
                String value = String.format("百%s十%s个%s",bai,shi,ge);
                String buyDesc = "包选三"+value;
                vo.setBuyDesc(buyDesc);
                vo.setHuizongName(buyDesc);
                vo.setBai(bai);
                vo.setShi(shi);
                vo.setGe(ge);
                vo.setCodeList(validCodeList);
                vo.setBuyAmount(validCodeList.size());
                vo.setValue(value);
                vo.setLmId(lm.getId());
                vo.setLsTypeId("1");
                vo.setBuyMoney(buyMoney);
                list.add(vo);
            }
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
            List<String> allB6Codes = Code3DCreateUtils.b6Code();

            List<String> b6CodeList = Code3DCreateUtils.b6Code(bai,shi,ge);
            List<String> validCodeList = Lists.newArrayList();
            b6CodeList.forEach(cc->{
                if(allB6Codes.contains(cc)){
                    validCodeList.add(cc);
                }
            });
            if(!validCodeList.isEmpty()){
                BuyRecord3DVO vo = new BuyRecord3DVO();
                String value = String.format("百%s十%s个%s",bai,shi,ge);
                String buyDesc = "包选六"+value;
                vo.setBuyDesc(buyDesc);
                vo.setHuizongName(buyDesc);
                vo.setCodeList(validCodeList);
                vo.setBuyAmount(validCodeList.size());
                vo.setValue(value);
                vo.setBai(bai);
                vo.setShi(shi);
                vo.setGe(ge);
                vo.setBuyMoney(buyMoney);
                vo.setLmId(lm.getId());
                vo.setLsTypeId("2");
                list.add(vo);
            }
        }
        return list;
    }

    public List<BuyRecord3DVO> random1D(LotteryMethod lm ,LotterySetting ls) {
        List<BuyRecord3DVO> list = Lists.newArrayList();
        Random random = new Random();
        BigDecimal buyMoney = BigDecimal.valueOf(1+random.nextInt(10));
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
            String value = "";
            if (StringUtil.isNull(bai)){
                bai = "-";
            }else{
                value+="百"+bai;
            }
            if (StringUtil.isNull(shi)){
                shi = "-";
            }else{
                value+="十"+shi;
            }
            if (StringUtil.isNull(ge)){
                ge = "-";
            }else{
                value+="个"+ge;
            }
            String buyDesc = "1D"+value;

            List<String> codeList = Code3DCreateUtils.ding1Code(bai, shi, ge);
            BuyRecord3DVO vo = new BuyRecord3DVO();
            vo.setHuizongName(buyDesc);
            vo.setBuyDesc(buyDesc);
            vo.setHuizongName(buyDesc);
            vo.setBai(bai);
            vo.setShi(shi);
            vo.setGe(ge);
            vo.setLmId("6");
            vo.setLsTypeId("1");
            vo.setValue(value);
            vo.setBuyAmount(codeList.size());
            vo.setCodeList(codeList);
            vo.setBuyMoney(buyMoney);
            list.add(vo);
        }else if(lsType==2){
            //猜1D
            int numCount = random.nextInt(5);
            if (numCount==0) {
                numCount=1;
            }
            List<String> numList = getNums(numCount,numsArr);
            String nums = numList.stream().collect(Collectors.joining());
            BuyRecord3DVO vo = new BuyRecord3DVO();
            String buyDesc = "猜1D"+nums;
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
        }
        return list;
    }

    public List<BuyRecord3DVO>  random2D(LotteryMethod lm ,LotterySetting ls) {
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        Random random = new Random();
        BigDecimal buyMoney = BigDecimal.valueOf(1+random.nextInt(10));
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
            String value = "";
            if (StringUtil.isNull(bai)){
                bai = "-";
            }else{
                value+="百"+bai;
            }
            if (StringUtil.isNull(shi)){
                shi = "-";
            }else{
                value+="十"+shi;
            }
            if (StringUtil.isNull(ge)){
                ge = "-";
            }else{
                value+="个"+ge;
            }
            String buyDesc = "2D"+value;

            List<String> codeList = Code3DCreateUtils.ding2Code(bai, shi, ge);
            BuyRecord3DVO vo = new BuyRecord3DVO();
            vo.setHuizongName(buyDesc);
            vo.setBuyDesc(buyDesc);
            vo.setHuizongName(buyDesc);
            vo.setBai(bai);
            vo.setShi(shi);
            vo.setGe(ge);
            vo.setLmId(lm.getId());
            vo.setLsTypeId("1");
            vo.setValue(value);
            vo.setBuyAmount(codeList.size());
            vo.setCodeList(codeList);
            vo.setBuyMoney(buyMoney);
            list.add(vo);
        }else if(lsType==2){
            //c2d
            int numCount = random.nextInt(5);
            if (numCount<2) {
                numCount = 2;
            }

            List<String> numList = getNums(numCount,numsArr);
            String nums = numList.stream().collect(Collectors.joining());
            List<String> codeList = Code3DCreateUtils.c2dCode(nums);

            BuyRecord3DVO vo = new BuyRecord3DVO();
            String buyDesc = "猜2D"+nums;
            vo.setHuizongName(buyDesc);
            vo.setBuyDesc(buyDesc);
            vo.setBai(nums);
            vo.setValue(nums);
            vo.setLmId(lm.getId());
            vo.setLsTypeId("2");
            vo.setBuyCodes(nums);
            vo.setBuyAmount(codeList.size());
            vo.setCodeList(codeList);
            vo.setBuyMoney(buyMoney);
            list.add(vo);
        }
        return list;

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
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(buyDesc);
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


    public List<BuyRecord3DVO> randomDd(LotteryMethod lm) {
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
        String buyDesc = "独胆"+value;
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


    public List<BuyRecord3DVO> createDummyBuyRecords(LotteryMethod lm ,List<LotterySetting> lsList){
        List<String> excludeLs = new ArrayList<>();
        excludeLs.add("直选和值");
        excludeLs.add("组三和值");
        excludeLs.add("组六和值");
        excludeLs.add("猜1D");
        excludeLs.add("猜2D");



        LotterySetting ls = null;
        if(lsList.size()>0){
            if(lsList.size()==1){
                ls = lsList.get(0);
            }else{
                lsList = lsList.stream().filter(item->!excludeLs.contains(item.getBettingRule())).collect(Collectors.toList());
                Collections.shuffle(lsList);
                ls = lsList.get(0);
            }
        }
        if(null == ls){
            return null;
        }
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        String lmId = lm.getId();
        switch(lmId){
            case "1": //直选
                recordList = randomZx(lm,ls);
                break;
            case "2":
                //通选
                recordList = randomTx(lm,lsList);
                break;
            case "3":
                recordList = randomZ3(lm,lsList);
                break;
            case "4":
                recordList = randomZ6(lm,lsList);
                break;
            case "5":
                recordList = randomHs(lm);
                break;
            case "13":
                recordList = randomKd(lm);
                break;
            case "14":
                recordList = randomDd(lm);
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
