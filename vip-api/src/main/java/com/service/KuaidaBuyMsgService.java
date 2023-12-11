package com.service;

import cn.hutool.core.lang.Snowflake;
import com.GlobalConst;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.beans.*;
import com.config.RequestDataHelper;
import com.dao.ChatRoomMsgDAO;
import com.util.Code3DCreateUtils;
import com.util.StringUtil;
import com.vo.BuyRecord3DVO;
import org.assertj.core.util.Lists;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Lazy
public class KuaidaBuyMsgService {


    public Map<String,Object> kuaidaBuy(String content, BotUser botUser, Player player){
        Map<String,Object> resultMap = new HashMap<>();
        String userId = botUser.getId();
        String[] arr = content.split("各");
        List<BuyRecord3DVO> buyList = Lists.newArrayList();
        if(arr.length!=2){
            resultMap.put("errcode",-1);
            resultMap.put("errmsg","格式错误");
            return resultMap;
        }
        BigDecimal buyMoney = BigDecimal.ZERO;
        try{
            buyMoney = new BigDecimal(arr[1]);
        }catch (Exception e){
            resultMap.put("errcode",-1);
            resultMap.put("errmsg","购买金额错误");
            return resultMap;
        }
        if(arr[0].equals("拖拉机") || arr[0].equals("三同号") || arr[0].equals("猜三同")
                || arr[0].equals("大") || arr[0].equals("小") || arr[0].equals("奇") || arr[0].equals("偶")
                || arr[0].equals("大小") || arr[0].equals("奇偶")){
            switch(arr[0]){
                case "大小":
                case "大":
                case "小":
                    buyList = noCodesBuy(botUser,player,buyMoney,arr[0],"9");
                    break;
                case "奇偶":
                case "偶":
                case "奇":
                    buyList = noCodesBuy(botUser,player,buyMoney,arr[0],"10");
                    break;
                case "拖拉机":
                    buyList = noCodesBuy(botUser,player,buyMoney,arr[0],"12");
                    break;
                case "猜三同":
                case "三同号":
                    buyList = noCodesBuy(botUser,player,buyMoney,arr[0],"11");
                    break;
            }
            if(null==buyList || buyList.size()<1){
                resultMap.put("errcode",-1);
                resultMap.put("errmsg","号码错误");
                return resultMap;
            }
            resultMap.put("errcode",0);
            resultMap.put("errmsg","");
            resultMap.put("buyList",buyList);
            return resultMap;
        }else{
            for(String word : GlobalConst.keywords1){
                if(arr[0].startsWith(word)){
                    String[] typeArr = arr[0].split(word);
                    if(typeArr.length!=2){
                        resultMap.put("errcode",-1);
                        resultMap.put("errmsg","号码错误");
                        return resultMap;
                    }

                    String type = word.toUpperCase();
                    String code = typeArr[1];
                    if(!StringUtil.checkCodeFormat(code)){
                        resultMap.put("errcode",-1);
                        resultMap.put("errmsg","号码格式错误");
                        return resultMap;
                    }
                    switch(type){
                        case "单选":
                            buyList = codeBuy(botUser,player,buyMoney,code,"1");
                            break;
                        case "直选":
                        case "直选普通":
                            buyList = zxtxBuy(botUser,player,buyMoney,code,"1");
                            break;
                        case "直选和值":
                            buyList = zxhzBuy(botUser,player,buyMoney,code,"1");
                            break;
                        case "通选":
                            buyList = zxtxBuy(botUser,player,buyMoney,code,"2");
                            break;
                        case "组三普通":
                        case "组三":
                            buyList = z3Buy(botUser,player,buyMoney,code,"3");
                            break;
                        case "组三胆拖":
                            buyList = z3dtBuy(botUser,player,buyMoney,code,"3");
                            break;
                        case "组三和值":
                            buyList = z3hzBuy(botUser,player,buyMoney,code,"3");
                            break;
                        case "组六":
                        case "组六普通":
                            buyList = z6Buy(botUser,player,buyMoney,code,"4");
                            break;
                        case "组六胆拖":
                            buyList = z6dtBuy(botUser,player,buyMoney,code,"4");
                            break;
                        case "组六和值":
                            buyList = z6hzBuy(botUser,player,buyMoney,code,"4");
                            break;
                        case "和数":
                            buyList = hsBuy(botUser,player,buyMoney,code,"5");
                            break;
                        case "跨度":
                            buyList = kdBuy(botUser,player,buyMoney,code,"13");
                            break;
                        case "1d":
                            buyList = ding1Buy(botUser,player,buyMoney,code,"6");
                            break;
                        case "c1d":
                            buyList = c1dBuy(botUser,player,buyMoney,code,"6");
                            break;
                        case "2d":
                            buyList = ding2Buy(botUser,player,buyMoney,code,"7");
                            break;
                        case "c2d":
                            buyList = c2dBuy(botUser,player,buyMoney,code,"7");
                            break;
                        case "包选三":
                            buyList = b3Buy(botUser,player,buyMoney,code,"8");
                            break;
                        case "包选六":
                            buyList = b6Buy(botUser,player,buyMoney,code,"8");
                            break;
                        case "3D":
                            buyList = D3Buy(botUser,player,buyMoney,code,"1");
                            break;
                        default:
                            resultMap.put("errcode",-1);
                            resultMap.put("errmsg","格式错误");
                            return resultMap;
                    }
                    if(null==buyList){
                        resultMap.put("errcode",-1);
                        resultMap.put("errmsg","号码错误");
                        return resultMap;
                    }
                    resultMap.put("errcode",0);
                    resultMap.put("errmsg","");
                    resultMap.put("buyList",buyList);
                    return resultMap;
                }
            }
            resultMap.put("errcode",-1);
            resultMap.put("errmsg","格式错误");
            return resultMap;
        }
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





    private List<BuyRecord3DVO> codeBuy(BotUser botUser, Player player, BigDecimal buyMoney, String codeRule, String lmId) {
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] codeArr = codeRule.split(",|，");
        if(codeArr.length==0){
            return null;
        }
        for(String code : codeArr){
            if(code.length()!=3){
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

    public List<BuyRecord3DVO> zxtxBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] locArr = codeRule.split(",|，");
        if(locArr.length!=3){
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
//            toMsg.setSource(1);
//            dataDao.insert(toMsg);
//            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
//            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
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
//            toMsg.setSource(1);
//            dataDao.insert(toMsg);
//            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
//            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
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
            oneRecord.setLmId(lmId);
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
//            toMsg.setSource(1);
//            dataDao.insert(toMsg);
//            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
//            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
            return null;
        }

        List<String> codeList = Code3DCreateUtils.z3hezhi(hzList);
        String hzname = "组三和值";
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setValue(hzList.stream().collect(Collectors.joining(",")));
        oneRecord.setLmId(lmId);
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
//            toMsg.setSource(1);
//            dataDao.insert(toMsg);
//            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
//            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
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
            oneRecord.setLmId(lmId);
            oneRecord.setLsTypeId("1");
            oneRecord.setBuyMoney(buyMoney);
            list.add(oneRecord);
        }
        return list;
    }

