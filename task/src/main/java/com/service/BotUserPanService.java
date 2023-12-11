package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.beans.BotUserPan;
import com.dao.BotUserDAO;
import com.dao.BotUserPanDAO;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 账号服务类
 */
@Service
public class BotUserPanService extends ServiceImpl<BotUserPanDAO, BotUserPan> {

    @Autowired
    private BotUserPanDAO dataDAO;

    public List<BotUserPan> listBy(String botUserId) {
        LambdaQueryWrapper<BotUserPan> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(BotUserPan::getBotUserId, botUserId);
        return dataDAO.selectList(lambdaQueryWrapper);
    }

    /**
     * 根据用户ID和彩种，获取盘口信息
     * @param lotteryType
     * @param botUserId
     * @return
     */
    public BotUserPan getOneBy(Integer lotteryType,String botUserId) {
        LambdaQueryWrapper<BotUserPan> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(BotUserPan::getBotUserId,botUserId);
        lambdaQueryWrapper.eq(BotUserPan::getLotteryType,lotteryType);
        lambdaQueryWrapper.last("limit 1");
        return dataDAO.selectOne(lambdaQueryWrapper);
    }


    public int clearInfo(Integer lotteryType,String botUserId) {
        LambdaUpdateWrapper<BotUserPan> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(BotUserPan::getBotUserId,botUserId);
        lambdaUpdateWrapper.eq(BotUserPan::getLotteryType,lotteryType);
        lambdaUpdateWrapper.set(BotUserPan::getLogin3dToken,"");
        lambdaUpdateWrapper.set(BotUserPan::getActiveStatus,0);
        return dataDAO.update(null,lambdaUpdateWrapper);
    }
}
