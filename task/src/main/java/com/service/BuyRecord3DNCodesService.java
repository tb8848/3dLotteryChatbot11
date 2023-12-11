package com.service;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.*;
import com.config.RequestDataHelper;
import com.dao.DrawBuyRecordDAO;
import com.util.IpUtil;
import com.vo.BuyRecord3DVO;
import com.vo.CodesVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 组三、组六、复式N码包组下注
 *
 *
 */
@Service
public class BuyRecord3DNCodesService extends ServiceImpl<DrawBuyRecordDAO, DrawBuyRecord> {

    @Autowired
    private Snowflake snowflake;

    @Autowired
    private DrawBuyRecordDAO drawBuyRecordDAO;


    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;


    @Autowired
    private BotUserOddsService botUserOddsService;

    @Autowired
    private PlayerService playerService;




    Logger logger = LoggerFactory.getLogger(BuyRecord3DNCodesService.class);



    public Map<String,Object> fushiBaozu(Player player,LotteryMethod lotteryMethod, LotterySetting lotterySetting,
                                         List<BuyRecord3DVO> lsDataList,
                                         Draw draw,
                                         Integer codesFrom,
                                         String huizongName,
                                         HttpServletRequest request, String buyDesc, Integer lottype){

        Map<String,Object> resultMap = new HashMap<>();
        String uid = player.getId();
        String lmId = lotteryMethod.getId();

        BotUserOdds botUserOdds = botUserOddsService.getOneBy(player.getBotUserId(),lmId,lotterySetting.getId(),lottype);
        if(null == botUserOdds){
            resultMap.put("errcode", -1);
            resultMap.put("errmsg", "操作失败:赔率参数错误");
            return resultMap;
        }


        List<CodesVO> multiCodesVOList = new ArrayList<>();

        for (BuyRecord3DVO cvo : lsDataList) {
            List<String> codeList = cvo.getCodeList();
            CodesVO vo = new CodesVO();
            vo.setCodeList(codeList);
            vo.setValue(cvo.getHuizongName());
            vo.setHzname(cvo.getHuizongName());
            vo.setLotterySettingId(lotterySetting.getId());
            vo.setLotteryMethodId(lmId);
            vo.setBuyMoney(cvo.getBuyMoney());
            vo.setIsXian(0);
            multiCodesVOList.add(vo);

        }

        List<DrawBuyRecord> buyList = new ArrayList<>();
        //List<DrawBuyRecord> buyList1 = new ArrayList<>();
        List<DrawBuyRecord> lsBuyRecordList = new ArrayList<>();

        if(multiCodesVOList.size()>0) {

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

//                    if (cvo.getIsXian() == 0) {
//                        buyRecord.setBai(arr[0]);
//                        buyRecord.setShi(arr[1]);
//                        buyRecord.setGe(arr[2]);
//                    }

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


    public Map<String,Object> z3z6Baozu(Player player,LotteryMethod lotteryMethod,LotterySetting lotterySetting,
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
//                buyRecord.setBai(arr[0]);
//                buyRecord.setShi(arr[1]);
//                buyRecord.setGe(arr[2]);
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





















}
