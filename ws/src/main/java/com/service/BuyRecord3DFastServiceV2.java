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
public class BuyRecord3DFastServiceV2 extends ServiceImpl<DrawBuyRecordDAO, DrawBuyRecord> {

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

            List<String> codeList = cvo.getCodeList();

//            boolean isXian = false;
//            String bai = cvo.getBai();
//            String shi = cvo.getShi();
//            String ge = cvo.getGe();
//            String value = bai + "," + shi + "," + ge;
//            List<String> codeList = new ArrayList<>();
//            switch(lmId){
//                case "8":
//                    if (lotterySetting.getTypeId() == 2) {
//                        codeList = Code3DCreateUtils.b6Code(bai, shi, ge);
//                    } else {
//                        codeList = Code3DCreateUtils.b3Code(bai, shi, ge);
//                    }
//                    break;
//                case "7":
//                    if (lotterySetting.getTypeId() == 2) {
//                        //猜2D
//                        isXian = true;
//                        codeList = Code3DCreateUtils.c2dCode(cvo.getBai());
//                        value = cvo.getBai();
//                    } else {
//                        codeList = Code3DCreateUtils.ding2Code(bai, shi, ge);
//                        value = String.format("%s,%s,%s", (StringUtil.isNotNull(bai) ? bai : '-'), (StringUtil.isNotNull(shi) ? shi : '-'), (StringUtil.isNotNull(ge) ? ge : '-'));
//                    }
//                    break;
//                case "6":
//                    if (lotterySetting.getTypeId() == 2) {
//                        //猜1D
//                        isXian = true;
//                        if (bai.length() > 1) {
//                            codeList = Arrays.asList(bai.split(""));
//                        } else {
//                            codeList.add(bai);
//                        }
//                        value = bai;
//                    } else {
//                        codeList = Code3DCreateUtils.ding1Code(bai, shi, ge);
//                        value = String.format("%s,%s,%s", (StringUtil.isNotNull(bai) ? bai : '-'), (StringUtil.isNotNull(shi) ? shi : '-'), (StringUtil.isNotNull(ge) ? ge : '-'));
//                    }
//                    break;
//                default:
//                    codeList = Code3DCreateUtils.zxFushi(bai, shi, ge);
//                    break;
//            }

            String hzname = huizongName + "快速下注" + " [" + codeList.size() + "注]";

            if(codeList.size()==1){
                for (String code : codeList) {
                    CodesVO vo = new CodesVO();
                    vo.setBuyCode(code);
                    vo.setValue(code);
                    vo.setHzname(hzname);
                    vo.setLotterySettingId(lotterySetting.getId());
                    vo.setLotteryMethodId(lmId);
                    vo.setBuyMoney(cvo.getBuyMoney());
                    vo.setIsXian(0);
                    singleCodesVOList.add(vo);
                }
            }else{
                CodesVO vo = new CodesVO();
                vo.setCodeList(codeList);
                vo.setValue(hzname);
                vo.setHzname(hzname);
                vo.setLotterySettingId(lotterySetting.getId());
                vo.setLotteryMethodId(lmId);
                vo.setBuyMoney(cvo.getBuyMoney());
                vo.setIsXian(0);
                multiCodesVOList.add(vo);
            }
        }

        List<DrawBuyRecord> buyList = new ArrayList<>();
        //List<DrawBuyRecord> buyList1 = new ArrayList<>();
        List<DrawBuyRecord> lsBuyRecordList = new ArrayList<>();
        //List<VipStopBuyCodes> vipStopBuyCodesList = new ArrayList<>(); //保存已停押的号码

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

                    buyRecord.setBuyType(buyType);
                    buyRecord.setHasOneFlag(lottype);

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

                buyRecord.setHasOneFlag(lottype);
                buyRecord.setBuyType(buyType);
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
                            break;
                        case 2:
                            //if(1==lsTypeInt){
                            String huzongName = "复式";
                            resultMap = zxtx1(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,huzongName,request,buyDesc,lotteryType);
                            //}
                            break;
                        case 3:
                            if(5==typeFlag){
                                String huizongName = "组三";
                                resultMap = z3z6(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,huizongName,request,buyDesc,lotteryType);
                            }
                            break;
                        case 4:
                            if(5==typeFlag){
                                String huizongName = "组六";
                                resultMap = z3z6(player,lotteryMethod,lotterySetting,lsDataList,draw,codesFrom,huizongName,request,buyDesc,lotteryType);
                            }
                            break;
                        case 6:
                            if(1==lsTypeInt) {
                                String huizongName = "1D";
                                resultMap = zxtx1(player, lotteryMethod, lotterySetting, lsDataList, draw, codesFrom, huizongName, request,buyDesc,lotteryType);
                            }
                            break;
                        case 7:
                            if(1==lsTypeInt) {
                                String huizongName = "2D";
                                resultMap = zxtx1(player, lotteryMethod, lotterySetting, lsDataList, draw, codesFrom, huizongName, request,buyDesc,lotteryType);
                            }
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

}
