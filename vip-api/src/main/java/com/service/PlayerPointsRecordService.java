package com.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.lock.LockTemplate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.beans.DrawBuyRecord;
import com.beans.PlayerBuyRecord;
import com.beans.PlayerPointsRecord;
import com.dao.BotUserDAO;
import com.dao.PlayerPointsRecordDAO;
import com.util.StringUtil;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class PlayerPointsRecordService extends ServiceImpl<PlayerPointsRecordDAO, PlayerPointsRecord> {


    @Autowired
    private PlayerPointsRecordDAO dataDao;


    public IPage<PlayerPointsRecord> getByPage(Integer pageNo, Integer pageSize, Integer upDownType, String playerId, String startTime, String endTime, String botUserId){
        Page<PlayerPointsRecord> page = new Page(pageNo,pageSize);
        LambdaQueryWrapper<PlayerPointsRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(StringUtil.isNotNull(playerId)){
            lambdaQueryWrapper.eq(PlayerPointsRecord::getPlayerId,playerId);
        }else {
            lambdaQueryWrapper.eq(PlayerPointsRecord::getBotUserId,botUserId);
        }

        lambdaQueryWrapper.eq(PlayerPointsRecord::getOptType,upDownType);

        lambdaQueryWrapper.ge(PlayerPointsRecord::getApplyTime,startTime);
        lambdaQueryWrapper.le(PlayerPointsRecord::getApplyTime,endTime);
        lambdaQueryWrapper.in(PlayerPointsRecord::getAuthStatus,1);
        lambdaQueryWrapper.orderByDesc(PlayerPointsRecord::getApplyTime);
        return dataDao.selectPage(page,lambdaQueryWrapper);

    }


    /**
     * 清空玩家上下分记录
     * @param playerId
     */
    public void clearData(String playerId){
        LambdaUpdateWrapper<PlayerPointsRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PlayerPointsRecord::getPlayerId,playerId);
        dataDao.delete(updateWrapper);
    }

    public PlayerPointsRecord countDayData(String botUserId, Date startTime, Date endTime,Integer optType){
        return dataDao.countDayDataBy(botUserId,startTime,endTime,optType);
    }


    /**
     * 根据日期和机器人ID查询上下分记录
     * @param botUserId
     * @param date
     */
    public List<PlayerPointsRecord> getPointsRecordByBotUserId (String botUserId, String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = DateUtil.beginOfDay(sdf.parse(date)); //date的开始时间
        Date endDate = DateUtil.endOfDay(sdf.parse(date)); //date的结束时间
        LambdaQueryWrapper<PlayerPointsRecord> qw = new LambdaQueryWrapper();
        qw.eq(PlayerPointsRecord::getBotUserId,botUserId)
                .ge(PlayerPointsRecord::getApplyTime, startDate)
                .le(PlayerPointsRecord::getApplyTime, endDate);
        return dataDao.selectList(qw);
    }

    /**
     * 根据日期和玩家ID查询上下分记录
     * @param playerId
     * @param date
     */
    public PlayerPointsRecord getPointsRecordByPlayerId (String playerId, String date, Integer optType) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = DateUtil.beginOfDay(sdf.parse(date)); //date的开始时间
        Date endDate = DateUtil.endOfDay(sdf.parse(date)); //date的结束时间
        return dataDao.getSumPointsByPlayerId(playerId, startDate, endDate, String.valueOf(optType));
    }

    public List<PlayerPointsRecord> getUnAuthList(String uid) {
        return dataDao.getUnAuthList(uid);
    }
}
