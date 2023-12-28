package com.service;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.*;
import com.config.RequestDataHelper;
import com.dao.DrawBuyRecordDAO;
import com.util.Code3DCreateUtils;
import com.util.IpUtil;
import com.util.StringUtil;
import com.vo.BuyRecord3DVO;
import com.vo.CodesVO;
import org.assertj.core.util.Lists;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 下注服务类
 * 下注方法加入锁机制
 */
@Service
public class BuyRecord3DServiceV2 extends ServiceImpl<DrawBuyRecordDAO, DrawBuyRecord> {

    @Autowired
    private Snowflake snowflake;

    @Autowired
    private DrawBuyRecordDAO drawBuyRecordDAO;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    @Autowired
    private BotUserOddsService botUserOddsService;

    @Autowired
    private LotterySettingService lotterySettingService;

    @Autowired
    private LotteryMethodService lotteryMethodService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private LockTemplate lockTemplate;

    @Autowired
    private BuyRecord3DNCodesService buyRecord3DNCodesService;

    //和值
    //和值
    public Map<String,Object> hz(Player player,LotteryMethod lotteryMethod,
                                 LotterySetting lotterySetting,
                                 List<BuyRecord3DVO> lsDataList,
                                 Draw draw,
                                 Integer codesFrom,
                                 String huizongName,
                                 HttpServletRequest request,String buyDesc,Integer lottype){

        Map<String,Object> resultMap = new HashMap<>();
        String uid = player.getId();
        List<DrawBuyRecord> lsBuyRecordList  = new ArrayList<>();
        String lmId = lotteryMethod.getId();
        BotUserOdds botUserOdds = botUserOddsService.getOneBy(player.getBotUserId(),lmId,lotterySetting.getId(),lottype);
        if(null == botUserOdds){
            resultMap.put("errcode", -1);
            resultMap.put("errmsg", "操作失败:赔率参数错误");
            return resultMap;
        }

        List<DrawBuyRecord> buyList = new ArrayList<>();

        for (BuyRecord3DVO cvo : lsDataList) {

            List<String> sums = cvo.getHzList();
            String sumsStr = sums.stream().collect(Collectors.joining(","));
            boolean isXian = false;
            List<String> codeList = Code3DCreateUtils.hezhi(sums);
            boolean isMulti = codeList.size()>1?true:false;
            switch(lmId){
                case "3":
                    //组三和值
                    codeList = Code3DCreateUtils.z3hezhi(sums);
                    isMulti = codeList.size()>1?true:false;
                    break;
                case "4":
                    //组六和值
                    codeList = Code3DCreateUtils.z6hezhi(sums);
                    isMulti = codeList.size()>1?true:false;
                    break;
                case "14": //独胆
                    isXian = true;
                    codeList = sums;
                    isMulti = codeList.size()>1?true:false;
                    break;
            }
            String hzname = String.format("%s %s [%s]",huizongName,sumsStr,codeList.size()+"注");
            int idx = 0;
            for(String code : codeList){

                String[] arr = code.split("");
                Integer sum = Arrays.stream(arr).mapToInt(cc->Integer.valueOf(cc)).sum();
                BigDecimal codeBuyMoney = cvo.getBuyMoney();

                BigDecimal hasBuyMoney = BigDecimal.ZERO;
                BigDecimal lhMoney = BigDecimal.ZERO;

                BigDecimal zongjianChuhuoMoney = hasBuyMoney.subtract(lhMoney);
                //获取赔率和单项上限
                BigDecimal hsValue = player.getHsvalue().multiply(BigDecimal.valueOf(0.0001));
                Integer stopBuyFlag = 0;
                String odds = lotterySetting.getPeiRate();
                if(null!=botUserOdds){
                    odds = botUserOdds.getOdds();
                }

                DrawBuyRecord buyRecord = new DrawBuyRecord();
                buyRecord.setBuyMoney(codeBuyMoney);
                buyRecord.setBuyAmount(1);
                buyRecord.setParam1(codeBuyMoney.stripTrailingZeros().toPlainString());
                buyRecord.setCreateTime(new Date());
                buyRecord.setVipId(player.getId());
                buyRecord.setDrawId(String.valueOf(draw.getDrawId())); //回填期号，用于分库分表
                buyRecord.setBuyCodes(code);
                buyRecord.setBuyCodeShortName(code);
                buyRecord.setLotterSettingId(lotterySetting.getId());
                buyRecord.setLotteryMethodId(lotteryMethod.getId());
                buyRecord.setCodesFrom(codesFrom); //购买路径：快选
                if(odds.indexOf("/")<0){
                    buyRecord.setPeiRate(new BigDecimal(odds));
                }
                //回水金额
                buyRecord.setHsValue(hsValue);
                buyRecord.setHuishui(codeBuyMoney.multiply(hsValue));
                buyRecord.setIp(IpUtil.getIpAddr(request)); //回填会员的IP
                buyRecord.setParam3(odds);
                buyRecord.setId(snowflake.nextIdStr());
                buyRecord.setHasBuyMoney(zongjianChuhuoMoney); //总监的出货金额
                buyRecord.setHuizongFlag(0);
                buyRecord.setHuizongName(hzname);
                buyRecord.setParam2(String.valueOf(sum)); //和值
                if(!isXian){
                    buyRecord.setBai(arr[0]);
                    buyRecord.setShi(arr[1]);
                    buyRecord.setGe(arr[2]);
                }
                buyRecord.setBuyType(lottype);
                buyList.add(buyRecord);
                lsBuyRecordList.add(buyRecord);
                idx++;
            }
        }
        Map dynamicPrama = new HashMap();
        dynamicPrama.put("qihao", String.valueOf(draw.getDrawId()));
        RequestDataHelper.setRequestData(dynamicPrama);

        if (buyList.size() > 0) {

            PlayerBuyRecord playerBuyRecord = new PlayerBuyRecord();
            playerBuyRecord.setPlayerId(uid);
            playerBuyRecord.setId(snowflake.nextIdStr());
            playerBuyRecord.setBotUserId(player.getBotUserId());

            //去掉汇总记录
            List<DrawBuyRecord> noHuiZongList = buyList.stream().filter(item->item.getHuizongFlag()!=1).collect(Collectors.toList());

            BigDecimal totalBuyMoney = noHuiZongList.stream().map(item -> item.getBuyMoney()).reduce(BigDecimal.ZERO, BigDecimal::add);

            buyList.stream().forEach(item -> {
                item.setBaopaiId(playerBuyRecord.getId());
            });
            int buyType = 2;
            if(player.getUserType()==0){
                buyType = 2;
            }else{
                if(player.getPretexting()==1){
                    buyType = 2;
                }else if(player.getReportNet()==1){
                    buyType = 0;
                }else if(player.getEatPrize()==1){
                    buyType = 1;
                }
            }
            playerBuyRecord.setBuyAmount(noHuiZongList.size());
            playerBuyRecord.setBuyPoints(totalBuyMoney);
            playerBuyRecord.setDrawNo(draw.getDrawId());
            playerBuyRecord.setBuyTime(new Date());
            playerBuyRecord.setBuyDesc(buyDesc);
            playerBuyRecord.setBuyType(buyType);
            playerBuyRecord.setBuyStatus(0);
            playerBuyRecord.setEarnPoints(playerBuyRecord.getDrawPoints().subtract(playerBuyRecord.getBuyPoints()));
            playerBuyRecord.setLotteryType(lottype);
            if(codesFrom==-10){
                //假人下注
                playerBuyRecord.setBuyType(2);
            }

            if (drawBuyRecordDAO.batchAddBuyCode(buyList) > 0) {//批量插入

                playerBuyRecordService.save(playerBuyRecord);
                //更新余额
                playerService.updatePoint(uid, totalBuyMoney, false);
                resultMap.put("errcode",0);
                resultMap.put("errmsg", "0");
                resultMap.put("playerBuyId", playerBuyRecord.getId());
                return resultMap;

            } else {
                resultMap.put("errcode", -1);
                resultMap.put("errmsg", "operation.error");
                return resultMap;
            }
        }

        resultMap.put("errcode",-1);
        resultMap.put("errmsg", "无效订单");
        return resultMap;

    }



    /**
     *  直选(普通)/通选 共享方法
     *  针对定位置的玩法
     *
     */

