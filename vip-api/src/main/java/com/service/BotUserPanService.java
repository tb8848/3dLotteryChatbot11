package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUserPan;
import com.dao.BotUserDAO;
import com.dao.BotUserPanDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Auther: tz
 * @Date: 2023/6/25 16:39
 */
@Service
public class BotUserPanService extends ServiceImpl<BotUserPanDAO, BotUserPan> {

    @Autowired
    private BotUserPanDAO botUserPanDAO;


    public BotUserPan getOneBy(Integer lotteryType,String botUserId) {
        LambdaQueryWrapper<BotUserPan> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(BotUserPan::getBotUserId,botUserId);
        lambdaQueryWrapper.eq(BotUserPan::getLotteryType,lotteryType);
        lambdaQueryWrapper.last("limit 1");
        return botUserPanDAO.selectOne(lambdaQueryWrapper);
    }

    public int clearInfo(Integer lotteryType,String botUserId) {
        LambdaUpdateWrapper<BotUserPan> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(BotUserPan::getBotUserId,botUserId);
        lambdaUpdateWrapper.eq(BotUserPan::getLotteryType,lotteryType);
        lambdaUpdateWrapper.set(BotUserPan::getLogin3dToken,"");
        lambdaUpdateWrapper.set(BotUserPan::getActiveStatus,0);
        return botUserPanDAO.update(null,lambdaUpdateWrapper);
    }


}
