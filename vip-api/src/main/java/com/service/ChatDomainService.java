package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.*;
import com.dao.ChatDomainDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class ChatDomainService extends ServiceImpl<ChatDomainDAO, ChatDomain> {
    @Autowired
    private ChatDomainDAO chatDomainDAO;


    public ChatDomain getOne(){
        LambdaQueryWrapper<ChatDomain> qw = new LambdaQueryWrapper<>();
        qw.eq(ChatDomain::getStatus,1);
        qw.last("limit 1");
        return chatDomainDAO.selectOne(qw);
    }
}