    public Map<String,Object> zxtx1(Player player,LotteryMethod lotteryMethod,LotterySetting lotterySetting,
                                    List<BuyRecord3DVO> lsDataList,
                                    Draw draw,
                                    Integer codesFrom,
                                    String huizongName,
                                    HttpServletRequest request,String buyDesc,Integer lottype){

        Map<String,Object> resultMap = new HashMap<>();
        String uid = player.getId();
        String lmId = lotteryMethod.getId();
        BotUserOdds botUserOdds = botUserOddsService.getOneBy(player.getBotUserId(),lmId,lotterySetting.getId(),lottype);
        if(null == botUserOdds){
            resultMap.put("errcode", -1);
            resultMap.put("errmsg", "操作失败:赔率参数错误");
            return resultMap;
        }
        List<CodesVO> singleCodesVOList = new ArrayList<>();

        List<CodesVO> multiCodesVOList = new ArrayList<>();

        for (BuyRecord3DVO cvo : lsDataList) {

            boolean isXian = false;
            String bai = cvo.getBai();
            String shi = cvo.getShi();
            String ge = cvo.getGe();
            String value = bai + "," + shi + "," + ge;
            List<String> codeList = new ArrayList<>();
            switch(lmId){
                case "8":
                    if (lotterySetting.getTypeId() == 2) {
                        codeList = Code3DCreateUtils.b6Code(bai, shi, ge);
                    } else {
                        codeList = Code3DCreateUtils.b3Code(bai, shi, ge);
                    }
                    break;
                case "7":
                    if (lotterySetting.getTypeId() == 2) {
                        //猜2D
                        isXian = true;
                        codeList = Code3DCreateUtils.c2dCode(cvo.getBai());
                        value = cvo.getBai();
                    } else {
                        codeList = Code3DCreateUtils.ding2Code(bai, shi, ge);
                        value = String.format("%s,%s,%s", (StringUtil.isNotNull(bai) ? bai : '-'), (StringUtil.isNotNull(shi) ? shi : '-'), (StringUtil.isNotNull(ge) ? ge : '-'));
                    }
                    break;
                case "6":
                    if (lotterySetting.getTypeId() == 2) {
                        //猜1D
                        isXian = true;
                        if (bai.length() > 1) {
                            codeList = Arrays.asList(bai.split(""));
                        } else {
                            codeList.add(bai);
                        }
                        value = bai;
                    } else {
                        codeList = Code3DCreateUtils.ding1Code(bai, shi, ge);
                        value = String.format("%s,%s,%s", (StringUtil.isNotNull(bai) ? bai : '-'), (StringUtil.isNotNull(shi) ? shi : '-'), (StringUtil.isNotNull(ge) ? ge : '-'));
                    }
                    break;
                case "2":
                    codeList = Code3DCreateUtils.zxFushi(bai, shi, ge);
                    break;
                case "1":
                    codeList = cvo.getCodeList();
                    break;
                default:
                    break;
            }
            String hzname = cvo.getHuizongName();
            codeList = cvo.getCodeList();
            if((null == codeList || codeList.size()==0) && "1".equals(lmId)){
                codeList = Code3DCreateUtils.zxFushi(bai, shi, ge);
                if (codeList.size() == 1) {
                    hzname = huizongName + "单式 ";
                } else {
                    hzname = huizongName + "复式 ";
                }
                hzname = hzname + value + " [" + codeList.size() + "注]";
                if(lmId.equals("2")){
                    hzname = cvo.getHuizongName();
                }
            }

            if(codeList.size()==1){
                for (String code : codeList) {
                    CodesVO vo = new CodesVO();
                    vo.setBuyCode(code);
                    vo.setValue(value);
                    vo.setHzname(hzname);
                    vo.setLotterySettingId(lotterySetting.getId());
                    vo.setLotteryMethodId(lmId);
                    vo.setBuyMoney(cvo.getBuyMoney());
                    vo.setIsXian(isXian?1:0);
                    singleCodesVOList.add(vo);
                }
            }else{
                CodesVO vo = new CodesVO();
                vo.setCodeList(codeList);
                vo.setValue(value);
                vo.setHzname(hzname);
                vo.setLotterySettingId(lotterySetting.getId());
                vo.setLotteryMethodId(lmId);
                vo.setBuyMoney(cvo.getBuyMoney());
                vo.setIsXian(isXian?1:0);
                multiCodesVOList.add(vo);
            }
        }

        List<DrawBuyRecord> buyList = new ArrayList<>();
        //List<DrawBuyRecord> buyList1 = new ArrayList<>();
        List<DrawBuyRecord> lsBuyRecordList = new ArrayList<>();
        //List<VipStopBuyCodes> vipStopBuyCodesList = new ArrayList<>(); //保存已停押的号码

        if(singleCodesVOList.size()>0){

//            System.out.println("==============singleCodesVOList=====");
            Map<String,List<CodesVO>> codeGroup = singleCodesVOList.stream().collect(Collectors.groupingBy(CodesVO::getBuyCode));
            List<String> codeList = codeGroup.keySet().stream().collect(Collectors.toList());

            for(String code : codeList){
                String[] arr = code.split("");
                List<CodesVO> sameCodeList = codeGroup.get(code);

                for(CodesVO cvo : sameCodeList){

                    BigDecimal codeBuyMoney = cvo.getBuyMoney();

                    BigDecimal hsValue = player.getHsvalue().multiply(BigDecimal.valueOf(0.0001));

                    String odds = lotterySetting.getPeiRate();
                    if(null!=botUserOdds){
                        odds = botUserOdds.getOdds();
                    }

                    DrawBuyRecord buyRecord = new DrawBuyRecord();
                    buyRecord.setBuyMoney(codeBuyMoney);
                    buyRecord.setBuyAmount(1);
                    buyRecord.setParam1(codeBuyMoney.stripTrailingZeros().toPlainString());
                    buyRecord.setCreateTime(new Date());
                    buyRecord.setVipId(player.getId());
                    buyRecord.setDrawId(String.valueOf(draw.getDrawId())); //回填期号，用于分库分表
                    buyRecord.setBuyCodes(code);
                    buyRecord.setBuyCodeShortName(code);
                    buyRecord.setLotterSettingId(lotterySetting.getId());
                    buyRecord.setLotteryMethodId(lotteryMethod.getId());
                    //buyRecord.setPrintFlag(0);
                    buyRecord.setCodesFrom(codesFrom); //购买路径：快选
                    if(odds.indexOf("/")<0){
                        buyRecord.setPeiRate(new BigDecimal(odds));
                    }

                    //buyRecord.setPrintId(printNo);
                    //buyRecord.setPrintCacheId(printCache.getId());
                    buyRecord.setHasOneFlag(0);
                    //buyRecord.setBuyType(1);

                    buyRecord.setParam3(odds);
                    //回水金额
                    buyRecord.setHsValue(hsValue);
                    buyRecord.setHuishui(codeBuyMoney.multiply(hsValue));
                    //buyRecord.setIp(IpUtil.getIpAddr(request)); //回填会员的IP

                    buyRecord.setId(snowflake.nextIdStr());

                    buyRecord.setParentsUserId(player.getBotUserId());

                    //buyRecord.setHasBuyMoney(zongjianChuhuoMoney); //总监的出货金额

                    buyRecord.setHuizongFlag(0);

                    buyRecord.setHuizongName(cvo.getHzname());

                    if(cvo.getIsXian()==0){
                        buyRecord.setBai(arr[0]);
                        buyRecord.setShi(arr[1]);
                        buyRecord.setGe(arr[2]);
                    }

                    buyRecord.setBuyType(lottype);

                    buyList.add(buyRecord);

                    lsBuyRecordList.add(buyRecord);

                    //hasBuyMoney = hasBuyMoney.add(codeBuyMoney);
                }

            }
        }

        if(multiCodesVOList.size()>0) {

//            System.out.println("==============multiCodesVOList=====");
            for (CodesVO cvo : multiCodesVOList) {

                List<DrawBuyRecord> buyList1 = new ArrayList<>();

                List<String> codeList = cvo.getCodeList();

                for (String code : codeList) {

                    String[] arr = code.split("");
                    BigDecimal codeBuyMoney = cvo.getBuyMoney();
                    BigDecimal hsValue = player.getHsvalue().multiply(BigDecimal.valueOf(0.0001));
                    String odds = lotterySetting.getPeiRate();
                    if(null!=botUserOdds){
                        odds = botUserOdds.getOdds();
                    }
                    DrawBuyRecord buyRecord = new DrawBuyRecord();
                    buyRecord.setBuyMoney(codeBuyMoney);
                    buyRecord.setBuyAmount(1);
                    buyRecord.setParam1(codeBuyMoney.stripTrailingZeros().toPlainString());
                    buyRecord.setCreateTime(new Date());
                    buyRecord.setVipId(player.getId());
                    buyRecord.setDrawId(String.valueOf(draw.getDrawId())); //回填期号，用于分库分表
                    buyRecord.setBuyCodes(code);
                    buyRecord.setBuyCodeShortName(code);
                    buyRecord.setLotterSettingId(lotterySetting.getId());
                    buyRecord.setLotteryMethodId(lotteryMethod.getId());
                    buyRecord.setParentsUserId(player.getBotUserId());
                    //buyRecord.setPrintFlag(0);
                    buyRecord.setCodesFrom(codesFrom); //购买路径：快选
                    if (odds.indexOf("/") < 0) {
                        buyRecord.setPeiRate(new BigDecimal(odds));
                    }

                    //buyRecord.setPrintId(printNo);
                    //buyRecord.setPrintCacheId(printCache.getId());
                    buyRecord.setHasOneFlag(0);
                    buyRecord.setBuyType(1);

                    buyRecord.setParam3(odds);
                    //回水金额
                    buyRecord.setHsValue(hsValue);
                    buyRecord.setHuishui(codeBuyMoney.multiply(hsValue));
                    buyRecord.setIp(IpUtil.getIpAddr(request)); //回填会员的IP

                    buyRecord.setId(snowflake.nextIdStr());

                    buyRecord.setParentsUserId(player.getBotUserId());
                    buyRecord.setBuyType(lottype);


                    buyRecord.setHuizongFlag(0);

                    buyRecord.setHuizongName(cvo.getHzname());

                    if (cvo.getIsXian() == 0) {
                        buyRecord.setBai(arr[0]);
                        buyRecord.setShi(arr[1]);
                        buyRecord.setGe(arr[2]);
                    }

                    buyList1.add(buyRecord);

                    lsBuyRecordList.add(buyRecord);

                }

                buyList.addAll(buyList1);

            }
        }

        Map dynamicPrama = new HashMap();
        dynamicPrama.put("qihao", String.valueOf(draw.getDrawId()));
        RequestDataHelper.setRequestData(dynamicPrama);

        if (buyList.size() > 0) {

            PlayerBuyRecord playerBuyRecord = new PlayerBuyRecord();
            playerBuyRecord.setPlayerId(uid);
            playerBuyRecord.setId(snowflake.nextIdStr());
            playerBuyRecord.setBotUserId(player.getBotUserId());

            //去掉汇总记录
            List<DrawBuyRecord> noHuiZongList = buyList.stream().filter(item->item.getHuizongFlag()!=1).collect(Collectors.toList());

            //redisUtilService.update3DCodeHasBuyMoney(draw.getDrawId(),lotterySetting.getId(), noHuiZongList, false);

            BigDecimal totalBuyMoney = noHuiZongList.stream().map(item -> item.getBuyMoney()).reduce(BigDecimal.ZERO, BigDecimal::add);

            buyList.stream().forEach(item -> {
                item.setBaopaiId(playerBuyRecord.getId());
            });
            int buyType = 2;
            if(player.getUserType()==0){
                buyType = 2;
            }else{
                if(player.getPretexting()==1){
                    buyType = 2;
                }else if(player.getReportNet()==1){
                    buyType = 0;
                }else if(player.getEatPrize()==1){
                    buyType = 1;
                }
            }
            playerBuyRecord.setBuyAmount(noHuiZongList.size());
            playerBuyRecord.setBuyPoints(totalBuyMoney);
            playerBuyRecord.setDrawNo(draw.getDrawId());
            playerBuyRecord.setBuyTime(new Date());
            playerBuyRecord.setBuyDesc(buyDesc);
            playerBuyRecord.setBuyType(buyType);
            playerBuyRecord.setBuyStatus(0);
            playerBuyRecord.setEarnPoints(playerBuyRecord.getDrawPoints().subtract(playerBuyRecord.getBuyPoints()));
            playerBuyRecord.setLotteryType(lottype);
            if(codesFrom==-10){
                //假人下注
                playerBuyRecord.setBuyType(2);
            }

            if (drawBuyRecordDAO.batchAddBuyCode(buyList) > 0) {//批量插入
                playerBuyRecordService.save(playerBuyRecord);
                //更新余额
                playerService.updatePoint(uid, totalBuyMoney, false);

                resultMap.put("errcode",0);
                resultMap.put("errmsg", "0");
                resultMap.put("playerBuyId", playerBuyRecord.getId());
                return resultMap;

            } else {
                resultMap.put("errcode", -1);
                resultMap.put("errmsg", "操作失败");
                return resultMap;
            }
        }

        resultMap.put("errcode",-1);
        resultMap.put("errmsg", "无效订单");
        return resultMap;

    }


