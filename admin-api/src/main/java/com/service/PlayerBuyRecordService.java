package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.Draw;
import com.beans.PlayerBuyRecord;
import com.dao.PlayerBuyRecordDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PlayerBuyRecordService extends ServiceImpl<PlayerBuyRecordDAO, PlayerBuyRecord> {

    @Autowired
    private PlayerBuyRecordDAO dataDao;

    @Autowired
    private DrawService drawService;

    public PlayerBuyRecord countDayData(String botUserId,Date startTime,Date endTime){
        return dataDao.countDayDataBy(botUserId,startTime,endTime);
    }

    public PlayerBuyRecord getPlayerByRecordSumByPlayerId(String playerId,Date startTime,Date endTime){
        return dataDao.getPlayerByRecordSumByPlayerId(playerId,startTime,endTime);
    }

    /**
     * 当期报网金额
     * @return
     */
    public PlayerBuyRecord getSumMoneyByBw (String botUserId) {
        Draw draw = drawService.getLastDrawInfo();
        QueryWrapper<PlayerBuyRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sum(buyPoints) as buyPoints");
        queryWrapper.lambda().eq(PlayerBuyRecord::getDrawNo, String.valueOf(draw.getDrawId()))
                .eq(PlayerBuyRecord::getBotUserId, botUserId)
                .eq(PlayerBuyRecord::getBuyType, 0);
        return dataDao.selectOne(queryWrapper);
    }

}