    //组六胆拖
    public List<BuyRecord3DVO> z6dtBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId ){
        List<BuyRecord3DVO> list = Lists.newArrayList();
        boolean hasError = false;
        String[] numArr = codeRule.split(",|，");
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
//            toMsg.setSource(1);
//            dataDao.insert(toMsg);
//            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
//            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
            return null;
        }

        List<String> codeList = Code3DCreateUtils.z3DtCode(dm,tm);

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
            oneRecord.setLmId(lmId);
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
//            toMsg.setSource(1);
//            dataDao.insert(toMsg);
//            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
//            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
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
//            toMsg.setSource(1);
//            dataDao.insert(toMsg);
//            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
//            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
            return null;
        }

        String huizongName = hzList.size()==1?"和数单式":"和数复式";
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
//            toMsg.setSource(1);
//            dataDao.insert(toMsg);
//            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
//            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
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
//            toMsg.setSource(1);
//            dataDao.insert(toMsg);
//            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
//            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
            return null;
        }

        int size = bai.length();
        String hzname = size == 1 ? "猜1D单式" : "猜1D复式";
        String value = bai;
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBuyAmount(size);
        oneRecord.setValue(value);
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
//            toMsg.setSource(1);
//            dataDao.insert(toMsg);
//            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
//            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
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
//            toMsg.setSource(1);
//            dataDao.insert(toMsg);
//            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
//            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
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
//            toMsg.setSource(1);
//            dataDao.insert(toMsg);
//            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
//            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
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
//            toMsg.setSource(1);
//            dataDao.insert(toMsg);
//            rabbitTemplate.convertAndSend("exchange_lotteryTopic_3d","botChatMsg", JSON.toJSONString(toMsg));
//            wechatApiService.sendMsg(player.getWxFriendId(),botUser.getWxId(),toMsg.getMsg());
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
            oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
            oneRecord.setLmId("8");
            oneRecord.setLsTypeId("2");
            oneRecord.setBuyMoney(buyMoney);
            list.add(oneRecord);
        });
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
                oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("奇偶单式");
                oneRecord.setBuyAmount(1);
                oneRecord.setValue(codeRule);
                oneRecord.setLmId(lmId);
                oneRecord.setLsTypeId("1");
                oneRecord.setBuyMoney(buyMoney);
                recordList.add(oneRecord);
                break;
            case "9":
                oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("大小单式");
                oneRecord.setBuyAmount(1);
                oneRecord.setValue(codeRule);
                oneRecord.setLmId(lmId);
                oneRecord.setLsTypeId("1");
                oneRecord.setBuyMoney(buyMoney);
                recordList.add(oneRecord);
                break;
        }
        return recordList;
    }




}
