package com.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUserSetting;
import com.beans.ChatRoom;
import com.dao.BotUserSettingDAO;
import com.dao.ChatRoomDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatRoomService extends ServiceImpl<ChatRoomDAO, ChatRoom> {

    @Autowired
    private ChatRoomDAO dataDAO;


}
