package com.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.beans.Player;
import com.beans.PlayerPointsRecord;
import com.dao.PlayerPointsRecordDAO;
import com.util.StringUtil;
import org.apache.ibatis.annotations.Param;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlayerPointsRecordService extends ServiceImpl<PlayerPointsRecordDAO, PlayerPointsRecord> {
    @Autowired
    private PlayerPointsRecordDAO playerPointsRecordDAO;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private BotUserService botUserService;

    public Map<String, Object> listPage (Integer page, Integer limit, String playerName, String botName, String opType, String startTime, String endTime, String userId) {
        Map<String, Object> resultMap = new HashMap<>();
        QueryWrapper<PlayerPointsRecord> query = new QueryWrapper<>();
        if (StringUtil.isNotNull(userId)) {
            query.eq("c.playerId", userId);
        }

        if (StringUtil.isNotNull(playerName)) {
            LambdaQueryWrapper<Player> adminQuery = new LambdaQueryWrapper();
            adminQuery.eq(Player::getNickname, playerName);
            adminQuery.last("limit 1");
            Player admin = playerService.getOne(adminQuery);
            if (null == admin) {
                query.eq("c.playerId", "");
            }else {
                query.eq("c.playerId", admin.getId());
            }
        }

        if (StringUtil.isNotNull(botName)) {
            LambdaQueryWrapper<BotUser> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(BotUser::getLoginName, botName);
            queryWrapper.last("limit 1");
            BotUser botUser = botUserService.getOne(queryWrapper);
            if (null == botUser) {
                query.eq("c.botUserId", "");
            }else {
                query.eq("c.botUserId", botUser.getId());
            }
        }
        if (StringUtil.isNotNull(opType)) {
            query.eq("c.optType", opType);
        }

        if (StringUtil.isNotNull(startTime)) {
            String start = startTime + " 00:00:00";
            query.ge("c.applyTime", start);
        }
        if (StringUtil.isNotNull(endTime)) {
            String end = endTime + " 23:59:59";
            query.le("c.applyTime", end);
        }

        page = (page - 1) * limit;
        List<PlayerPointsRecord> playerPointsRecords = playerPointsRecordDAO.dynamicQuery(query, page, limit);
        Long count = playerPointsRecordDAO.dynamicQueryCount(query);
        resultMap.put("totalSize", count);
        resultMap.put("dataList", playerPointsRecords);
        return resultMap;
    }

    /**
     * 根据机器人ID查询机器人下玩家上下分数据
     * @param botUserId
     * @return
     */
    public List<PlayerPointsRecord> getCurrentDayScorceByBotUserId (String botUserId) {
        LambdaQueryWrapper<Player> playerQueryWrapper = new LambdaQueryWrapper<>();
        playerQueryWrapper.eq(Player::getBotUserId, botUserId);
        playerQueryWrapper.in(Player::getUserType, Lists.newArrayList(1,2));
        List<Player> players = playerService.list(playerQueryWrapper);
        if (!players.isEmpty()) {
            List<String> ids = players.stream().map(Player::getId).collect(Collectors.toList());
            LambdaQueryWrapper<PlayerPointsRecord> queryWrapper = new LambdaQueryWrapper<>();
            Date currDate = DateUtil.date(); //当前时间
            Date startDate = DateUtil.beginOfDay(currDate); //当前时间的开始时间
            Date endDate = DateUtil.endOfDay(currDate); //当前时间的结束时间
            queryWrapper.ge(PlayerPointsRecord::getApplyTime,startDate);
            queryWrapper.le(PlayerPointsRecord::getApplyTime,endDate);
            queryWrapper.in(PlayerPointsRecord::getPlayerId, ids);
            List<PlayerPointsRecord> playerPointsRecords = playerPointsRecordDAO.selectList(queryWrapper);
            return playerPointsRecords;
        }
        return Lists.newArrayList();
    }

    public PlayerPointsRecord getSumPoints (String botUserId, Date startTime, Date endTime, String optType) {
        return playerPointsRecordDAO.getSumPoints(botUserId, startTime, endTime, optType);
    }

    public PlayerPointsRecord getSumPointsByPlayerId (String playerId, Date startTime, Date endTime, String optType) {
        return playerPointsRecordDAO.getSumPointsByPlayerId(playerId, startTime, endTime, optType);
    }
}
