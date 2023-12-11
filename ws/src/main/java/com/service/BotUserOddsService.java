package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUserOdds;
import com.dao.BotUserOddsDAO;
import com.google.common.collect.Maps;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description:
 * @Auther: tz
 * @Date: 2023/5/31 14:45
 */
@Service
public class BotUserOddsService extends ServiceImpl<BotUserOddsDAO, BotUserOdds> {

    @Autowired
    private BotUserOddsDAO botUserOddsDAO;

    public BotUserOdds getOneBy(String botUserId,String lmId,String lsId){
        Integer[] excludeIds = {1,3,4,5,6,7,8};
        LambdaQueryWrapper<BotUserOdds> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BotUserOdds::getBotUserId, botUserId);
        queryWrapper.eq(BotUserOdds::getLotteryMethodId, lmId);
        queryWrapper.notIn(BotUserOdds::getLotterySettingId, excludeIds);
        queryWrapper.eq(BotUserOdds::getLotterySettingId, lsId);
        queryWrapper.last("limit 1");
        return botUserOddsDAO.selectOne(queryWrapper);
    }

    public BotUserOdds getOneBy(String botUserId,String lmId,String lsId,Integer lotteryType){
        Integer[] excludeIds = {1,3,4,5,6,7,8};
        LambdaQueryWrapper<BotUserOdds> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BotUserOdds::getBotUserId, botUserId);
        queryWrapper.eq(BotUserOdds::getLotteryMethodId, lmId);
        queryWrapper.notIn(BotUserOdds::getLotterySettingId, excludeIds);
        queryWrapper.eq(BotUserOdds::getLotterySettingId, lsId);
        queryWrapper.eq(BotUserOdds::getLotteryType, lotteryType);
        queryWrapper.last("limit 1");
        return botUserOddsDAO.selectOne(queryWrapper);
    }


    public List<BotUserOdds> getListByUserId (String userId, String lmId) {
        Integer[] excludeIds = {1,3,4,5,6,7,8};
        LambdaQueryWrapper<BotUserOdds> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BotUserOdds::getBotUserId, userId);
        queryWrapper.eq(BotUserOdds::getLotteryMethodId, lmId);
        queryWrapper.notIn(BotUserOdds::getLotterySettingId, excludeIds);
        queryWrapper.orderByAsc(BotUserOdds::getShortNo);
        List<BotUserOdds> botUserOddsList = botUserOddsDAO.selectList(queryWrapper);
        return botUserOddsList;
    }

    public List<BotUserOdds> getListByUserId (String userId, String lmId,Integer lotteryType) {
        Integer[] excludeIds = {1,3,4,5,6,7,8};
        LambdaQueryWrapper<BotUserOdds> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BotUserOdds::getBotUserId, userId);
        queryWrapper.eq(BotUserOdds::getLotteryMethodId, lmId);
        queryWrapper.eq(BotUserOdds::getLotteryType, lotteryType);
        queryWrapper.notIn(BotUserOdds::getLotterySettingId, excludeIds);
        queryWrapper.orderByAsc(BotUserOdds::getShortNo);
        List<BotUserOdds> botUserOddsList = botUserOddsDAO.selectList(queryWrapper);
        return botUserOddsList;
    }
}
