package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.ChatRoom;
import com.beans.Player;
import com.dao.ChatRoomDAO;
import com.dao.PlayerDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ChatRoomService extends ServiceImpl<ChatRoomDAO, ChatRoom> {

    @Autowired
    private ChatRoomDAO dataDao;

    public ChatRoom getByUserId(String userId) {
        LambdaQueryWrapper<ChatRoom> qw = new LambdaQueryWrapper<>();
        qw.eq(ChatRoom::getUserId,userId);
        qw.last("limit 1");
        return dataDao.selectOne(qw);
    }
}