    public Map<String,Object> z3z6(Player player,LotteryMethod lotteryMethod,LotterySetting lotterySetting,
                                   List<BuyRecord3DVO> lsDataList,
                                   Draw draw,
                                   Integer codesFrom,
                                   String huizongName,
                                   HttpServletRequest request,String buyDesc,Integer lottype){

        Map<String,Object> resultMap = new HashMap<>();
        String uid = player.getId();
        String lmId = lotteryMethod.getId();
        BotUserOdds botUserOdds = botUserOddsService.getOneBy(player.getBotUserId(),lmId,lotterySetting.getId(),lottype);
        if(null == botUserOdds){
            resultMap.put("errcode", -1);
            resultMap.put("errmsg", "操作失败:赔率参数错误");
            return resultMap;
        }
        List<DrawBuyRecord> buyList = new ArrayList<>();
        for (BuyRecord3DVO cvo : lsDataList) {
            List<String> codeList = cvo.getCodeList();
            if(null == codeList || codeList.size()<1){
                resultMap.put("errcode",-1);
                resultMap.put("errmsg", "号码错误");
                return resultMap;
            }
            for (String code : codeList) {
                String[] arr = code.split("");
                BigDecimal codeBuyMoney = cvo.getBuyMoney();
                BigDecimal hsValue = player.getHsvalue().multiply(BigDecimal.valueOf(0.0001));
                String odds = lotterySetting.getPeiRate();
                if(null!=botUserOdds){
                    odds = botUserOdds.getOdds();
                }

                DrawBuyRecord buyRecord = new DrawBuyRecord();
                buyRecord.setBuyMoney(codeBuyMoney);
                buyRecord.setBuyAmount(1);
                buyRecord.setParam1(codeBuyMoney.stripTrailingZeros().toPlainString());
                buyRecord.setCreateTime(new Date());
                buyRecord.setVipId(player.getId());
                buyRecord.setDrawId(String.valueOf(draw.getDrawId())); //回填期号，用于分库分表
                buyRecord.setBuyCodes(code);
                buyRecord.setBuyCodeShortName(code);
                buyRecord.setLotterSettingId(lotterySetting.getId());
                buyRecord.setLotteryMethodId(lotteryMethod.getId());
                buyRecord.setPrintFlag(0);
                buyRecord.setCodesFrom(codesFrom); //购买路径：快选
                if (odds.indexOf("/") < 0) {
                    buyRecord.setPeiRate(new BigDecimal(odds));
                }

                buyRecord.setHasOneFlag(0);
                buyRecord.setBuyType(1);
                buyRecord.setHuizongFlag(0);

                buyRecord.setParam3(odds);
                //回水金额
                buyRecord.setHsValue(hsValue);
                buyRecord.setHuishui(codeBuyMoney.multiply(hsValue));
                buyRecord.setIp(IpUtil.getIpAddr(request)); //回填会员的IP

                buyRecord.setId(snowflake.nextIdStr());
                buyRecord.setHuizongName(cvo.getHuizongName());
                buyRecord.setBai(arr[0]);
                buyRecord.setShi(arr[1]);
                buyRecord.setGe(arr[2]);
                buyList.add(buyRecord);
            }
        }
        Map dynamicPrama = new HashMap();
        dynamicPrama.put("qihao", String.valueOf(draw.getDrawId()));
        RequestDataHelper.setRequestData(dynamicPrama);

        if (buyList.size() > 0) {

            PlayerBuyRecord playerBuyRecord = new PlayerBuyRecord();
            playerBuyRecord.setPlayerId(uid);
            playerBuyRecord.setId(snowflake.nextIdStr());
            playerBuyRecord.setBotUserId(player.getBotUserId());

            //去掉汇总记录
            List<DrawBuyRecord> noHuiZongList = buyList.stream().filter(item->item.getHuizongFlag()!=1).collect(Collectors.toList());
            BigDecimal totalBuyMoney = noHuiZongList.stream().map(item -> item.getBuyMoney()).reduce(BigDecimal.ZERO, BigDecimal::add);

            buyList.stream().forEach(item -> {
                item.setBaopaiId(playerBuyRecord.getId());
            });
            int buyType = 2;
            if(player.getUserType()==0){
                buyType = 2;
            }else{
                if(player.getPretexting()==1){
                    buyType = 2;
                }else if(player.getReportNet()==1){
                    buyType = 0;
                }else if(player.getEatPrize()==1){
                    buyType = 1;
                }
            }
            playerBuyRecord.setBuyAmount(noHuiZongList.size());
            playerBuyRecord.setBuyPoints(totalBuyMoney);
            playerBuyRecord.setDrawNo(draw.getDrawId());
            playerBuyRecord.setBuyTime(new Date());
            playerBuyRecord.setBuyDesc(buyDesc);
            playerBuyRecord.setBuyType(buyType);
            playerBuyRecord.setBuyStatus(0);
            playerBuyRecord.setEarnPoints(playerBuyRecord.getDrawPoints().subtract(playerBuyRecord.getBuyPoints()));
            playerBuyRecord.setLotteryType(lottype);
            if(codesFrom==-10){
                //假人下注
                playerBuyRecord.setBuyType(2);
            }

            if (drawBuyRecordDAO.batchAddBuyCode(buyList) > 0) {//批量插入
                playerBuyRecordService.save(playerBuyRecord);
                //更新余额
                playerService.updatePoint(uid, totalBuyMoney, false);

                resultMap.put("errcode",0);
                resultMap.put("errmsg", "0");
                resultMap.put("playerBuyId", playerBuyRecord.getId());
                return resultMap;

            } else {
                resultMap.put("errcode", -1);
                resultMap.put("errmsg", "操作失败");
                return resultMap;
            }
        }

        resultMap.put("errcode",-1);
        resultMap.put("errmsg", "无效订单");
        return resultMap;


    }





