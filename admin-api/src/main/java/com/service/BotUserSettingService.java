package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.beans.BotUserSetting;
import com.dao.BotUserDAO;
import com.dao.BotUserSettingDAO;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BotUserSettingService extends ServiceImpl<BotUserSettingDAO, BotUserSetting> {

    @Autowired
    private BotUserSettingDAO botUserSettingDAO;


}
