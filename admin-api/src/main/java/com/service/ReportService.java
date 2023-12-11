package com.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.beans.BotUser;
import com.beans.Player;
import com.beans.PlayerBuyRecord;
import com.beans.PlayerPointsRecord;
import com.model.res.ReportDetailRes;
import com.model.res.ReportRes;
import com.util.StringUtil;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class ReportService {
    @Autowired
    private PlayerPointsRecordService playerPointsRecordService;

    @Autowired
    private PlayerBuyRecordService playerBuyRecordService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private BotUserService botUserService;

    /**
     * 机器人报表数据
     * @return
     */
    public ReportRes getBotReportList (String botName, String startDate, String endDate) throws ParseException {
        ReportRes reportRes = new ReportRes();
        List<ReportDetailRes> reportResList = Lists.newArrayList();
        List<BotUser> botUserList = Lists.newArrayList();
        if (StringUtil.isNotNull(botName)) {
            LambdaQueryWrapper<BotUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BotUser::getLoginName, botName);
            queryWrapper.last("limit 1");
            BotUser botUser = botUserService.getOne(queryWrapper);
            if (null != botUser) {
                botUserList.add(botUser);
            }
        }else {
            botUserList.addAll(botUserService.list());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date start = DateUtil.beginOfDay(sdf.parse(startDate)); //date的开始时间
        Date end = DateUtil.endOfDay(sdf.parse(endDate)); //date的结束时间

        if (!botUserList.isEmpty()) {
            for (BotUser botUser : botUserList) {
                ReportDetailRes reportDetailRes = new ReportDetailRes();
                reportDetailRes.setUserId(botUser.getId());
                reportDetailRes.setUserName(botUser.getLoginName());
                // 上分
                PlayerPointsRecord up = playerPointsRecordService.getSumPoints(botUser.getId(), start, end, "0");
                if (null != up) {
                    reportDetailRes.setUpScore(up.getPoints());
                }
                // 下分
                PlayerPointsRecord down = playerPointsRecordService.getSumPoints(botUser.getId(), start, end, "1");
                if (null != down) {
                    reportDetailRes.setDownScore(down.getPoints());
                }

                PlayerBuyRecord playerBuyRecord = playerBuyRecordService.countDayData(botUser.getId(), start, end);
                if (null != playerBuyRecord) {
                    reportDetailRes.setDrawMoney(playerBuyRecord.getDrawPoints());
                    reportDetailRes.setTotalCount(playerBuyRecord.getBuyAmount());
                    reportDetailRes.setTotalHs(playerBuyRecord.getHsPoints());
                    reportDetailRes.setTotalMoney(playerBuyRecord.getBuyPoints());
                    reportDetailRes.setProfitLossMoney(playerBuyRecord.getEarnPoints());
                }
                reportResList.add(reportDetailRes);
            }
        }
        if (!reportResList.isEmpty()) {
            BigDecimal totalUpScore = reportResList.stream().map(ReportDetailRes::getUpScore).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDownScore = reportResList.stream().map(ReportDetailRes::getDownScore).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDrawMoney = reportResList.stream().map(ReportDetailRes::getDrawMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalProfitLossMoney = reportResList.stream().map(ReportDetailRes::getProfitLossMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer totalCount = reportResList.stream().mapToInt(ReportDetailRes::getTotalCount).sum();
            BigDecimal totalHs = reportResList.stream().map(ReportDetailRes::getTotalHs).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalMoney = reportResList.stream().map(ReportDetailRes::getTotalMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            reportRes.setTotalMoney(totalMoney);
            reportRes.setTotalHs(totalHs);
            reportRes.setTotalCount(totalCount);
            reportRes.setTotalDownScore(totalDownScore);
            reportRes.setTotalUpScore(totalUpScore);
            reportRes.setTotalProfitLossMoney(totalProfitLossMoney);
            reportRes.setTotalDrawMoney(totalDrawMoney);
        }

        reportRes.setReportDetailResList(reportResList);
        return reportRes;
    }

    /**
     * 机器人报表数据
     * @return
     */
    public ReportRes getPlayerReportList (String botName, String startDate, String endDate, String playerId, String botUserId) throws ParseException {
        ReportRes reportRes = new ReportRes();
        List<ReportDetailRes> reportResList = Lists.newArrayList();
        List<Player> playerList = Lists.newArrayList();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date start = DateUtil.beginOfDay(sdf.parse(startDate)); //date的开始时间
        Date end = DateUtil.endOfDay(sdf.parse(endDate)); //date的结束时间
        if (StringUtil.isNotNull(playerId)) {
            ReportDetailRes reportDetailRes = new ReportDetailRes();
            Player player = playerService.getById(playerId);
            reportDetailRes.setUserId(player.getId());
            reportDetailRes.setUserName(player.getNickname());
            buildPlayerReport(playerId, start, end, reportDetailRes);
            reportResList.add(reportDetailRes);
        }else {
            if (StringUtil.isNotNull(botName)) {
                LambdaQueryWrapper<Player> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Player::getNickname, botName);
                queryWrapper.eq(Player::getBotUserId, botUserId);
                queryWrapper.in(Player::getUserType, Arrays.asList(1,2));
                List<Player> players = playerService.list(queryWrapper);
                if (!players.isEmpty()) {
                    playerList.addAll(players);
                }
            }else {
                LambdaQueryWrapper<Player> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Player::getBotUserId, botUserId);
                queryWrapper.in(Player::getUserType, Lists.newArrayList(1,2));
                List<Player> players = playerService.list(queryWrapper);
                if (!players.isEmpty()) {
                    playerList.addAll(players);
                }
            }

            if (!playerList.isEmpty()) {
                for (Player player : playerList) {
                    ReportDetailRes reportDetailRes = new ReportDetailRes();
                    reportDetailRes.setUserId(player.getId());
                    reportDetailRes.setUserName(player.getNickname());
                    buildPlayerReport(player.getId(), start, end, reportDetailRes);
                    reportResList.add(reportDetailRes);
                }
            }
        }
        if (!reportResList.isEmpty()) {
            BigDecimal totalUpScore = reportResList.stream().map(ReportDetailRes::getUpScore).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDownScore = reportResList.stream().map(ReportDetailRes::getDownScore).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDrawMoney = reportResList.stream().map(ReportDetailRes::getDrawMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalProfitLossMoney = reportResList.stream().map(ReportDetailRes::getProfitLossMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer totalCount = reportResList.stream().mapToInt(ReportDetailRes::getTotalCount).sum();
            BigDecimal totalHs = reportResList.stream().map(ReportDetailRes::getTotalHs).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalMoney = reportResList.stream().map(ReportDetailRes::getTotalMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            reportRes.setTotalMoney(totalMoney);
            reportRes.setTotalHs(totalHs);
            reportRes.setTotalCount(totalCount);
            reportRes.setTotalDownScore(totalDownScore);
            reportRes.setTotalUpScore(totalUpScore);
            reportRes.setTotalProfitLossMoney(totalProfitLossMoney);
            reportRes.setTotalDrawMoney(totalDrawMoney);
        }
        reportRes.setReportDetailResList(reportResList);

        return reportRes;
    }

    private void buildPlayerReport(String playerId, Date start, Date end, ReportDetailRes reportDetailRes) {
        // 上分
        PlayerPointsRecord upScore = playerPointsRecordService.getSumPointsByPlayerId(playerId, start, end, "0");
        if (null != upScore) {
            reportDetailRes.setUpScore(upScore.getPoints());
        }

        // 下分
        PlayerPointsRecord downScore = playerPointsRecordService.getSumPointsByPlayerId(playerId, start, end, "1");
        if (null != downScore) {
            reportDetailRes.setDownScore(downScore.getPoints());
        }

        PlayerBuyRecord playerBuyRecord = playerBuyRecordService.getPlayerByRecordSumByPlayerId(playerId, start, end);
        if (null != playerBuyRecord) {
            reportDetailRes.setDrawMoney(playerBuyRecord.getDrawPoints());
            reportDetailRes.setTotalCount(playerBuyRecord.getBuyAmount());
            reportDetailRes.setTotalHs(playerBuyRecord.getHsPoints());
            reportDetailRes.setTotalMoney(playerBuyRecord.getBuyPoints());
            reportDetailRes.setProfitLossMoney(playerBuyRecord.getEarnPoints());
        }
    }
}