    /**
     * 大小、奇偶、拖拉机、猜三同
     * @param player
     * @param lotteryMethod
     * @param lotterySetting
     * @param lsDataList
     * @param draw
     * @param codesFrom
     * @param request
     * @return
     */
    public Map<String,Object> dxOrJoOrC3tOrTljV2(Player player, LotteryMethod lotteryMethod, LotterySetting lotterySetting,
                                                 List<BuyRecord3DVO> lsDataList, Draw draw, Integer codesFrom,
                                                 HttpServletRequest request,String buyDesc,Integer lottype){

        Map<String,Object> resultMap = new HashMap<>();
        String uid = player.getId();
        BotUserOdds userOdds = botUserOddsService.getOneBy(player.getBotUserId(),lotteryMethod.getId(),lotterySetting.getId(),lottype);
        if(null == userOdds){
            resultMap.put("errcode", -1);
            resultMap.put("errmsg", "操作失败:赔率参数错误");
            return resultMap;
        }
        List<String> lsIdList = Lists.newArrayList();
        lsIdList.add(lotterySetting.getId());

        for (BuyRecord3DVO br : lsDataList) {
            String buyCodeName = Code3DCreateUtils.getCodeShortName(br.getValue());
            br.setBuyCodeShortName(buyCodeName);
        }

        Map<String,List<BuyRecord3DVO>> codeGroup = lsDataList.stream().collect(Collectors.groupingBy(BuyRecord3DVO::getBuyCodeShortName));
        List<String> buyCodeNameList = codeGroup.keySet().stream().collect(Collectors.toList());

        List<DrawBuyRecord> buyList = new ArrayList<>();

        List<DrawBuyRecord> lsBuyRecordList  = new ArrayList<>();

        for(String codeShortName : buyCodeNameList) {
            List<BuyRecord3DVO> dlist = codeGroup.get(codeShortName);

            for (BuyRecord3DVO cvo : dlist) {
                String huizongName = cvo.getHuizongName() + " " + cvo.getValue() + " " + "[" + cvo.getBuyAmount() + "注]";
                BigDecimal hsValue = player.getHsvalue().multiply(BigDecimal.valueOf(0.0001));

                String odds = lotterySetting.getPeiRate();
                if(null!=userOdds){
                    odds = userOdds.getOdds();
                }
                BigDecimal codeBuyMoney = cvo.getBuyMoney();

                DrawBuyRecord buyRecord = new DrawBuyRecord();
                buyRecord.setBuyMoney(codeBuyMoney);
                buyRecord.setBuyAmount(1);
                buyRecord.setParam1(codeBuyMoney.stripTrailingZeros().toPlainString());
                buyRecord.setCreateTime(new Date());
                buyRecord.setVipId(player.getId());
                buyRecord.setDrawId(String.valueOf(draw.getDrawId())); //回填期号，用于分库分表
                buyRecord.setBuyCodes(cvo.getValue());
                if (odds.indexOf("/") < 0) {
                    buyRecord.setPeiRate(new BigDecimal(odds));
                }

                buyRecord.setLotterSettingId(lotterySetting.getId());
                buyRecord.setLotteryMethodId(lotteryMethod.getId());
                //buyRecord.setPrintFlag(0);
                buyRecord.setCodesFrom(codesFrom); //购买路径：快选
                buyRecord.setParam3(odds);
                //buyRecord.setPrintId(printNo);
                //buyRecord.setPrintCacheId(printCache.getId());
                buyRecord.setHasOneFlag(0);
                buyRecord.setBuyType(lottype);
                //回水金额
                buyRecord.setHsValue(hsValue);
                buyRecord.setHuishui(codeBuyMoney.multiply(hsValue));
                buyRecord.setIp(IpUtil.getIpAddr(request)); //回填会员的IP

                buyRecord.setId(snowflake.nextIdStr());

                buyRecord.setParentsUserId(player.getBotUserId());

                //buyRecord.setHasBuyMoney(zongjianChuhuoMoney); //总监的出货金额

                buyRecord.setHuizongFlag(0);

                buyRecord.setHuizongName(huizongName);

                buyRecord.setBuyCodeShortName(codeShortName);

                buyList.add(buyRecord);

                lsBuyRecordList.add(buyRecord);

            }
        }

        Map dynamicPrama = new HashMap();
        dynamicPrama.put("qihao", String.valueOf(draw.getDrawId()));
        RequestDataHelper.setRequestData(dynamicPrama);
        if (buyList.size() > 0) {

            PlayerBuyRecord playerBuyRecord = new PlayerBuyRecord();
            playerBuyRecord.setPlayerId(uid);
            playerBuyRecord.setId(snowflake.nextIdStr());
            playerBuyRecord.setBotUserId(player.getBotUserId());

            //去掉汇总记录
            List<DrawBuyRecord> noHuiZongList = buyList.stream().filter(item->item.getHuizongFlag()!=1).collect(Collectors.toList());

            BigDecimal totalBuyMoney = noHuiZongList.stream().map(item -> item.getBuyMoney()).reduce(BigDecimal.ZERO, BigDecimal::add);

            buyList.stream().forEach(item -> {
                item.setBaopaiId(playerBuyRecord.getId());
            });
            int buyType = 2;
            if(player.getUserType()==0){
                buyType = 2;
            }else{
                if(player.getPretexting()==1){
                    buyType = 2;
                }else if(player.getReportNet()==1){
                    buyType = 0;
                }else if(player.getEatPrize()==1){
                    buyType = 1;
                }
            }
            playerBuyRecord.setBuyAmount(noHuiZongList.size());
            playerBuyRecord.setBuyPoints(totalBuyMoney);
            playerBuyRecord.setDrawNo(draw.getDrawId());
            playerBuyRecord.setBuyTime(new Date());
            playerBuyRecord.setBuyDesc(buyDesc);
            playerBuyRecord.setBuyType(buyType);
            playerBuyRecord.setBuyStatus(0);
            playerBuyRecord.setEarnPoints(playerBuyRecord.getDrawPoints().subtract(playerBuyRecord.getBuyPoints()));
            playerBuyRecord.setLotteryType(lottype);
            if(codesFrom==-10){
                //假人下注
                playerBuyRecord.setBuyType(2);
            }

            if (drawBuyRecordDAO.batchAddBuyCode(buyList) > 0) {//批量插入

                playerBuyRecordService.save(playerBuyRecord);
                //更新积分
                playerService.updatePoint(uid, totalBuyMoney, false);

                resultMap.put("errcode",0);
                resultMap.put("errmsg", "0");
                resultMap.put("playerBuyId", playerBuyRecord.getId());
                return resultMap;
            } else {
                resultMap.put("errcode", -1);
                resultMap.put("errmsg", "operation.error");
                return resultMap;
            }
        }
        resultMap.put("errcode",-1);
        resultMap.put("errmsg", "无效订单");
        return resultMap;

    }



