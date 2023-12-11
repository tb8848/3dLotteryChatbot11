package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.beans.BotUserPan;
import com.dao.BotUserDAO;
import com.dao.BotUserPanDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BotUserPanService extends ServiceImpl<BotUserPanDAO, BotUserPan> {


    @Autowired
    private BotUserPanDAO dataDAO;

    public int clearPanInfo(String botUserId,Integer lotteryType) {
        LambdaUpdateWrapper<BotUserPan> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(BotUserPan::getBotUserId,botUserId);
        lambdaUpdateWrapper.eq(BotUserPan::getLotteryType,lotteryType);
        lambdaUpdateWrapper.set(BotUserPan::getLogin3dToken,"");
        lambdaUpdateWrapper.set(BotUserPan::getActiveStatus,0);
        return dataDAO.update(null,lambdaUpdateWrapper);
    }



    public BotUserPan getOneBy(String botUserId,Integer lotteryType) {
        LambdaQueryWrapper<BotUserPan> qw = new LambdaQueryWrapper<>();
        qw.eq(BotUserPan::getBotUserId,botUserId);
        qw.eq(BotUserPan::getLotteryType,lotteryType);
        qw.last("limit 1");
        return dataDAO.selectOne(qw);
    }
}
