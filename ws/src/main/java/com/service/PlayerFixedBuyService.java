package com.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.*;
import com.dao.PlayerFixedBuyDAO;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PlayerFixedBuyService extends ServiceImpl<PlayerFixedBuyDAO, PlayerFixedBuy> {


    @Autowired
    private PlayerFixedBuyDAO dataDao;


    public int updateStartTime(Date startTime) {
        LambdaUpdateWrapper<PlayerFixedBuy> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(PlayerFixedBuy::getStartTime, startTime);
        updateWrapper.in(PlayerFixedBuy::getTaskStatus, Lists.newArrayList(0,1));
        return dataDao.update(null,updateWrapper);
    }

    public int updateStartTime(Date startTime,Integer lotteryType) {
        LambdaUpdateWrapper<PlayerFixedBuy> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(PlayerFixedBuy::getStartTime, startTime);
        updateWrapper.in(PlayerFixedBuy::getTaskStatus, Lists.newArrayList(0,1));
        updateWrapper.eq(PlayerFixedBuy::getLotteryType,lotteryType);
        return dataDao.update(null,updateWrapper);
    }
}
