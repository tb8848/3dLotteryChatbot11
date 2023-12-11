package com.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.PlayerReturnPoints;
import com.dao.PlayerReturnPointsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Description:
 * @Auther: tz
 * @Date: 2023/6/15 14:27
 */
@Service
public class PlayerReturnPointsService extends ServiceImpl<PlayerReturnPointsDAO, PlayerReturnPoints> {

    @Autowired
    private PlayerReturnPointsDAO playerReturnPointsDAO;

    /**
     * 查询玩家当天总回水
     * @param botUserId
     * @param startTime
     * @param endTime
     * @return
     */
    public PlayerReturnPoints countDayData(String botUserId, Date startTime, Date endTime){
        return playerReturnPointsDAO.countDayData(botUserId,startTime,endTime);
    }
}
