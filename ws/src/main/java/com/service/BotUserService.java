package com.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.beans.Player;
import com.dao.BotUserDAO;
import com.dao.PlayerDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BotUserService extends ServiceImpl<BotUserDAO, BotUser> {


    @Autowired
    private BotUserDAO dataDAO;

    public int clearPanInfo(String botUserId) {
        LambdaUpdateWrapper<BotUser> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(BotUser::getId,botUserId);
        lambdaUpdateWrapper.set(BotUser::getLogin3dToken,"");
        return dataDAO.update(null,lambdaUpdateWrapper);
    }
}
