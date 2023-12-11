package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.Player;
import com.dao.PlayerDAO;
import com.google.common.collect.Lists;
import com.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService extends ServiceImpl<PlayerDAO, Player> {
    @Autowired
    private PlayerDAO playerDAO;

    /**
     * 根据机器人ID查询会员
     * @param nickName
     * @param pageNo
     * @param pageSize
     * @return
     */
    public IPage<Player> listPagerByBotUserId (String userId,String nickName,String userType,String pretexting, String reportNet, String eatPrize,Integer pageNo,Integer pageSize) {
        LambdaQueryWrapper<Player> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        Page<Player> page = new Page<>(pageNo,pageSize);
        if(StringUtil.isNotNull(nickName)){
            lambdaQueryWrapper.like(Player::getNickname,nickName);
        }
        if (StringUtil.isNotNull(userType)) {
            lambdaQueryWrapper.eq(Player::getUserType, userType);
        }
        if (StringUtil.isNotNull(pretexting)) {
            lambdaQueryWrapper.eq(Player::getPretexting, pretexting);
        }
        if (StringUtil.isNotNull(reportNet)) {
            lambdaQueryWrapper.eq(Player::getReportNet, reportNet);
        }
        if (StringUtil.isNotNull(eatPrize)) {
            lambdaQueryWrapper.eq(Player::getEatPrize, eatPrize);
        }
        lambdaQueryWrapper.eq(Player::getBotUserId, userId);
        lambdaQueryWrapper.in(Player::getUserType, Lists.newArrayList(1,2));

        return playerDAO.selectPage(page,lambdaQueryWrapper);
    }

    /**
     * 根据机器人ID获取玩家列表
     * @param botUserId
     * @return
     */
    public List<Player> getPlayerListByBotUserId (String botUserId) {
        LambdaQueryWrapper<Player> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Player::getBotUserId, botUserId);
        lambdaQueryWrapper.in(Player::getUserType, Lists.newArrayList(1,2));
        return playerDAO.selectList(lambdaQueryWrapper);
    }
}
