package com.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.beans.PlayerPointsRecord;
import com.dao.BotUserDAO;
import com.dao.PlayerPointsRecordDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlayerPointsRecordService extends ServiceImpl<PlayerPointsRecordDAO, PlayerPointsRecord> {


    @Autowired
    private PlayerPointsRecordDAO dataDAO;

}