    /**
     * 批量号码购买
     * @param player 玩家信息
     * @param codeList 号码列表(含小分类ID、购买金额)
     * @param draw 当期期号信息
     * @param codesFrom 号码来源
     * @return
     */
    public Map<String,Object> codesBatchBuy(Player player, List<BuyRecord3DVO> codeList, Draw draw, Integer codesFrom,
                                            HttpServletRequest request,String buyDesc,Integer lotteryType){
        String uid = player.getId();
        player = playerService.getById(uid);
        Map<String,Object> resultMap = new HashMap<>();

        BigDecimal totalMoney = codeList.stream().map(item->item.getBuyMoney().multiply(new BigDecimal(item.getBuyAmount()))).reduce(BigDecimal.ZERO,BigDecimal::add);
        if(player.getPoints().compareTo(totalMoney)<0){
            //发送消息：余额不足
            resultMap.put("errcode",-1);
            //resultMap.put("errmsg","error.creditNotEnough");
            resultMap.put("errmsg","面上不足");
            return resultMap;
        }

        Map<String,List<BuyRecord3DVO>> lmGroup = codeList.stream().collect(Collectors.groupingBy(BuyRecord3DVO::getLmId));

        Set<String> lmIdSet = lmGroup.keySet();


        for(String lmId:lmIdSet) {

            List<BuyRecord3DVO> lmDataList = lmGroup.get(lmId);

            Map<String, List<BuyRecord3DVO>> lsGroup = lmDataList.stream().collect(Collectors.groupingBy(BuyRecord3DVO::getLsTypeId));

            Set<String> lsTypeIdSet = lsGroup.keySet();

            for (String lsTypeId : lsTypeIdSet) {

                List<BuyRecord3DVO> lsDataList = lsGroup.get(lsTypeId);

                LotterySetting lotterySetting = lotterySettingService.getLotterySettingByTypeId(lmId,lsTypeId);
                if (null == lotterySetting) {
                    resultMap.put("errcode", -1);
                    //resultMap.put("errmsg", "paramError");
                    resultMap.put("errmsg", "类型参数错误");
                    return resultMap;
                }

                if(null!=lotterySetting.getIsShow() && lotterySetting.getIsShow()==0){
                    resultMap.put("errcode", -1);
                    //resultMap.put("errmsg", "paramError");
                    resultMap.put("errmsg", "该玩法已关闭下注");
                    return resultMap;
                }


                BotUserOdds ar = botUserOddsService.getOneBy(player.getBotUserId(), lmId,lotterySetting.getId(),lotteryType);
                if (null == ar) {
                    resultMap.put("errcode", -1);
                    resultMap.put("errmsg", "赔率参数错误");
                    return resultMap;
                }
                //判断购买金额是否小于 最小下注金额
                BuyRecord3DVO minMoney = lsDataList.stream().sorted(Comparator.comparing(BuyRecord3DVO::getBuyMoney)).findFirst().get();
                if (minMoney.getBuyMoney().compareTo(ar.getMinBuy()) == -1) {
                    resultMap.put("errcode", -1);
                    resultMap.put("errmsg", "最小限额："+ar.getMinBuy().stripTrailingZeros().toPlainString());
                    return resultMap;
                }

                //判断购买金额是否大于 单注购买上限
                if (lotterySetting.getMinBettingPrice().compareTo(new BigDecimal(lotterySetting.getMaxBettingCount())) < 0) {
                    //在定盘设置中，最小下注金额配置 小于 单注上限
                    if (minMoney.getBuyMoney().compareTo(new BigDecimal(ar.getMaxBuy())) == 1) {

                        //发送消息：格式错误
                        //购买金额大于单注上限
                        resultMap.put("errcode", -1);
                        resultMap.put("errmsg", "最大限额："+ar.getMaxBuy());
                        return resultMap;
                    }
                }
            }
        }

        for(String lmId:lmIdSet){

            LotteryMethod lotteryMethod = lotteryMethodService.getById(lmId);

            List<BuyRecord3DVO> lmDataList = lmGroup.get(lmId);

            Map<String, List<BuyRecord3DVO>> lsGroup = lmDataList.stream().collect(Collectors.groupingBy(BuyRecord3DVO::getLsTypeId));

            Set<String> lsTypeIdSet = lsGroup.keySet();

            for (String lsTypeId : lsTypeIdSet) {

                List<BuyRecord3DVO> lsDataList = lsGroup.get(lsTypeId);

                LotterySetting lotterySetting = lotterySettingService.getLotterySettingByTypeId(lmId,lsTypeId);

                String lsId = lotterySetting.getId();

                LockInfo lockInfo = null;

                try {

                    String lockKey = (lotteryType==2?"p3":"3d")+"-bot-xz-lock-"+lsId;
                    lockInfo = lockTemplate.lock(lockKey, 3600000, 3600000);

                    int lmIdInt = Integer.valueOf(lmId);
                    int lsTypeInt = Integer.valueOf(lsTypeId);
                    int typeFlag = Integer.valueOf(lotterySetting.getWaysTips());

                    switch(lmIdInt){
                        case 1:
                            if(1==lsTypeInt){
                                String huzongName = "直选";
                                resultMap = zxtx1(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,huzongName,request,buyDesc,lotteryType);
                            }
                            if(2==lsTypeInt){
                                String huizongName = "直选和值";
                                resultMap = hz(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,huizongName,request,buyDesc,lotteryType);
                            }
                            break;
                        case 2:
                            //if(1==lsTypeInt){
                            String huzongName = "复式";
                            resultMap = zxtx1(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,huzongName,request,buyDesc,lotteryType);
                            //}
                            break;
                        case 3:
                            if(1==typeFlag || 2==typeFlag || 4==typeFlag || 5==typeFlag){
                                String huizongName = "组三";
                                resultMap = z3z6(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,huizongName,request,buyDesc,lotteryType);
                            }
                            if(3==lsTypeInt){
                                String huizongName = "组三和值";
                                resultMap = hz(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,huizongName,request,buyDesc,lotteryType);
                            }
                            break;
                        case 4:
                            if(1==typeFlag || 2==typeFlag || 4==typeFlag || 5==typeFlag){
                                String huizongName = "组六";
                                resultMap = z3z6(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,huizongName,request,buyDesc,lotteryType);
                            }
                            if(3==lsTypeInt){
                                String huizongName = "组六和值";
                                resultMap = hz(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,huizongName,request,buyDesc,lotteryType);
                            }
                            break;
                        case 8: //包选
                            if(1==lsTypeInt) {
                                String huizongName = "包选三";
                                resultMap = zxtx1(player, lotteryMethod, lotterySetting, lsDataList, draw, codesFrom, huizongName, request,buyDesc,lotteryType);
                            }
                            if(2==lsTypeInt) {
                                String huizongName = "包选六";
                                resultMap = zxtx1(player, lotteryMethod, lotterySetting, lsDataList, draw, codesFrom, huizongName, request,buyDesc,lotteryType);
                            }
                            break;
                        case 6:
                            if(1==lsTypeInt) {
                                String huizongName = "1D";
                                resultMap = zxtx1(player, lotteryMethod, lotterySetting, lsDataList, draw, codesFrom, huizongName, request,buyDesc,lotteryType);
                            }
                            if(2==lsTypeInt) {
                                //猜1D
                                String huizongName = "猜1D";
                                resultMap = zxtx1(player, lotteryMethod, lotterySetting, lsDataList, draw, codesFrom, huizongName, request,buyDesc,lotteryType);
                            }
                            break;
                        case 7:
                            if(1==lsTypeInt) {
                                String huizongName = "2D";
                                resultMap = zxtx1(player, lotteryMethod, lotterySetting, lsDataList, draw, codesFrom, huizongName, request,buyDesc,lotteryType);
                            }
                            if(2==lsTypeInt) {
                                String huizongName = "猜2D";
                                resultMap = zxtx1(player, lotteryMethod, lotterySetting, lsDataList, draw, codesFrom, huizongName, request,buyDesc,lotteryType);
                            }
                            break;
                        case 12: //拖拉机
                            if(1==lsTypeInt){
                                //
                                resultMap = dxOrJoOrC3tOrTljV2(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,request,buyDesc,lotteryType);
                            }
                            break;
                        case 11: //猜三同
                            if(1==lsTypeInt){
                                //
                                resultMap = dxOrJoOrC3tOrTljV2(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,request,buyDesc,lotteryType);
                            }
                            break;
                        case 10: //奇偶
                            if(1==lsTypeInt){
                                //
                                resultMap = dxOrJoOrC3tOrTljV2(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,request,buyDesc,lotteryType);
                            }
                            break;
                        case 9: //大小
                            if(1==lsTypeInt){
                                //
                                resultMap = dxOrJoOrC3tOrTljV2(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,request,buyDesc,lotteryType);
                            }
                            break;
                        case 5:
                            resultMap = hs(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,request,buyDesc,lotteryType);
                            break;
                        case 13:
                            resultMap = hs(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,request,buyDesc,lotteryType);
                            //resultMap = hs(vipMember,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,request);
                            break;
                        case 14:
                            String huizongName = "独胆";
                            resultMap = hz(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,huizongName,request,buyDesc,lotteryType);
                            break;
                        case 100:
                        case 200:
                            String hzName = lmIdInt==200?"组六N码":"组三N码";
                            resultMap = buyRecord3DNCodesService.z3z6Baozu(player,lotteryMethod,lotterySetting,lsDataList,draw,5,hzName,request,"",1);
                            break;
                        case 300:
                            resultMap = buyRecord3DNCodesService.fushiBaozu(player,lotteryMethod,lotterySetting,lsDataList,draw,5,"复式N码",request,"",1);
                            break;


                        default:
                            resultMap.put("errcode",-1);
                            resultMap.put("errmsg","无效订单");
                            resultMap.put("stopCodeList",null);
                            return resultMap;
                    }

                    return resultMap;

                }catch (Exception e){
                    e.printStackTrace();
                    resultMap.put("errcode",-1);
                    resultMap.put("errmsg","操作失败");
                    return resultMap;
                }finally {
                    if(null!=lockInfo){
                        lockTemplate.releaseLock(lockInfo);
                    }
                }
            }
        }
        resultMap.put("errcode",-1);
        resultMap.put("errmsg","无效订单");
        resultMap.put("stopCodeList",null);
        return resultMap;
    }




