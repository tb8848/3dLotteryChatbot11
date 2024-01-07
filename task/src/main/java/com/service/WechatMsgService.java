package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.beans.WechatMsg;
import com.dao.BotUserDAO;
import com.dao.WechatMsgDAO;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 账号服务类
 */
@Service
public class WechatMsgService extends ServiceImpl<WechatMsgDAO, WechatMsg> {

    @Autowired
    private WechatMsgDAO dataDAO;



    public boolean checkExist(String msgId){
        LambdaQueryWrapper<WechatMsg> qw = new LambdaQueryWrapper();
        qw.eq(WechatMsg::getMsgId,msgId);
        long cc = dataDAO.selectCount(qw);
        if(cc>0){
            return true;
        }
        return false;
    }

}
