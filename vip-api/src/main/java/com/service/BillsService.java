package com.service;

import cn.hutool.core.date.DateUtil;
import com.beans.Player;
import com.beans.PlayerBuyRecord;
import com.beans.PlayerPointsRecord;
import com.google.common.collect.Maps;
import com.model.res.BillsDetailRes;
import com.model.res.BillsRes;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BillsService {
    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    @Autowired
    private PlayerPointsRecordService playerPointsRecordService;

    @Autowired
    private PlayerService playerService;

    /**
     * 获取账单信息
     * @param botUserId 机器人ID
     * @param date 查询日期
     * @return
     */
    public BillsRes getBillsInfo (String botUserId, String date) throws ParseException {
        BillsRes billsRes = new BillsRes();
        List<BillsDetailRes> billsDetailResList = Lists.newArrayList();
        Map<String, BillsDetailRes> billsMap = Maps.newHashMap();
        // 根据机器人ID 查询上下分数据
        List<PlayerPointsRecord> playerPointsRecordList = playerPointsRecordService.getPointsRecordByBotUserId(botUserId, date);
        if (!playerPointsRecordList.isEmpty()) {
            Map<String, List<PlayerPointsRecord>> map = playerPointsRecordList.stream().collect(Collectors.groupingBy(PlayerPointsRecord::getPlayerId));
            map.forEach((key, value) -> {
                BillsDetailRes billsDetailRes = new BillsDetailRes();
                Player player = playerService.getById(key);
                if (player.getUserType() != 0 && player.getPretexting() == 0) {
                    billsDetailRes.setHeadImg(player.getHeadimg());
                    billsDetailRes.setUsername(player.getNickname());
                    BigDecimal upScore = value.stream().filter(playerPointsRecord -> playerPointsRecord.getOptType() == 0).map(PlayerPointsRecord::getPoints).reduce(BigDecimal.ZERO, BigDecimal::add);
                    billsDetailRes.setUpScore(upScore);
                    BigDecimal downScore = value.stream().filter(playerPointsRecord -> playerPointsRecord.getOptType() == 1).map(PlayerPointsRecord::getPoints).reduce(BigDecimal.ZERO, BigDecimal::add);
                    billsDetailRes.setDownScore(downScore);
                    billsMap.put(key, billsDetailRes);
                }
            });
        }
        // 根据机器人ID 查询总投和总回水
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startTime = DateUtil.beginOfDay(sdf.parse(date));
        Date endTime = DateUtil.endOfDay(sdf.parse(date));
        List<PlayerBuyRecord> playerBuyRecordList = playerBuyRecordService.getPlayerByRecordSumByBotUserId(botUserId, startTime, endTime);
        if (!playerBuyRecordList.isEmpty()) {
            Map<String, List<PlayerBuyRecord>> recordMap = playerBuyRecordList.stream().collect(Collectors.groupingBy(PlayerBuyRecord::getPlayerId));
            recordMap.forEach((key, value) -> {
                BigDecimal totalMoney = value.stream().map(PlayerBuyRecord::getBuyPoints).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalHs = value.stream().map(PlayerBuyRecord::getHsPoints).reduce(BigDecimal.ZERO, BigDecimal::add);
                if (billsMap.size() > 0 && billsMap.containsKey(key)) {
                    BillsDetailRes billsDetailRes = billsMap.get(key);
                    billsDetailRes.setTotalMoney(totalMoney);
                    billsDetailRes.setTotalHs(totalHs);
                    billsMap.put(key, billsDetailRes);
                }else {
                    BillsDetailRes billsDetailRes = new BillsDetailRes();
                    Player player = playerService.getById(key);
                    if (player.getUserType() != 0 && player.getPretexting() == 0) {
                        billsDetailRes.setHeadImg(player.getHeadimg());
                        billsDetailRes.setUsername(player.getNickname());
                        billsDetailRes.setTotalMoney(totalMoney);
                        billsDetailRes.setTotalHs(totalHs);
                        billsMap.put(key, billsDetailRes);
                    }
                }
            });
        }

        billsMap.forEach((key, value) -> {
            billsDetailResList.add(value);
        });

        billsRes.setBillsDetailResList(billsDetailResList);
        BigDecimal botUpScore = billsDetailResList.stream().map(BillsDetailRes::getUpScore).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal botDownScore = billsDetailResList.stream().map(BillsDetailRes::getDownScore).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal botTotalMoney = billsDetailResList.stream().map(BillsDetailRes::getTotalMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal botTotalHs = billsDetailResList.stream().map(BillsDetailRes::getTotalHs).reduce(BigDecimal.ZERO, BigDecimal::add);
        billsRes.setBotUpScore(botUpScore);
        billsRes.setBotDownScore(botDownScore);
        billsRes.setBotTotalMoney(botTotalMoney);
        billsRes.setBotTotalHs(botTotalHs);
        return billsRes;
    }
}
