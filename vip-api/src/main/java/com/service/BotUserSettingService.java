package com.service;

import com.baomidou.lock.LockTemplate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.beans.BotUserSetting;
import com.dao.BotUserDAO;
import com.dao.BotUserSettingDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BotUserSettingService extends ServiceImpl<BotUserSettingDAO, BotUserSetting> {




    @Autowired
    private BotUserSettingDAO dataDAO;

    @Autowired
    private LockTemplate lockTemplate;


    public BotUserSetting getByUserId(String userId){
        LambdaQueryWrapper<BotUserSetting> qw = new LambdaQueryWrapper();
        qw.eq(BotUserSetting::getBotUserId,userId);
        qw.last("limit 1");
        return dataDAO.selectOne(qw);
    }

}
