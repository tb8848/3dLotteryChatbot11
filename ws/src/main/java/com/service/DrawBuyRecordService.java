package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.Draw;
import com.beans.DrawBuyRecord;
import com.dao.DrawBuyRecordDAO;
import com.google.common.collect.Maps;
import com.util.Code3DCreateUtils;
import com.util.CodeUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class DrawBuyRecordService extends ServiceImpl<DrawBuyRecordDAO, DrawBuyRecord> {


    @Autowired
    private DrawBuyRecordDAO drawBuyRecordDAO;



    /**
     * 更新一重赔率中奖数据
     * @param drawBuyRecordList
     */
    private BigDecimal updateOneOddsBuyRecords(List<DrawBuyRecord> drawBuyRecordList, boolean bjFlag) {
        BigDecimal drawMoney = BigDecimal.ZERO;
        if (!drawBuyRecordList.isEmpty()) {
            for (DrawBuyRecord drawBuyRecord : drawBuyRecordList) {
                BigDecimal odds = new BigDecimal(drawBuyRecord.getParam3());
                if (bjFlag) {
                    BigDecimal dm = new BigDecimal(drawBuyRecord.getParam1()).multiply(odds);
                }else {
                    BigDecimal dm = drawBuyRecord.getBuyMoney().multiply(odds);
                }
                BigDecimal dm = drawBuyRecord.getBuyMoney().multiply(odds);
                drawMoney = drawMoney.add(dm);

                LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper();
                uw.set(DrawBuyRecord::getDrawStatus,1);
                uw.set(DrawBuyRecord::getDrawLotteryFlag,1);
                uw.set(DrawBuyRecord::getDrawMoney, dm);
                uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                uw.eq(DrawBuyRecord::getDrawId,drawBuyRecord.getDrawId());
                drawBuyRecordDAO.update(drawBuyRecord, uw);
            };
        }
        return drawMoney;
    }



    /**
     * 更新一重赔率中奖数据
     * @param drawBuyRecordList
     */
    private BigDecimal updateOneOddsBuyRecords(List<DrawBuyRecord> drawBuyRecordList) {
        BigDecimal drawMoney = BigDecimal.ZERO;
        if (!drawBuyRecordList.isEmpty()) {
            for (DrawBuyRecord drawBuyRecord : drawBuyRecordList) {
                BigDecimal odds = new BigDecimal(drawBuyRecord.getParam3());
                BigDecimal dm = drawBuyRecord.getBuyMoney().multiply(odds);
                drawMoney = drawMoney.add(dm);

                LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper();
                uw.set(DrawBuyRecord::getDrawStatus,1);
                uw.set(DrawBuyRecord::getDrawLotteryFlag,1);
                uw.set(DrawBuyRecord::getDrawMoney, dm);
                uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                uw.eq(DrawBuyRecord::getDrawId,drawBuyRecord.getDrawId());
                drawBuyRecordDAO.update(drawBuyRecord, uw);
            };
        }
        return drawMoney;
    }


    /**
     * 查询中奖号码记录
     * @param drawId
     * @return
     */
    public Map<String, Object> getDrawRecordByCodes (String drawId, Draw draw,Integer lotteryType) {
        Map<String, Object> result = Maps.newHashMap();
        int drawCount = 0;
        BigDecimal totalDrawMoney = BigDecimal.ZERO;
        // 中奖号码三数和
        Integer sum = Integer.valueOf(draw.getDrawResult2T()) + Integer.valueOf(draw.getDrawResult3T()) + Integer.valueOf(draw.getDrawResult4T());
        // 查询直选普通中奖号码
        LambdaQueryWrapper<DrawBuyRecord> query = new LambdaQueryWrapper<>();
        query.eq(DrawBuyRecord::getBackCodeFlag, 0);
        query.eq(DrawBuyRecord::getDrawId, drawId);
        query.eq(DrawBuyRecord::getLotterSettingId, "19");
        query.eq(DrawBuyRecord::getBuyCodes, draw.getDrawResult());
        query.eq(DrawBuyRecord::getBuyType, lotteryType);
        query.in(DrawBuyRecord::getHuizongFlag, Lists.newArrayList("0", "-1"));
        List<DrawBuyRecord> zxPtList = drawBuyRecordDAO.selectList(query);
        BigDecimal drawMoney = updateOneOddsBuyRecords(zxPtList, false);
        drawCount = drawCount + zxPtList.size();
        totalDrawMoney = totalDrawMoney.add(drawMoney);

        // 查询直选和值中奖号码
        LambdaQueryWrapper<DrawBuyRecord> query2 = new LambdaQueryWrapper<>();
        query2.eq(DrawBuyRecord::getBackCodeFlag, 0);
        query2.eq(DrawBuyRecord::getDrawId, drawId);
        query2.eq(DrawBuyRecord::getLotterSettingId, "20");
        query2.eq(DrawBuyRecord::getParam2, sum.toString());
        query2.eq(DrawBuyRecord::getHasOneFlag, lotteryType);
        query2.in(DrawBuyRecord::getHuizongFlag, Lists.newArrayList("0", "-1"));
        List<DrawBuyRecord> zxHzList = drawBuyRecordDAO.selectList(query2);
        BigDecimal drawMoney2 = updateOneOddsBuyRecords(zxHzList, false);
        drawCount = drawCount + zxHzList.size();
        totalDrawMoney = totalDrawMoney.add(drawMoney2);

        // 查询复式中奖号码1(号码相同，顺序相同)
        LambdaQueryWrapper<DrawBuyRecord> query3 = new LambdaQueryWrapper<>();
        query3.eq(DrawBuyRecord::getBackCodeFlag, 0);
        query3.eq(DrawBuyRecord::getDrawId, drawId);
        query3.eq(DrawBuyRecord::getLotteryMethodId, "2");
        query3.in(DrawBuyRecord::getHuizongFlag, Arrays.asList("0", "-1"));
        query3.eq(DrawBuyRecord::getBuyCodes, draw.getDrawResult());
        query3.eq(DrawBuyRecord::getHasOneFlag, lotteryType);
        List<DrawBuyRecord> txList = drawBuyRecordDAO.selectList(query3);
        BigDecimal drawMoney3 = BigDecimal.ZERO;
        if (!txList.isEmpty()) {
            drawMoney3 = updateOneOddsBuyRecords(txList, true);
        }
        drawCount = drawCount + txList.size();
        totalDrawMoney = totalDrawMoney.add(drawMoney3);

        // 获取开奖号码中是否存在重号
        int count = CodeUtils.getCountByCode(draw.getDrawResult());
        BigDecimal drawMoney5 = BigDecimal.ZERO;
        if (count == 2) { // 有重号
            List<String> codeList = new ArrayList<>();
            CodeUtils.z3Combine(draw.getDrawResult().toCharArray(), 0, draw.getDrawResult().length(), codeList);
            // 查询组3普通、胆拖、包选3号码
            LambdaQueryWrapper<DrawBuyRecord> query5 = new LambdaQueryWrapper<>();
            query5.eq(DrawBuyRecord::getBackCodeFlag, 0);
            query5.eq(DrawBuyRecord::getDrawId, drawId);
            query5.in(DrawBuyRecord::getLotterSettingId, Arrays.asList("53", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64",
                    "65", "66", "67", "68", "69", "70", "71", "17"));
            query5.eq(DrawBuyRecord::getHasOneFlag, lotteryType);
            query5.in(DrawBuyRecord::getHuizongFlag, Arrays.asList("0", "-1"));
            if (!codeList.isEmpty()) {
                query5.in(DrawBuyRecord::getBuyCodes, codeList);
            }

            //query5.ne(DrawBuyRecord::getBuyCodes, draw.getDrawResult());
            List<DrawBuyRecord> z3List = drawBuyRecordDAO.selectList(query5);
            if (!z3List.isEmpty()) {
                for (DrawBuyRecord drawBuyRecord : z3List){
                    LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper();
                    uw.set(DrawBuyRecord::getDrawStatus,1);
                    uw.set(DrawBuyRecord::getDrawLotteryFlag,1);
                    if (drawBuyRecord.getLotterSettingId().equals("17")) { // 包选3
                        if (drawBuyRecord.getParam3().contains("/")) {
                            String odds;
                            if (drawBuyRecord.getBuyCodes().equals(draw.getDrawResult())) { // 包选3全中-号码相同，位置相同
                                odds = drawBuyRecord.getParam3().split("/")[1];
                            }else { // 包选3组中-号码相同，位置不同
                                odds = drawBuyRecord.getParam3().split("/")[0];
                            }
                            uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(odds));
                            BigDecimal dm = drawBuyRecord.getBuyMoney().multiply(new BigDecimal(odds));
                            uw.set(DrawBuyRecord::getDrawMoney,dm);
                            drawMoney5 = drawMoney5.add(dm);
                            uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                            uw.eq(DrawBuyRecord::getDrawId,drawId);
                            drawBuyRecordDAO.update(drawBuyRecord, uw);
                            drawCount ++;
                        }
                    }else { // 组3
                        BigDecimal odds = new BigDecimal(drawBuyRecord.getParam3());
                        BigDecimal dm = new BigDecimal(drawBuyRecord.getParam1()).multiply(odds);
                        uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(drawBuyRecord.getParam3()));
                        uw.set(DrawBuyRecord::getDrawMoney,dm);
                        drawMoney5 = drawMoney5.add(dm);
                        uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                        uw.eq(DrawBuyRecord::getDrawId,drawId);
                        drawBuyRecordDAO.update(drawBuyRecord, uw);
                        drawCount ++;
                    }

                    // 获取相同号码个数,等于3则中奖
                    /*int countSame = CodeUtils.countSameStr(draw.getDrawResult(), drawBuyRecord.getBuyCodes());
                    if (countSame == 3) {
                        LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper();
                        uw.set(DrawBuyRecord::getDrawStatus,1);
                        uw.set(DrawBuyRecord::getDrawLotteryFlag,1);
                        if (drawBuyRecord.getLotterSettingId().equals("17")) { // 包选3
                            if (drawBuyRecord.getParam3().contains("/")) {
                                String odds;
                                if (drawBuyRecord.getBuyCodes().equals(draw.getDrawResult())) { // 包选3全中-号码相同，位置相同
                                    odds = drawBuyRecord.getParam3().split("/")[1];
                                }else { // 包选3组中-号码相同，位置不同
                                    odds = drawBuyRecord.getParam3().split("/")[0];
                                }
                                uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(odds));
                                BigDecimal dm = drawBuyRecord.getBuyMoney().multiply(new BigDecimal(odds));
                                uw.set(DrawBuyRecord::getDrawMoney,dm);
                                drawMoney5 = drawMoney5.add(dm);
                                uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                                uw.eq(DrawBuyRecord::getDrawId,drawId);
                                drawBuyRecordDAO.update(drawBuyRecord, uw);
                                drawCount ++;
                            }
                        }else { // 组3
                            BigDecimal odds = new BigDecimal(drawBuyRecord.getParam3());
                            BigDecimal dm = new BigDecimal(drawBuyRecord.getParam1()).multiply(odds);
                            uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(drawBuyRecord.getParam3()));
                            uw.set(DrawBuyRecord::getDrawMoney,dm);
                            drawMoney5 = drawMoney5.add(dm);
                            uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                            uw.eq(DrawBuyRecord::getDrawId,drawId);
                            drawBuyRecordDAO.update(drawBuyRecord, uw);
                            drawCount ++;
                        }
                    }*/
                };
            }

            // 根据开奖号码算双飞组三中奖号码
            // 开奖号码去重
            String str = CodeUtils.removeRepeatChar(draw.getDrawResult());
            if (str.length() <= 1) {
                str = str + str;
            }
            List<String> codes = Code3DCreateUtils.z3SFCode(str);
            LambdaQueryWrapper<DrawBuyRecord> querySfZ3 = new LambdaQueryWrapper<>();
            querySfZ3.eq(DrawBuyRecord::getBackCodeFlag, 0);
            querySfZ3.eq(DrawBuyRecord::getDrawId, drawId);
            querySfZ3.eq(DrawBuyRecord::getLotterSettingId, "54");
            querySfZ3.in(DrawBuyRecord::getBuyCodes, codes);
            querySfZ3.in(DrawBuyRecord::getHuizongFlag, Arrays.asList("0", "-1"));
            List<DrawBuyRecord> sfz3List = drawBuyRecordDAO.selectList(querySfZ3);
            drawMoney5 = updateOneOddsBuyRecords(sfz3List, false);
            drawCount = drawCount + sfz3List.size();
        }else if (count == 3) { // 三同
            LambdaQueryWrapper<DrawBuyRecord> query12 = new LambdaQueryWrapper<>();
            query12.eq(DrawBuyRecord::getBackCodeFlag, 0);
            query12.eq(DrawBuyRecord::getDrawId, drawId);
            query12.eq(DrawBuyRecord::getLotterSettingId, "11");
            List<DrawBuyRecord> stList = drawBuyRecordDAO.selectList(query12);
            /*if (!stList.isEmpty()) {
                for (DrawBuyRecord drawBuyRecord : stList) {
                    if (drawBuyRecord.getLotterSettingId().equals("54") && drawBuyRecord.getBuyCodes().equals(draw.getDrawResult())) {
                        LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper();
                        uw.set(DrawBuyRecord::getDrawStatus,1);
                        uw.set(DrawBuyRecord::getDrawLotteryFlag,1);
                        BigDecimal odds = new BigDecimal(drawBuyRecord.getParam3());
                        BigDecimal dm = drawBuyRecord.getBuyMoney().multiply(odds);
                        uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(drawBuyRecord.getParam3()));
                        uw.set(DrawBuyRecord::getDrawMoney,dm);
                        drawMoney5 = drawMoney5.add(dm);
                        uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                        uw.eq(DrawBuyRecord::getDrawId,drawId);
                        drawBuyRecordDAO.update(drawBuyRecord, uw);
                        drawCount ++;
                    }else {
                        LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper();
                        uw.set(DrawBuyRecord::getDrawStatus,1);
                        uw.set(DrawBuyRecord::getDrawLotteryFlag,1);
                        BigDecimal odds = new BigDecimal(drawBuyRecord.getParam3());
                        BigDecimal dm = drawBuyRecord.getBuyMoney().multiply(odds);
                        uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(drawBuyRecord.getParam3()));
                        uw.set(DrawBuyRecord::getDrawMoney,dm);
                        drawMoney5 = drawMoney5.add(dm);
                        uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                        uw.eq(DrawBuyRecord::getDrawId,drawId);
                        drawBuyRecordDAO.update(drawBuyRecord, uw);
                        drawCount ++;
                    }
                }
            }*/
            drawMoney5 = updateOneOddsBuyRecords(stList, false);
            drawCount = drawCount + stList.size();

            String str = CodeUtils.removeRepeatChar(draw.getDrawResult());
            if (str.length() <= 1) {
                str = str + str;
            }
            List<String> codes = Code3DCreateUtils.z3SFCode(str);
            LambdaQueryWrapper<DrawBuyRecord> querySfZ3 = new LambdaQueryWrapper<>();
            querySfZ3.eq(DrawBuyRecord::getBackCodeFlag, 0);
            querySfZ3.eq(DrawBuyRecord::getDrawId, drawId);
            querySfZ3.eq(DrawBuyRecord::getLotterSettingId, "54");
            querySfZ3.in(DrawBuyRecord::getBuyCodes, codes);
            querySfZ3.in(DrawBuyRecord::getHuizongFlag, Arrays.asList("0", "-1"));
            List<DrawBuyRecord> sfz3List = drawBuyRecordDAO.selectList(querySfZ3);
            drawMoney5 = updateOneOddsBuyRecords(sfz3List, false);
            drawCount = drawCount + sfz3List.size();
        } else{ // 没有重号
            // 查询组6普通、胆拖和包选6号码
            LambdaQueryWrapper<DrawBuyRecord> query5 = new LambdaQueryWrapper<>();
            query5.eq(DrawBuyRecord::getBackCodeFlag, 0);
            query5.eq(DrawBuyRecord::getDrawId, drawId);
            query5.in(DrawBuyRecord::getLotterSettingId, Arrays.asList("72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83",
                    "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "18", "105"));
            query5.in(DrawBuyRecord::getHuizongFlag, Arrays.asList("0", "-1"));
            List<DrawBuyRecord> z6List = drawBuyRecordDAO.selectList(query5);
            if (!z6List.isEmpty()) {
                for (DrawBuyRecord drawBuyRecord : z6List) {
                    // 获取相同号码个数,等于3则中奖
                    int countSame = CodeUtils.countSameStr(draw.getDrawResult(), drawBuyRecord.getBuyCodes());
                    if (countSame == 3) {
                        LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper();
                        uw.set(DrawBuyRecord::getDrawStatus,1);
                        uw.set(DrawBuyRecord::getDrawLotteryFlag,1);
                        if (drawBuyRecord.getLotterSettingId().equals("18")) { // 包选6
                            if (drawBuyRecord.getParam3().contains("/")) {
                                String odds;
                                if (drawBuyRecord.getBuyCodes().equals(draw.getDrawResult())) { // 包选6全中-号码相同，位置相同
                                    odds = drawBuyRecord.getParam3().split("/")[1];
                                } else { // 包选6组中-号码相同，位置不同
                                    odds = drawBuyRecord.getParam3().split("/")[0];
                                }
                                uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(odds));
                                BigDecimal dm = drawBuyRecord.getBuyMoney().multiply(new BigDecimal(odds));
                                uw.set(DrawBuyRecord::getDrawMoney,dm);
                                drawMoney5 = drawMoney5.add(dm);
                                uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                                uw.eq(DrawBuyRecord::getDrawId,drawId);
                                drawBuyRecordDAO.update(drawBuyRecord, uw);
                                drawCount ++;
                            }
                        } else { // 组6
                            BigDecimal odds = new BigDecimal(drawBuyRecord.getParam3());
                            BigDecimal dm = new BigDecimal(drawBuyRecord.getParam1()).multiply(odds);
                            uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(drawBuyRecord.getParam3()));
                            uw.set(DrawBuyRecord::getDrawMoney,dm);
                            drawMoney5 = drawMoney5.add(dm);
                            uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                            uw.eq(DrawBuyRecord::getDrawId,drawId);
                            drawBuyRecordDAO.update(drawBuyRecord, uw);
                            drawCount ++;
                        }
                    }
                    if (drawBuyRecord.getLotterSettingId().equals("73")) { // 双飞组六
                        //String str = drawBuyRecord.getHuizongName().substring(0, drawBuyRecord.getHuizongName().indexOf(":"));
                        //String number = drawBuyRecord.getHuizongName().substring(str.length()+1, drawBuyRecord.getHuizongName().length());
                        Pattern pattern = Pattern.compile("\\d+");
                        Matcher matcher = pattern.matcher(drawBuyRecord.getHuizongName());
                        String number = "";
                        while (matcher.find()) {
                            number = matcher.group();
                        }
                        if (getZxDraw(number, draw.getDrawResult()) == 1) {
                            LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper();
                            uw.set(DrawBuyRecord::getDrawStatus,1);
                            uw.set(DrawBuyRecord::getDrawLotteryFlag,1);
                            BigDecimal odds = new BigDecimal(drawBuyRecord.getParam3());
                            BigDecimal dm = new BigDecimal(drawBuyRecord.getParam1()).multiply(odds);
                            uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(drawBuyRecord.getParam3()));
                            uw.set(DrawBuyRecord::getDrawMoney,dm);
                            drawMoney5 = drawMoney5.add(dm);
                            uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                            uw.eq(DrawBuyRecord::getDrawId,drawId);
                            drawBuyRecordDAO.update(drawBuyRecord, uw);
                            drawCount ++;
                        }

                    }
                };
            }
        }
        totalDrawMoney = totalDrawMoney.add(drawMoney5);

        // 组3,组6和值中奖
        LambdaQueryWrapper<DrawBuyRecord> query6 = new LambdaQueryWrapper<>();
        query6.eq(DrawBuyRecord::getBackCodeFlag, 0);
        query6.eq(DrawBuyRecord::getDrawId, drawId);
        //query6.in(DrawBuyRecord::getLotterSettingId, Arrays.asList("23", "26", "5"));
        query6.in(DrawBuyRecord::getLotterSettingId, Arrays.asList("23", "26"));
        query6.eq(DrawBuyRecord::getParam2, sum.toString());
        query6.in(DrawBuyRecord::getHuizongFlag, Lists.newArrayList("0", "-1"));
        List<DrawBuyRecord> hzList = drawBuyRecordDAO.selectList(query6);
        BigDecimal drawMoney6 = updateOneOddsBuyRecords(hzList, false);
        drawCount = drawCount + hzList.size();
        totalDrawMoney = totalDrawMoney.add(drawMoney6);

        // 和数中奖
        LambdaQueryWrapper<DrawBuyRecord> queryHs = new LambdaQueryWrapper<>();
        queryHs.eq(DrawBuyRecord::getBackCodeFlag, 0);
        queryHs.eq(DrawBuyRecord::getDrawId, drawId);
        queryHs.eq(DrawBuyRecord::getLotteryMethodId, "5");
        queryHs.eq(DrawBuyRecord::getBuyCodes, sum.toString());
        queryHs.in(DrawBuyRecord::getHuizongFlag, Lists.newArrayList("0", "-1"));
        List<DrawBuyRecord> hsList = drawBuyRecordDAO.selectList(queryHs);
        BigDecimal drawMoneyHs = updateOneOddsBuyRecords(hsList, false);
        drawCount = drawCount + hsList.size();
        totalDrawMoney = totalDrawMoney.add(drawMoneyHs);

        // 1D-1D
        List<String> codes1DList = CodeUtils.get1DCodes(draw.getDrawResult());
        LambdaQueryWrapper<DrawBuyRecord> query8 = new LambdaQueryWrapper<>();
        query8.eq(DrawBuyRecord::getBackCodeFlag, 0);
        query8.eq(DrawBuyRecord::getDrawId, drawId);
        query8.eq(DrawBuyRecord::getLotterSettingId, "13");
        query8.in(DrawBuyRecord::getHuizongFlag, Lists.newArrayList("0", "-1"));
        /*query8.and(tmp -> tmp.eq(DrawBuyRecord::getBai, draw.getDrawResult2T())
                .or().eq(DrawBuyRecord::getShi, draw.getDrawResult3T())
                .or().eq(DrawBuyRecord::getGe, draw.getDrawResult4T()));*/
        query8.in(DrawBuyRecord::getBuyCodes, codes1DList);
        List<DrawBuyRecord> list1D = drawBuyRecordDAO.selectList(query8);
        BigDecimal drawMoney7 = updateOneOddsBuyRecords(list1D, false);
        drawCount = drawCount + list1D.size();
        totalDrawMoney = totalDrawMoney.add(drawMoney7);

        // 1D-猜1D
        LambdaQueryWrapper<DrawBuyRecord> query9 = new LambdaQueryWrapper<>();
        query9.eq(DrawBuyRecord::getBackCodeFlag, 0);
        query9.eq(DrawBuyRecord::getDrawId, drawId);
        query9.eq(DrawBuyRecord::getLotterSettingId, "14");
        query9.in(DrawBuyRecord::getHuizongFlag, Arrays.asList("0", "-1"));
        List<DrawBuyRecord> listC1D = drawBuyRecordDAO.selectList(query9);
        BigDecimal drawMoney8 = BigDecimal.ZERO;
        if (!listC1D.isEmpty()) {
            for (DrawBuyRecord drawBuyRecord : listC1D) {
                if (drawBuyRecord.getParam3().contains("/")) {
                    // 获取相同号码个数
                    int countSame = CodeUtils.countSameStr(draw.getDrawResult(), drawBuyRecord.getBuyCodes());
                    String odds;
                    if (countSame == 1) {
                        odds = drawBuyRecord.getParam3().split("/")[0];
                        BigDecimal dm = drawBuyRecord.getBuyMoney().multiply(new BigDecimal(odds));
                        LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper();
                        uw.set(DrawBuyRecord::getDrawStatus,1);
                        uw.set(DrawBuyRecord::getDrawLotteryFlag,1);
                        uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(odds));
                        uw.set(DrawBuyRecord::getDrawMoney, dm);
                        uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                        uw.eq(DrawBuyRecord::getDrawId,drawId);
                        drawMoney8 = drawMoney8.add(dm);
                        drawBuyRecordDAO.update(drawBuyRecord, uw);
                        drawCount ++;
                    }else if (countSame == 2) {
                        odds = drawBuyRecord.getParam3().split("/")[1];
                        BigDecimal dm = drawBuyRecord.getBuyMoney().multiply(new BigDecimal(odds));
                        LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper();
                        uw.set(DrawBuyRecord::getDrawStatus,1);
                        uw.set(DrawBuyRecord::getDrawLotteryFlag,1);
                        uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(odds));
                        uw.set(DrawBuyRecord::getDrawMoney, dm);
                        uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                        uw.eq(DrawBuyRecord::getDrawId,drawId);
                        drawMoney8 = drawMoney8.add(dm);
                        drawBuyRecordDAO.update(drawBuyRecord, uw);
                        drawCount ++;
                    }else if (countSame == 3) {
                        odds = drawBuyRecord.getParam3().split("/")[2];
                        BigDecimal dm = drawBuyRecord.getBuyMoney().multiply(new BigDecimal(odds));
                        LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper();
                        uw.set(DrawBuyRecord::getDrawStatus,1);
                        uw.set(DrawBuyRecord::getDrawLotteryFlag,1);
                        uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(odds));
                        uw.set(DrawBuyRecord::getDrawMoney, dm);
                        uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                        uw.eq(DrawBuyRecord::getDrawId,drawId);
                        drawMoney8 = drawMoney8.add(dm);
                        drawBuyRecordDAO.update(drawBuyRecord, uw);
                        drawCount ++;
                    }
                }
            }
        }
        totalDrawMoney = totalDrawMoney.add(drawMoney8);

        // 2D-2D
        List<String> codes2DList = CodeUtils.get2DCodes(draw.getDrawResult());
        LambdaQueryWrapper<DrawBuyRecord> query10 = new LambdaQueryWrapper<>();
        query10.eq(DrawBuyRecord::getBackCodeFlag, 0);
        query10.eq(DrawBuyRecord::getDrawId, drawId);
        query10.eq(DrawBuyRecord::getLotterSettingId, "15");
        query10.in(DrawBuyRecord::getBuyCodes, codes2DList);
        query10.in(DrawBuyRecord::getHuizongFlag, Lists.newArrayList("0", "-1"));
        List<DrawBuyRecord> list2D = drawBuyRecordDAO.selectList(query10);
        BigDecimal drawMoney9 = updateOneOddsBuyRecords(list2D, false);
        drawCount = drawCount + list2D.size();
        totalDrawMoney = totalDrawMoney.add(drawMoney9);

        // 2D-猜2D
        List<String> codesC2DList = CodeUtils.get2XCodes(draw.getDrawResult());
        LambdaQueryWrapper<DrawBuyRecord> query11 = new LambdaQueryWrapper<>();
        query11.eq(DrawBuyRecord::getBackCodeFlag, 0);
        query11.eq(DrawBuyRecord::getDrawId, drawId);
        query11.eq(DrawBuyRecord::getLotterSettingId, "16");
        query11.in(DrawBuyRecord::getBuyCodes, codesC2DList);
        query11.in(DrawBuyRecord::getHuizongFlag, Lists.newArrayList("0", "-1"));
        List<DrawBuyRecord> listC2D = drawBuyRecordDAO.selectList(query11);
        BigDecimal drawMoney10 = BigDecimal.ZERO;
        if (!listC2D.isEmpty()) {
            for (DrawBuyRecord drawBuyRecord : listC2D) {
                if (drawBuyRecord.getParam3().contains("/")) {
                    LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper();
                    uw.set(DrawBuyRecord::getDrawStatus,1);
                    uw.set(DrawBuyRecord::getDrawLotteryFlag,1);
                    String[] oddsList = drawBuyRecord.getParam3().split("/");
                    int countSameCode = CodeUtils.getCountByCode(drawBuyRecord.getBuyCodes());
                    if (countSameCode > 1) { // 两同号
                        String odds = oddsList[1];
                        uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(odds));
                        BigDecimal dm = drawBuyRecord.getBuyMoney().multiply(new BigDecimal(odds));
                        drawMoney10 = drawMoney10.add(dm);
                        uw.set(DrawBuyRecord::getDrawMoney, dm);
                    }else { // 两不同号
                        String odds = oddsList[0];
                        uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(odds));
                        BigDecimal dm = drawBuyRecord.getBuyMoney().multiply(new BigDecimal(odds));
                        drawMoney10 = drawMoney10.add(dm);
                        uw.set(DrawBuyRecord::getDrawMoney, dm);
                    }
                    uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                    uw.eq(DrawBuyRecord::getDrawId,drawId);
                    drawBuyRecordDAO.update(drawBuyRecord, uw);
                    drawCount ++;
                }
            }
        }
        totalDrawMoney = totalDrawMoney.add(drawMoney10);

        // 猜大小中奖
        LambdaQueryWrapper<DrawBuyRecord> query12 = new LambdaQueryWrapper<>();
        query12.eq(DrawBuyRecord::getBackCodeFlag, 0);
        query12.eq(DrawBuyRecord::getDrawId, drawId);
        query12.eq(DrawBuyRecord::getLotterSettingId, 9);
        query12.in(DrawBuyRecord::getHuizongFlag, Lists.newArrayList("0", "-1"));
        if (sum >= 14 && sum <= 27) { // 大于等于19，小于等于27 为大
            query12.eq(DrawBuyRecord::getBuyCodes, "大");
        }else if (sum >= 0 && sum <= 13) { // 大于等于0，小于等于8 为小
            query12.eq(DrawBuyRecord::getBuyCodes, "小");
        }else {
            query12.eq(DrawBuyRecord::getBuyCodes, "");
        }
        List<DrawBuyRecord> dxList = drawBuyRecordDAO.selectList(query12);
        BigDecimal drawMoney11 = updateOneOddsBuyRecords(dxList, false);
        drawCount = drawCount + dxList.size();
        totalDrawMoney = totalDrawMoney.add(drawMoney11);

        // 猜奇偶中奖
        LambdaQueryWrapper<DrawBuyRecord> query13 = new LambdaQueryWrapper<>();
        query13.eq(DrawBuyRecord::getBackCodeFlag, 0);
        query13.eq(DrawBuyRecord::getDrawId, drawId);
        query13.eq(DrawBuyRecord::getLotterSettingId, 10);
        query13.in(DrawBuyRecord::getHuizongFlag, Lists.newArrayList("0", "-1"));
        Integer ge = Integer.valueOf(draw.getDrawResult4T());
        Integer shi = Integer.valueOf(draw.getDrawResult3T());
        Integer bai = Integer.valueOf(draw.getDrawResult2T());
        int jo = bai + shi + ge;
        if (jo % 2 == 0) {
            query13.eq(DrawBuyRecord::getBuyCodes, "偶");
        }else {
            query13.eq(DrawBuyRecord::getBuyCodes, "奇");
        }
        List<DrawBuyRecord> joList = drawBuyRecordDAO.selectList(query13);
        BigDecimal drawMoney12 = updateOneOddsBuyRecords(joList, false);
        drawCount = drawCount + joList.size();
        totalDrawMoney = totalDrawMoney.add(drawMoney12);

        // 猜三同中奖
        /*if (draw.getDrawResult2T().equals(draw.getDrawResult3T()) && draw.getDrawResult3T().equals(draw.getDrawResult4T())) {
            LambdaQueryWrapper<DrawBuyRecord> query12 = new LambdaQueryWrapper<>();
            query12.eq(DrawBuyRecord::getBackCodeFlag, 0);
            query12.eq(DrawBuyRecord::getDrawId, drawId);
            query12.eq(DrawBuyRecord::getLotterSettingId, 11);
            List<DrawBuyRecord> stList = drawBuyRecordDAO.selectList(query12);
            updateOneOddsBuyRecords(stList);
        }*/

        // 拖拉机中奖
        boolean tljFlag = CodeUtils.isTlj(bai, shi, ge);
        if (tljFlag) {
            LambdaQueryWrapper<DrawBuyRecord> query14 = new LambdaQueryWrapper<>();
            query14.eq(DrawBuyRecord::getBackCodeFlag, 0);
            query14.eq(DrawBuyRecord::getDrawId, drawId);
            query14.eq(DrawBuyRecord::getLotterSettingId, 12);
            query14.in(DrawBuyRecord::getHuizongFlag, Lists.newArrayList("0", "-1"));
            List<DrawBuyRecord> tljList = drawBuyRecordDAO.selectList(query14);
            BigDecimal drawMoney13 = updateOneOddsBuyRecords(tljList, false);
            drawCount = drawCount + tljList.size();
            totalDrawMoney = totalDrawMoney.add(drawMoney13);
        }

        // 跨度中奖
        Integer maxNumber = Stream.of(Integer.valueOf(draw.getDrawResult2T()), Integer.valueOf(draw.getDrawResult3T()),
                Integer.valueOf(draw.getDrawResult4T())).max(Comparator.comparing(Integer::valueOf)).get();
        Integer minNumber = Stream.of(Integer.valueOf(draw.getDrawResult2T()), Integer.valueOf(draw.getDrawResult3T()),
                Integer.valueOf(draw.getDrawResult4T())).min(Comparator.comparing(Integer::valueOf)).get();
        Integer kd = maxNumber - minNumber;
        LambdaQueryWrapper<DrawBuyRecord> queryKd = new LambdaQueryWrapper<>();
        queryKd.eq(DrawBuyRecord::getBackCodeFlag, 0);
        queryKd.eq(DrawBuyRecord::getDrawId, drawId);
        queryKd.eq(DrawBuyRecord::getLotteryMethodId, "13");
        queryKd.eq(DrawBuyRecord::getBuyCodes, kd.toString());
        queryKd.in(DrawBuyRecord::getHuizongFlag, Lists.newArrayList("0", "-1"));
        List<DrawBuyRecord> kdList = drawBuyRecordDAO.selectList(queryKd);
        BigDecimal drawMoney14 = updateOneOddsBuyRecords(kdList, false);
        drawCount = drawCount + kdList.size();
        totalDrawMoney = totalDrawMoney.add(drawMoney14);

        // 独胆中奖
        LambdaQueryWrapper<DrawBuyRecord> query15 = new LambdaQueryWrapper<>();
        query15.eq(DrawBuyRecord::getBackCodeFlag, 0);
        query15.eq(DrawBuyRecord::getDrawId, drawId);
        query15.eq(DrawBuyRecord::getLotterSettingId, 52);
        query15.in(DrawBuyRecord::getHuizongFlag, Lists.newArrayList("0", "-1"));
        query15.and(tmp -> tmp.eq(DrawBuyRecord::getBuyCodes, draw.getDrawResult2T())
                .or().eq(DrawBuyRecord::getBuyCodes, draw.getDrawResult3T())
                .or().eq(DrawBuyRecord::getBuyCodes, draw.getDrawResult4T()));
        List<DrawBuyRecord> ddList = drawBuyRecordDAO.selectList(query15);
        BigDecimal drawMoney15 = updateOneOddsBuyRecords(ddList, false);
        drawCount = drawCount + ddList.size();
        totalDrawMoney = totalDrawMoney.add(drawMoney15);

        if (count == 2) {
            BigDecimal drawMoney16 = BigDecimal.ZERO;
            LambdaQueryWrapper<DrawBuyRecord> query16 = new LambdaQueryWrapper<>();
            query16.eq(DrawBuyRecord::getBackCodeFlag, 0);
            query16.eq(DrawBuyRecord::getDrawId, drawId);
            query16.in(DrawBuyRecord::getLotterSettingId, Lists.newArrayList("1000", "1001", "1002", "1003",
                    "1004", "1005", "1006", "1007", "1008"));
            query16.in(DrawBuyRecord::getHuizongFlag, Lists.newArrayList("0", "-1"));
            List<DrawBuyRecord> z3zxList = drawBuyRecordDAO.selectList(query16);
            if (!z3zxList.isEmpty()) {
                for (DrawBuyRecord drawBuyRecord : z3zxList) {
                    List<String> z3CodeList = Code3DCreateUtils.z3Code(drawBuyRecord.getBuyCodes());
                    List<String> codeList = new ArrayList<>();
                    CodeUtils.z3Combine(draw.getDrawResult().toCharArray(), 0, draw.getDrawResult().length(), codeList);
                    if (!z3CodeList.isEmpty() && !codeList.isEmpty()) {
                        if (CollectionUtils.containsAny(z3CodeList, codeList)) {
                            LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper();
                            uw.set(DrawBuyRecord::getDrawStatus,1);
                            uw.set(DrawBuyRecord::getDrawLotteryFlag,1);
                            BigDecimal odds = new BigDecimal(drawBuyRecord.getParam3());
                            BigDecimal dm = new BigDecimal(drawBuyRecord.getParam1()).multiply(odds);
                            uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(drawBuyRecord.getParam3()));
                            uw.set(DrawBuyRecord::getDrawMoney,dm);
                            drawMoney16 = drawMoney16.add(dm);
                            uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                            uw.eq(DrawBuyRecord::getDrawId,drawId);
                            drawBuyRecordDAO.update(drawBuyRecord, uw);
                            drawCount ++;
                        }
                    }
                }
            }
            totalDrawMoney = totalDrawMoney.add(drawMoney16);
        }else if (count == 1) {
            // 按组下注组六组码
            BigDecimal drawMoney17 = BigDecimal.ZERO;
            LambdaQueryWrapper<DrawBuyRecord> query17 = new LambdaQueryWrapper<>();
            query17.eq(DrawBuyRecord::getBackCodeFlag, 0);
            query17.eq(DrawBuyRecord::getDrawId, drawId);
            query17.in(DrawBuyRecord::getLotterSettingId, Lists.newArrayList("2001", "2002", "2003",
                    "2004", "2005", "2006", "2007", "2008"));
            query17.in(DrawBuyRecord::getHuizongFlag, Lists.newArrayList("0", "-1"));
            List<DrawBuyRecord> z6zxList = drawBuyRecordDAO.selectList(query17);
            if (!z6zxList.isEmpty()) {
                for (DrawBuyRecord drawBuyRecord : z6zxList) {
                    List<String> z6CodeList = Code3DCreateUtils.z6Code(drawBuyRecord.getBuyCodes());
                    if (!z6CodeList.isEmpty()) {
                        for (String z6Code : z6CodeList) {
                            int countSame = CodeUtils.countSameStr(draw.getDrawResult(), z6Code);
                            if (countSame == 3) {
                                LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper();
                                uw.set(DrawBuyRecord::getDrawStatus,1);
                                uw.set(DrawBuyRecord::getDrawLotteryFlag,1);
                                BigDecimal odds = new BigDecimal(drawBuyRecord.getParam3());
                                BigDecimal dm = new BigDecimal(drawBuyRecord.getParam1()).multiply(odds);
                                uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(drawBuyRecord.getParam3()));
                                uw.set(DrawBuyRecord::getDrawMoney,dm);
                                drawMoney17 = drawMoney17.add(dm);
                                uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                                uw.eq(DrawBuyRecord::getDrawId,drawId);
                                drawBuyRecordDAO.update(drawBuyRecord, uw);
                                drawCount ++;
                                break;
                            }
                        }
                    }
                }
            }
            totalDrawMoney = totalDrawMoney.add(drawMoney17);
        }

        // 按组下注组六组码
        BigDecimal drawMoney18 = BigDecimal.ZERO;
        LambdaQueryWrapper<DrawBuyRecord> query18 = new LambdaQueryWrapper<>();
        query18.eq(DrawBuyRecord::getBackCodeFlag, 0);
        query18.eq(DrawBuyRecord::getDrawId, drawId);
        query18.in(DrawBuyRecord::getLotterSettingId, Lists.newArrayList("3000", "3001", "3002",
                "3003", "3004", "3005", "3006", "3007"));
        query18.in(DrawBuyRecord::getHuizongFlag, Lists.newArrayList("0", "-1"));
        List<DrawBuyRecord> fszxList = drawBuyRecordDAO.selectList(query18);
        if (!fszxList.isEmpty()) {
            for (DrawBuyRecord drawBuyRecord : fszxList) {
                List<String> fsCodeList = Code3DCreateUtils.zxFushi(drawBuyRecord.getBuyCodes(), drawBuyRecord.getBuyCodes(), drawBuyRecord.getBuyCodes());
                if (!fsCodeList.isEmpty()) {
                    if (fsCodeList.contains(draw.getDrawResult())) {
                        LambdaUpdateWrapper<DrawBuyRecord> uw = new LambdaUpdateWrapper();
                        uw.set(DrawBuyRecord::getDrawStatus,1);
                        uw.set(DrawBuyRecord::getDrawLotteryFlag,1);
                        BigDecimal odds = new BigDecimal(drawBuyRecord.getParam3());
                        BigDecimal dm = new BigDecimal(drawBuyRecord.getParam1()).multiply(odds);
                        uw.set(DrawBuyRecord::getPeiRate, Double.valueOf(drawBuyRecord.getParam3()));
                        uw.set(DrawBuyRecord::getDrawMoney,dm);
                        drawMoney18 = drawMoney18.add(dm);
                        uw.eq(DrawBuyRecord::getId,drawBuyRecord.getId());
                        uw.eq(DrawBuyRecord::getDrawId,drawId);
                        drawBuyRecordDAO.update(drawBuyRecord, uw);
                        drawCount ++;
                        break;
                    }
                }
            }
        }
        totalDrawMoney = totalDrawMoney.add(drawMoney18);
        result.put("drawCount", drawCount);
        result.put("totalDrawAmount", totalDrawMoney);
        return result;
    }

    public static void main(String[] args) {
        int count = CodeUtils.getCountByCode("405");
        System.out.println(count);
        List<String> z6CodeList = Code3DCreateUtils.z6Code("12345678");
        z6CodeList.forEach(str -> {
            int countSame = CodeUtils.countSameStr("405", str);
            if (countSame == 3) {
                System.out.println(str);
            }
        });

    }


    public List<DrawBuyRecord> getDrawPrizeList(Integer drawNo) {
        LambdaQueryWrapper<DrawBuyRecord> query = new LambdaQueryWrapper<>();
        query.eq(DrawBuyRecord::getBackCodeFlag, 0);
        query.eq(DrawBuyRecord::getDrawStatus, 1);
        query.eq(DrawBuyRecord::getDrawId,drawNo);
        return drawBuyRecordDAO.selectList(query);
    }

    public List<DrawBuyRecord> getDrawPrizeList(Integer drawNo,Integer lotteryType) {
        LambdaQueryWrapper<DrawBuyRecord> query = new LambdaQueryWrapper<>();
        query.eq(DrawBuyRecord::getBackCodeFlag, 0);
        query.eq(DrawBuyRecord::getDrawStatus, 1);
        query.eq(DrawBuyRecord::getDrawId,drawNo);
        query.eq(DrawBuyRecord::getBuyType,lotteryType);
        return drawBuyRecordDAO.selectList(query);
    }

    public int clearDrawPrizeList(Integer drawNo, List<String> buyIdList) {
        LambdaUpdateWrapper<DrawBuyRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DrawBuyRecord::getId,buyIdList);
        updateWrapper.eq(DrawBuyRecord::getDrawId,String.valueOf(drawNo));
        updateWrapper.set(DrawBuyRecord::getDrawMoney,BigDecimal.ZERO);
        updateWrapper.set(DrawBuyRecord::getDrawStatus,0);
        updateWrapper.set(DrawBuyRecord::getDrawLotteryFlag,0);
        return drawBuyRecordDAO.update(null,updateWrapper);
    }

    public static Integer getZxDraw(String buyNumber, String drawNumber) {
        char[] c = buyNumber.toCharArray();
        int count = 0;
        for (char c2 : c) {
            if (drawNumber.contains(String.valueOf(c2))) {
                count ++;
            }
        }
        if (count == c.length) {
            return 1;
        }
        return 0;
    }
}