    /**
     * 和数
     * @param player 会员信息
     * @param draw 当期期号信息
     * @param codesFrom 号码来源
     * @return
     */
    public Map<String,Object> hs(Player player, LotteryMethod lotteryMethod, LotterySetting lotterySetting,
                                 List<BuyRecord3DVO> lsDataList, Draw draw, Integer codesFrom,
                                 HttpServletRequest request,String buyDesc,Integer lottype){

        Map<String,Object> resultMap = new HashMap<>();
        String uid = player.getId();
        BigDecimal totalMoney = lsDataList.stream().map(item->item.getBuyMoney().multiply(new BigDecimal(item.getBuyAmount()))).reduce(BigDecimal.ZERO,BigDecimal::add);
        if(player.getPoints().compareTo(totalMoney)<0){
            resultMap.put("errcode",-1);
            resultMap.put("errmsg","error.creditNotEnough");
            return resultMap;
        }
        String lsId = lotterySetting.getId();

        try {

            BotUserOdds userOdds = botUserOddsService.getOneBy(player.getBotUserId(),lotteryMethod.getId(),lotterySetting.getId(),lottype);
            if(null == userOdds){
                resultMap.put("errcode", -1);
                resultMap.put("errmsg", "操作失败:赔率参数错误");
                return resultMap;
            }

            String lsName = lotterySetting.getBettingRule();
            List<String> hzList = Arrays.asList(lsName.split("/"));

            List<DrawBuyRecord> buyList = new ArrayList<>();
            List<DrawBuyRecord> lsBuyRecordList  = new ArrayList<>();

            for (BuyRecord3DVO cvo : lsDataList) {
                String huizongName = cvo.getHuizongName();
                BigDecimal hsValue = player.getHsvalue().multiply(BigDecimal.valueOf(0.0001));
                String odds = lotterySetting.getPeiRate();
                if(null!=userOdds){
                    odds = userOdds.getOdds();
                }

                BigDecimal codeBuyMoney = cvo.getBuyMoney();


                DrawBuyRecord buyRecord = new DrawBuyRecord();
                buyRecord.setBuyMoney(codeBuyMoney);
                buyRecord.setBuyAmount(1);
                buyRecord.setParam1(codeBuyMoney.stripTrailingZeros().toPlainString());
                buyRecord.setCreateTime(new Date());
                buyRecord.setVipId(player.getId());
                buyRecord.setDrawId(String.valueOf(draw.getDrawId())); //回填期号，用于分库分表
                buyRecord.setBuyCodes(cvo.getValue());
                buyRecord.setBuyCodeShortName(cvo.getValue());
                if (odds.indexOf("/") < 0) {
                    buyRecord.setPeiRate(new BigDecimal(odds));
                }
                buyRecord.setLotterSettingId(lsId);
                buyRecord.setLotteryMethodId(lotteryMethod.getId());
                //buyRecord.setPrintFlag(0);
                buyRecord.setCodesFrom(codesFrom); //购买路径：快选
                buyRecord.setParam3(odds);
//                    buyRecord.setPrintId(printNo);
//                    buyRecord.setPrintCacheId(printCache.getId());
                buyRecord.setHasOneFlag(0);
                //buyRecord.setBuyType(1);
                //回水金额
                buyRecord.setHsValue(hsValue);
                buyRecord.setHuishui(codeBuyMoney.multiply(hsValue));
                buyRecord.setIp(IpUtil.getIpAddr(request)); //回填会员的IP

                buyRecord.setId(snowflake.nextIdStr());

                buyRecord.setHuizongFlag(0);

                buyRecord.setParentsUserId(player.getBotUserId());

                buyRecord.setHuizongName(huizongName);

                buyRecord.setBuyType(lottype);

                buyList.add(buyRecord);

                lsBuyRecordList.add(buyRecord);
            }

            Map dynamicPrama = new HashMap();
            dynamicPrama.put("qihao", String.valueOf(draw.getDrawId()));
            RequestDataHelper.setRequestData(dynamicPrama);

            if (buyList.size() > 0) {

                PlayerBuyRecord playerBuyRecord = new PlayerBuyRecord();
                playerBuyRecord.setPlayerId(uid);
                playerBuyRecord.setId(snowflake.nextIdStr());
                playerBuyRecord.setBotUserId(player.getBotUserId());

                //去掉汇总记录
                List<DrawBuyRecord> noHuiZongList = buyList.stream().filter(item->item.getHuizongFlag()!=1).collect(Collectors.toList());

                BigDecimal totalBuyMoney = noHuiZongList.stream().map(item -> item.getBuyMoney()).reduce(BigDecimal.ZERO, BigDecimal::add);

                buyList.stream().forEach(item -> {
                    item.setBaopaiId(playerBuyRecord.getId());
                });
                int buyType = 2;
                if(player.getUserType()==0){
                    buyType = 2;
                }else{
                    if(player.getPretexting()==1){
                        buyType = 2;
                    }else if(player.getReportNet()==1){
                        buyType = 0;
                    }else if(player.getEatPrize()==1){
                        buyType = 1;
                    }
                }
                playerBuyRecord.setBuyAmount(noHuiZongList.size());
                playerBuyRecord.setBuyPoints(totalBuyMoney);
                playerBuyRecord.setDrawNo(draw.getDrawId());
                playerBuyRecord.setBuyTime(new Date());
                playerBuyRecord.setBuyDesc(buyDesc);
                playerBuyRecord.setBuyType(buyType);
                playerBuyRecord.setBuyStatus(0);
                playerBuyRecord.setEarnPoints(playerBuyRecord.getDrawPoints().subtract(playerBuyRecord.getBuyPoints()));
                playerBuyRecord.setLotteryType(lottype);
                if(codesFrom==-10){
                    //假人下注
                    playerBuyRecord.setBuyType(2);
                }

                if (drawBuyRecordDAO.batchAddBuyCode(buyList) > 0) {//批量插入

                    playerBuyRecordService.save(playerBuyRecord);
                    //更新余额
                    playerService.updatePoint(player.getId(), totalBuyMoney, false);

                    resultMap.put("errcode",0);
                    resultMap.put("errmsg", "0");
                    resultMap.put("playerBuyId", playerBuyRecord.getId());
                    return resultMap;
                } else {
                    resultMap.put("errcode", -1);
                    resultMap.put("errmsg", "operation.error");
                    return resultMap;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            resultMap.put("errcode", -1);
            resultMap.put("errmsg", "operation.error");
            return resultMap;
        }

        resultMap.put("errcode",-1);
        resultMap.put("errmsg","无效订单");
        resultMap.put("stopCodeList",null);
        return resultMap;
    }


    /**
     * 和数
     * @param player 会员信息
     * @param codeList 号码列表(含小分类ID、购买金额)
     * @param draw 当期期号信息
     * @param codesFrom 号码来源
     * @return
     */
    public Map<String,Object> buy3dHs(Player player, List<BuyRecord3DVO> codeList, Draw draw, Integer codesFrom,
                                      HttpServletRequest request,String buyDesc,Integer lotteryType){

        Map<String,Object> resultMap = new HashMap<>();
        String uid = player.getId();
        BigDecimal totalMoney = codeList.stream().map(item->item.getBuyMoney().multiply(new BigDecimal(item.getBuyAmount()))).reduce(BigDecimal.ZERO,BigDecimal::add);
        if(player.getPoints().compareTo(totalMoney)<0){
            resultMap.put("errcode",-1);
            resultMap.put("errmsg","error.creditNotEnough");
            return resultMap;
        }

        Map<String,List<BuyRecord3DVO>> lmGroup = codeList.stream().collect(Collectors.groupingBy(BuyRecord3DVO::getLmId));

        Set<String> lmIdSet = lmGroup.keySet();

        List<BuyRecord3DVO> allDataList = new ArrayList<>();

        for(String lmId:lmIdSet) {

            LotteryMethod lotteryMethod = lotteryMethodService.getById(lmId);

            List<LotterySetting> lsList = lotterySettingService.getListBy(lmId);

            List<BuyRecord3DVO> lmDataList = lmGroup.get(lmId);

            String hzName = "";
            if(lmId.equals("5")){
                hzName = "和数";
            }else if(lmId.equals("13")){
                hzName = "跨度";
            }else{
                resultMap.put("errcode", -1);
                resultMap.put("errmsg", "参数错误");
                return resultMap;
            }

            for (BuyRecord3DVO data : lmDataList) {
                List<String> hzList = data.getHzList();

                String v = hzList.stream().collect(Collectors.joining(","));
                String huizongName = hzName + v;
                if (hzList.size() == 1) {
                    String hz = hzList.get(0);
                    LotterySetting ls = lsList.stream().filter(ll -> {
                        String[] nameArr = ll.getBettingRule().split("/");
                        return Arrays.asList(nameArr).contains(hz);
                    }).findFirst().orElse(null);
                    if (null == ls) {
                        Map<String, Object> rm = new HashMap();
                        rm.put("errcode", -1);
                        rm.put("errmsg", "参数错误");
                        return rm;
                    }
                    data.setValue(hz);
                    data.setLsTypeId(ls.getTypeId() + "");
                    data.setHuizongName(huizongName);
                    data.setBuyAmount(1);
                    data.setLotterySettingId(ls.getId());
                    allDataList.add(data);
                } else {

                    for (String hz : hzList) {
                        LotterySetting ls = lsList.stream().filter(ll -> {
                            String[] nameArr = ll.getBettingRule().split("/");
                            return Arrays.asList(nameArr).contains(hz);
                        }).findFirst().orElse(null);

                        if (null == ls) {
                            Map<String, Object> rm = new HashMap();
                            rm.put("errcode", -1);
                            rm.put("errmsg", "参数错误");
                            return rm;
                        }
                        BuyRecord3DVO vo = new BuyRecord3DVO();
                        vo.setValue(hz);
                        vo.setLsTypeId(ls.getTypeId() + "");
                        vo.setHuizongName(huizongName);
                        vo.setBuyAmount(1);
                        vo.setBuyMoney(data.getBuyMoney());
                        vo.setLmId(lmId);
                        vo.setLotterySettingId(ls.getId());
                        allDataList.add(vo);
                    }
                }
            }

            List<BotUserOdds> userOddsList = botUserOddsService.getListByUserId(player.getBotUserId(),lmId,lotteryType);

            Map<String, List<BuyRecord3DVO>> lsGroup = allDataList.stream().collect(Collectors.groupingBy(BuyRecord3DVO::getLotterySettingId));

            List<String> lsIdList = lsGroup.keySet().stream().collect(Collectors.toList());

            LockInfo lockInfo = null;

            for (String lsId : lsIdList) {

                LotterySetting lotterySetting = lsList.stream().filter(ll ->ll.getId().equals(lsId)).findFirst().orElse(null);

                List<BuyRecord3DVO> lsDataList = lsGroup.get(lsId);

                if (null == lotterySetting) {
                    resultMap.put("errcode", -1);
                    resultMap.put("errmsg", "参数错误");
                    return resultMap;
                }

                BotUserOdds  ar = userOddsList.stream().filter(item->item.getLotterySettingId().equals(lotterySetting.getId())).findFirst().orElse(null);
                if (null == ar) {
                    resultMap.put("errcode", -1);
                    resultMap.put("errmsg", "参数错误");
                    return resultMap;
                }
                //判断购买金额是否小于 最小下注金额
                BuyRecord3DVO minMoney = lsDataList.stream().sorted(Comparator.comparing(BuyRecord3DVO::getBuyMoney)).findFirst().get();
                if (minMoney.getBuyMoney().compareTo(ar.getMinBuy()) == -1) {
                    resultMap.put("errcode", -1);
                    resultMap.put("errmsg", "buy.minPrice.tip");
                    resultMap.put("holderValues", new Object[]{minMoney.getValue(), minMoney.getBuyMoney().toPlainString(),
                            ar.getMinBuy().stripTrailingZeros().toPlainString()});
                    return resultMap;
                }

                //判断购买金额是否大于 单注购买上限
                if (ar.getMinBuy().compareTo(new BigDecimal(ar.getMaxBuy())) < 0) {
                    //在定盘设置中，最小下注金额配置 小于 单注上限
                    if (minMoney.getBuyMoney().compareTo(new BigDecimal(ar.getMaxBuy())) == 1) {
                        //购买金额大于单注上限
                        resultMap.put("errcode", -1);
                        resultMap.put("errmsg", "buy.maxPrice.tip");
                        resultMap.put("holderValues", new Object[]{minMoney.getValue(),
                                minMoney.getBuyMoney().toPlainString(),
                                ar.getMaxBuy()});
                        return resultMap;
                    }
                }
            }

            List<DrawBuyRecord> buyList = new ArrayList<>();
            List<DrawBuyRecord> lsBuyRecordList  = new ArrayList<>();

            for (String lsId : lsIdList) {

                LotterySetting ls = lsList.stream().filter(ll ->ll.getId().equals(lsId)).findFirst().orElse(null);

                List<BuyRecord3DVO> lsDataList = lsGroup.get(lsId);

                String lockKey = (lotteryType==2?"p3":"3d")+"-bot-xz-lock-"+lsId;
                lockInfo = lockTemplate.lock(lockKey, 3600000, 3600000);

                try {

                    BotUserOdds  ar = userOddsList.stream().filter(item->item.getLotterySettingId().equals(lsId)).findFirst().orElse(null);

                    String lsName = ls.getBettingRule();
                    List<String> hzList = Arrays.asList(lsName.split("/"));

                    for (BuyRecord3DVO cvo : lsDataList) {

                        String hs = cvo.getValue();
                        String huizongName = cvo.getHuizongName();
//                        Integer stopBuyFlag = (Integer) oddsMap.get("stopBuy");
//                        String odds = (String) oddsMap.get("odds");
//                        Long oneItemLimit = (Long) oddsMap.get("oneItemLimit");
                        BigDecimal hsValue = player.getHsvalue().multiply(BigDecimal.valueOf(0.0001));

                        Integer stopBuyFlag = 0;
                        String odds = ls.getPeiRate();
                        if(null!=ar){
                            odds = ar.getOdds();
                        }
                        BigDecimal codeBuyMoney = cvo.getBuyMoney();

                        DrawBuyRecord buyRecord = new DrawBuyRecord();
                        buyRecord.setBuyMoney(codeBuyMoney);
                        buyRecord.setBuyAmount(1);
                        buyRecord.setParam1(codeBuyMoney.stripTrailingZeros().toPlainString());
                        buyRecord.setCreateTime(new Date());
                        buyRecord.setVipId(player.getId());
                        buyRecord.setDrawId(String.valueOf(draw.getDrawId())); //回填期号，用于分库分表
                        buyRecord.setBuyCodes(cvo.getValue());
                        buyRecord.setBuyCodeShortName(cvo.getValue());
                        if (odds.indexOf("/") < 0) {
                            buyRecord.setPeiRate(new BigDecimal(odds));
                        }
                        buyRecord.setLotterSettingId(lsId);
                        buyRecord.setLotteryMethodId(lotteryMethod.getId());
                        //buyRecord.setPrintFlag(0);
                        buyRecord.setCodesFrom(codesFrom); //购买路径：快选
                        buyRecord.setParam3(odds);
//                            buyRecord.setPrintId(printNo);
//                            buyRecord.setPrintCacheId(printCache.getId());
                        buyRecord.setHasOneFlag(0);
                        buyRecord.setBuyType(lotteryType);
                        //回水金额
                        buyRecord.setHsValue(hsValue);
                        buyRecord.setHuishui(codeBuyMoney.multiply(hsValue));
                        buyRecord.setIp(IpUtil.getIpAddr(request)); //回填会员的IP

                        buyRecord.setId(snowflake.nextIdStr());

                        buyRecord.setParentsUserId(player.getBotUserId());

                        //buyRecord.setHasBuyMoney(zongjianChuhuoMoney); //总监的出货金额

                        buyRecord.setHuizongFlag(0);

                        buyRecord.setHuizongName(huizongName);

                        buyList.add(buyRecord);

                        lsBuyRecordList.add(buyRecord);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    resultMap.put("errcode", -1);
                    resultMap.put("errmsg", "operation.error");
                    return resultMap;
                }finally {
                    if(null!=lockInfo){
                        lockTemplate.releaseLock(lockInfo);
                    }
                }
            }

            Map dynamicPrama = new HashMap();
            dynamicPrama.put("qihao", String.valueOf(draw.getDrawId()));
            RequestDataHelper.setRequestData(dynamicPrama);
            if (buyList.size() > 0) {

                PlayerBuyRecord playerBuyRecord = new PlayerBuyRecord();
                playerBuyRecord.setPlayerId(uid);
                playerBuyRecord.setId(snowflake.nextIdStr());
                playerBuyRecord.setBotUserId(player.getBotUserId());
                if(codesFrom==-10){
                    //假人下注
                    playerBuyRecord.setBuyType(2);
                }

                //去掉汇总记录
                List<DrawBuyRecord> noHuiZongList = buyList.stream().filter(item->item.getHuizongFlag()!=1).collect(Collectors.toList());

                BigDecimal totalBuyMoney = noHuiZongList.stream().map(item -> item.getBuyMoney()).reduce(BigDecimal.ZERO, BigDecimal::add);

                buyList.stream().forEach(item -> {
                    item.setBaopaiId(playerBuyRecord.getId());
                });

                int buyType = 2;
                if(player.getUserType()==0){
                    buyType = 2;
                }else{
                    if(player.getPretexting()==1){
                        buyType = 2;
                    }else if(player.getReportNet()==1){
                        buyType = 0;
                    }else if(player.getEatPrize()==1){
                        buyType = 1;
                    }
                }

                playerBuyRecord.setBuyAmount(noHuiZongList.size());
                playerBuyRecord.setBuyPoints(totalBuyMoney);
                playerBuyRecord.setDrawNo(draw.getDrawId());
                playerBuyRecord.setBuyTime(new Date());
                playerBuyRecord.setBuyDesc(buyDesc);
                playerBuyRecord.setBuyType(buyType);
                playerBuyRecord.setBuyStatus(0);
                playerBuyRecord.setEarnPoints(playerBuyRecord.getDrawPoints().subtract(playerBuyRecord.getBuyPoints()));
                playerBuyRecord.setLotteryType(lotteryType);

                if (drawBuyRecordDAO.batchAddBuyCode(buyList) > 0) {//批量插入

                    playerBuyRecordService.save(playerBuyRecord);
                    playerService.updatePoint(player.getId(), totalBuyMoney, false);

                    resultMap.put("errcode",0);
                    resultMap.put("errmsg", "0");
                    resultMap.put("playerBuyId", playerBuyRecord.getId());
                    return resultMap;
                } else {
                    resultMap.put("errcode", -1);
                    resultMap.put("errmsg", "operation.error");
                    return resultMap;
                }
            }
        }

        resultMap.put("errcode",-1);
        resultMap.put("errmsg","无效订单");
        resultMap.put("stopCodeList",null);
        return resultMap;
    }




    /**
     * 退码
     * @return
     */
    public Map<String,Object> tuima(Player player, String playerBuyId,Draw draw) {
        Map<String, Object> resultMap = new HashMap<>();
        PlayerBuyRecord playerBuyRecord = playerBuyRecordService.getById(playerBuyId);
        String drawNo = String.valueOf(draw.getDrawId());
        if (null == playerBuyRecord) {
            resultMap.put("errcode", -1);
            resultMap.put("errmsg", "退码失败，没有记录");
            return resultMap;
        }
        if (playerBuyRecord.getBuyStatus() != 0) {
            resultMap.put("errcode", -1);
            resultMap.put("errmsg", "不允许退码");
            return resultMap;
        }

        List<DrawBuyRecord> getBackCodeList = getBpCodesList(drawNo, playerBuyId);
        if (getBackCodeList.size() ==0){
            resultMap.put("errcode", -1);
            resultMap.put("errmsg", "退码失败，没有记录");
            return resultMap;
        }

        getBackCodeList.forEach(cc -> {
            cc.setBackCodeFlag(1);
            cc.setBackCodeTime(new Date());
        });
        List<String> recordIdList = getBackCodeList.stream().map(item -> item.getId()).collect(Collectors.toList());
        updateBackCodeFlag(drawNo, recordIdList);

        playerBuyRecord.setBuyStatus(-1);
        playerBuyRecordService.updateById(playerBuyRecord);

        BigDecimal backTotalMoney = getBackCodeList.stream().map(item -> item.getBuyMoney()).reduce(BigDecimal.ZERO, BigDecimal::add);
        //更新余额
        playerService.updatePoint(player.getId(), backTotalMoney, true);

        resultMap.put("errcode",0);
        resultMap.put("errmsg","退码成功");
        resultMap.put("backTotalMoney",backTotalMoney);
        return resultMap;

    }

    /**
     * 获取包牌号码
     * @param drawId
     * @param baopaiId
     * @return
     */
    public List<DrawBuyRecord> getBpCodesList(String drawId, String baopaiId){
        LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper<>();
        uw.eq(DrawBuyRecord::getDrawId,drawId);
        uw.eq(DrawBuyRecord::getBaopaiId,baopaiId);
        return drawBuyRecordDAO.selectList(uw);
    }


    /**
     * 更新退码状态和时间
     * @param drawId
     * @param recordIdList 退码记录ID列表
     * @return
     */
    public int updateBackCodeFlag(String drawId,List<String> recordIdList){
        LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper<>();
        uw.eq(DrawBuyRecord::getDrawId,drawId);
        uw.in(DrawBuyRecord::getId,recordIdList);
        uw.set(DrawBuyRecord::getBackCodeFlag,1);
        uw.set(DrawBuyRecord::getBackCodeTime,new Date());
        return drawBuyRecordDAO.update(null,uw);
    }


    public void copyTable (int drawNo) {
        drawBuyRecordDAO.copyTable(String.valueOf(drawNo));
    }


}
