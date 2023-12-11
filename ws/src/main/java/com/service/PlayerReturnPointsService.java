package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.PlayerReturnPoints;
import com.dao.PlayerReturnPointsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlayerReturnPointsService extends ServiceImpl<PlayerReturnPointsDAO, PlayerReturnPoints> {



    @Autowired
    private PlayerReturnPointsDAO playerReturnPointsDAO;


    /**
     * 添加记录
     * @param newOne
     * @return
     */
    public int addOne(PlayerReturnPoints newOne){
        PlayerReturnPoints existOne = getOneBy(newOne.getPlayerId(),newOne.getDrawNo());
        if(null!=existOne){
            return -1;
        }
        return playerReturnPointsDAO.insert(newOne);
    }

    public PlayerReturnPoints getOneBy(String playerId, Integer drawId) {
        LambdaQueryWrapper<PlayerReturnPoints> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(PlayerReturnPoints::getPlayerId,playerId);
        lambdaQueryWrapper.eq(PlayerReturnPoints::getDrawNo,drawId);
        lambdaQueryWrapper.last("limit 1");
        return playerReturnPointsDAO.selectOne(lambdaQueryWrapper);
    }

    public PlayerReturnPoints getOneBy(String playerId, Integer drawId,Integer lotteryType) {
        LambdaQueryWrapper<PlayerReturnPoints> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(PlayerReturnPoints::getPlayerId,playerId);
        lambdaQueryWrapper.eq(PlayerReturnPoints::getDrawNo,drawId);
        lambdaQueryWrapper.eq(PlayerReturnPoints::getLotteryType,lotteryType);
        lambdaQueryWrapper.last("limit 1");
        return playerReturnPointsDAO.selectOne(lambdaQueryWrapper);
    }
}
