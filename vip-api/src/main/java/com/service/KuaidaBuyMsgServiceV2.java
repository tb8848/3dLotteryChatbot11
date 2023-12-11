package com.service;

import com.GlobalConst;
import com.beans.BotUser;
import com.beans.ChatRoomMsg;
import com.beans.LotterySetting;
import com.beans.Player;
import com.google.common.collect.Maps;
import com.util.Code3DCreateUtils;
import com.util.StringUtil;
import com.vo.BuyRecord3DVO;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Lazy
public class KuaidaBuyMsgServiceV2 {

    @Autowired
    private LotterySettingService lotterySettingService;


    public Map<String,Object> kuaidaBuy(String content, BotUser botUser, Player player){
        Map<String,Object> resultMap = new HashMap<>();
        String userId = botUser.getId();
        String[] arr = content.split("各");
        String errmsg = null;
        List<BuyRecord3DVO> buyList = Lists.newArrayList();
        Map<String,Object> resMap = Maps.newHashMap();

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
                    switch (type) {
                        case "单选":
                            resMap = codeBuy(botUser, player, buyMoney, code, "1");
                            break;
                        case "直选":
                        case "直选普通":
                            resMap = zxtxBuy(botUser, player, buyMoney, code, "1");
                            break;
                        case "直选和值":
                            resMap = zxhzBuy(botUser, player, buyMoney, code, "1");
                            break;
                        case "通选":
                        case "复式":
                            resMap = fsBuy(botUser, player, buyMoney, code, "2");
                            break;
                        case "组三":
                        case "组三普通":
                            if(code.contains("拖")){
                                resMap = z3dtBuy(botUser, player, buyMoney, code, "3");
                            }else{
                                resMap = z3Buy(botUser, player, buyMoney, code, "3");
                            }
                            break;
                        case "双飞组三":
                            resMap = z3SFBuy(botUser, player, buyMoney, code, "3");
                            break;
                        case "组三和值":
                            resMap = z3hzBuy(botUser, player, buyMoney, code, "3");
                            break;
                        case "组六":
                        case "组六普通":
                            if(code.contains("拖")){
                                resMap = z6dtBuy(botUser, player, buyMoney, code, "3");
                            }else{
                                resMap = z6Buy(botUser, player, buyMoney, code, "3");
                            }
                            break;
                        case "双飞组六":
                            resMap = z6SFBuy(botUser, player, buyMoney, code, "4");
                            break;
                        case "组六和值":
                            resMap = z6hzBuy(botUser, player, buyMoney, code, "4");
                            break;
                        case "和数":
                            resMap = hsBuy(botUser, player, buyMoney, code, "5");
                            break;
                        case "跨度":
                            resMap = kdBuy(botUser, player, buyMoney, code, "13");
                            break;
                        case "独胆":
                            resMap = ddBuy(botUser, player, buyMoney, code, "14");
                            break;
                        case "1D":
                            resMap = ding1Buy(botUser, player, buyMoney, code, "6");
                            break;
                        case "猜1D":
                            resMap = c1dBuy(botUser, player, buyMoney, code, "6");
                            break;
                        case "2D":
                            resMap = ding2Buy(botUser, player, buyMoney, code, "7");
                            break;
                        case "猜2D":
                            resMap = c2dBuy(botUser, player, buyMoney, code, "7");
                            break;
                        case "包选三":
                            resMap = b3Buy(botUser, player, buyMoney, code, "8");
                            break;
                        case "包选六":
                            resMap = b6Buy(botUser, player, buyMoney, code, "8");
                            break;
                        default:
                            resultMap.put("errcode",-1);
                            resultMap.put("errmsg","号码类别错误");
                            return resultMap;
                    }
                    if(resMap.containsKey("list")){
                        buyList = (List<BuyRecord3DVO>) resMap.get("list");
                    }else{
                        errmsg = (String)resMap.get("errmsg");
                    }
                    if(StringUtil.isNotNull(errmsg)){
                        resultMap.put("errcode",-1);
                        resultMap.put("errmsg",errmsg);
                        return resultMap;
                    }

                    if(null==buyList || buyList.size()<1){
                        resultMap.put("errcode",-1);
                        resultMap.put("errmsg","号码格式错误");
                        return resultMap;
                    }
                    resultMap.put("errcode",0);
                    resultMap.put("errmsg","");
                    resultMap.put("buyList",buyList);
                    return resultMap;
                }
            }
            resultMap.put("errcode",-1);
            resultMap.put("errmsg","号码格式错误");
            return resultMap;
        }
    }


    //单选
    private Map<String,Object> codeBuy(BotUser botUser, Player player, BigDecimal buyMoney, String codeRule, String lmId) {
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] codeArr = codeRule.split(",|，");
        if(codeArr.length==0){
            resMap.put("errmsg","号码格式错误");
            return resMap;
        }
        for(String code : codeArr){
            if(code.length()!=3){
                resMap.put("errmsg","单选号码["+code+"]错误");
                return resMap;
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
        resMap.put("list",list);
        return resMap;
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
            String buyDesc = "直选:"+codeRule;
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
        String newValue  = codeRule.replaceAll("[^0-9]","");
        //删除数字之外的任何字符
        //codeRule = codeRule.replace("/[^[0-9]/g","");
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
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyMoney(buyMoney);
        oneRecord.setBuyDesc(buyDesc);
        list.add(oneRecord);
        resMap.put("list",list);
        return resMap;
    }



    public Map<String,Object> zxhzBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        codeRule = codeRule.replace("/[^[0-9,，]/g","");
        String[] hzArr = codeRule.split(",|，");
        Set<String> nums = Arrays.asList(hzArr).stream().collect(Collectors.toSet());
        for(String hz :nums){
            if(hz.contains(".")){
                resMap.put("errmsg","直选和值号码错误:"+hz);
                return resMap;
            }else{
                if(Integer.valueOf(hz)<0 || Integer.valueOf(hz)>27){
                    resMap.put("errmsg","直选和值号码不包含:"+hz);
                    return resMap;
                }
            }
        }
        List<String> hzList = nums.stream().collect(Collectors.toList());
        List<String> codeList = Code3DCreateUtils.hezhi(hzList);
        String value = hzList.stream().collect(Collectors.joining(","));
        String hzname = "直选和值";
        String buyDesc = hzname+":"+value;
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(buyDesc);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setHzList(hzList);
        oneRecord.setValue(hzList.stream().collect(Collectors.joining(",")));
        oneRecord.setLmId("1");
        oneRecord.setLsTypeId("2");
        oneRecord.setBuyMoney(buyMoney);
        oneRecord.setBuyDesc(buyDesc);
        list.add(oneRecord);
        resMap.put("list",resMap);
        return resMap;
    }


    //双飞组三
    public Map<String,Object> z3SFBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        boolean hasError = false;
        //删除数字之外的任何字符
        String newValue  = codeRule.replaceAll("[^0-9]","");
        //codeRule = codeRule.replace("/[^[0-9]/g","");
        String[] numArr = newValue.split("");
        //删除重复的数字
        Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
        if(nums.size()!=2){
            resMap.put("errmsg","只能选2个号码:"+codeRule);
            return resMap;
        }

        String bai = nums.stream().collect(Collectors.joining());
        List<String> codeList = Code3DCreateUtils.z3SFCode(bai);

        String buyDesc = "双飞组三"+":"+bai;
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
        resMap.put("list",list);
        return resMap;
    }


    //组三普通
    public Map<String,Object> z3Buy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        //删除数字之外的任何字符
        String newValue  = codeRule.replaceAll("[^0-9]","");
        //codeRule = codeRule.replace("/[^[0-9]/g","");
        boolean hasError = false;
        String[] numArr = newValue.split("");
        Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
        if(nums.size()<2){
            resMap.put("errmsg","号码格式不正确："+codeRule);
            return resMap;
        }

        String bai = nums.stream().collect(Collectors.joining());
        List<String> codeList = Code3DCreateUtils.z3Code(bai);
        String lsName = "组三"+StringUtil.changeDigitToChinese(bai.length(),"码");

        List<LotterySetting> lsList = lotterySettingService.getListBy("3");
        LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
        if(null == ls){
            return null;
        }
        String buyDesc = lsName+":"+bai;
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

    //组三胆拖
    public Map<String,Object> z3dtBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
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
        String buyDesc = "组三"+lsName+":"+dm+"拖"+tm;
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
        resMap.put("list",list);
        return resMap;
    }

    //组三和值
    public  Map<String,Object> z3hzBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split(",，");
        boolean hasError = false;
        if(numArr.length==0){
            resMap.put("errmsg","号码格式不正确");
            return resMap;
        }
        String[] validHzArr = {"1","2","3","4","5","6","7","8","9","10","11",
                "12","13","14","15","16","17","18","19","20","21","22","23","24","25","26"};
        List<String> validHzList = Arrays.asList(validHzArr);
        List<String> hzList = Arrays.asList(numArr);
        for(String hz : hzList){
            if(!validHzList.contains(hz)){
                resMap.put("errmsg","组三和值号码不包含："+hz);
                return resMap;
            }
        }

        List<String> codeList = Code3DCreateUtils.z3hezhi(hzList);
        String hzname = "组三和值";
        String value = hzList.stream().collect(Collectors.joining(","));
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setValue(value);
        oneRecord.setLmId("3");
        oneRecord.setLsTypeId("3");
        oneRecord.setHzList(hzList);
        oneRecord.setBuyMoney(buyMoney);
        oneRecord.setBuyDesc(hzname+":"+value);
        oneRecord.setCodeList(codeList);
        list.add(oneRecord);
        resMap.put("list",list);
        return resMap;
    }



    //组六普通
    public Map<String,Object> z6Buy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        //codeRule = codeRule.replace("/[^[0-9]/g","");
        String newValue  = codeRule.replaceAll("[^0-9]","");
        String[] numArr = newValue.split("");
        Set<String> nums = Arrays.asList(numArr).stream().collect(Collectors.toSet());
        if(nums.size()<4){
            resMap.put("errmsg","号码数量不能小于4");
            return resMap;
        }

        String bai = nums.stream().collect(Collectors.joining());
        List<String> codeList = Code3DCreateUtils.z6CodeBy(bai);

        String lsName = "组六"+StringUtil.changeDigitToChinese(bai.length(),"码");

        List<LotterySetting> lsList = lotterySettingService.getListBy(lmId);
        LotterySetting ls = lsList.stream().filter(item->item.getBettingRule().equals(lsName)).findFirst().orElse(null);
        if(null == ls){
            return null;
        }
        String buyDesc = lsName+":"+bai;
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

    //组六胆拖
    public Map<String,Object> z6dtBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId ){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
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
        String buyDesc = "组六"+lsName+":"+dm+"拖"+tm;
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
        resMap.put("list",list);
        return resMap;
    }

    //组六和值
    public Map<String,Object> z6hzBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split(",，");
        if(numArr.length==0){
            resMap.put("errmsg","号码格式不正确");
            return resMap;
        }
        String[] validHzArr = {"3","4","5","6","7","8","9","10","11",
                "12","13","14","15","16","17","18","19","20","21","22","23","24"};
        List<String> validHzList = Arrays.asList(validHzArr);
        List<String> hzList = Arrays.asList(numArr);
        for(String hz : hzList){
            if(!validHzList.contains(hz)){
                resMap.put("errmsg","组六和值号码不包含："+hz);
                return resMap;
            }
        }
        List<String> codeList = Code3DCreateUtils.z3hezhi(hzList);
        String hzname = "组六和值";
        String value = hzList.stream().collect(Collectors.joining(","));
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBuyAmount(codeList.size());
        oneRecord.setValue(value);
        oneRecord.setLmId(lmId);
        oneRecord.setLsTypeId("3");
        oneRecord.setHzList(hzList);
        oneRecord.setBuyMoney(buyMoney);
        oneRecord.setBuyDesc(hzname+":"+value);
        list.add(oneRecord);
        resMap.put("list",list);
        return resMap;
    }

    //双飞组三
    public Map<String,Object> z6SFBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
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

        String buyDesc = "双飞组六"+":"+bai;
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
        resMap.put("list",list);
        return resMap;
    }


    public Map<String,Object> hsBuy(BotUser botUser, Player player,BigDecimal buyMoney,String codeRule,String lmId){
        Map<String,Object> resMap = Maps.newHashMap();
        List<BuyRecord3DVO> list = Lists.newArrayList();
        String[] numArr = codeRule.split(",|，");
        if(numArr.length==0){
            resMap.put("errmsg","号码格式错误："+codeRule);
            return resMap;
        }
        List<String> validHzList = Arrays.asList("0","1","2","3","4","5","6","7","8","9","10","11",
                "12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27");
        List<String> hzList = Arrays.asList(numArr).stream().collect(Collectors.toSet()).stream().sorted().collect(Collectors.toList());
        for(String hz : hzList){
            if(!validHzList.contains(hz)){
                resMap.put("errmsg","和数号码不包含："+hz);
                return resMap;
            }
        }
        String value = hzList.stream().collect(Collectors.joining(","));
        String huizongName = "和数："+value;
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
        List<String> hzList = numsSet.stream().collect(Collectors.toList());
        for(String hz : hzList){
            if(!validHzList.contains(hz)){
                resMap.put("errmsg","跨度号码不包含："+hz);
                return resMap;
            }
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
        for(String hz : numsSet){
            if(!validHzList.contains(hz)){
                resMap.put("errmsg","独胆号码不包含："+hz);
                return resMap;
            }
        }
        String huizongName = "独胆";
        String value = numsSet.stream().collect(Collectors.joining(","));
        String buyDesc = huizongName+":"+value;
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(buyDesc);
        oneRecord.setBuyAmount(numsSet.size());
        oneRecord.setHzList(numsSet.stream().collect(Collectors.toList()));
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
            String buyDesc = "1D:" + codeRule;
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
        String buyDesc = "猜1D:"+nums;
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
            BuyRecord3DVO vo = new BuyRecord3DVO();
            String buyDesc = "2D:" + codeRule;
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
        String buyDesc = "猜2D:"+nums;
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

            if (!validCodeList.isEmpty()) {
                BuyRecord3DVO vo = new BuyRecord3DVO();
                String buyDesc = "包选三:" + codeRule;
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

            if(!validCodeList.isEmpty()){
                BuyRecord3DVO vo = new BuyRecord3DVO();
                String buyDesc = "包选六:"+codeRule;
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
                buyList.add(vo);
                resultMap.put("list",buyList);
                return resultMap;
            }

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
