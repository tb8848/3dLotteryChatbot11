package com.service;

import com.baomidou.lock.LockTemplate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.beans.WechatIpadToken;
import com.dao.BotUserDAO;
import com.dao.WechatIpadTokenDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WechatIpadTokenService extends ServiceImpl<WechatIpadTokenDAO, WechatIpadToken> {

    @Autowired
    private WechatIpadTokenDAO dataDAO;



    public WechatIpadToken getUnuseOne(){
        LambdaQueryWrapper<WechatIpadToken> qw = new LambdaQueryWrapper<>();
        qw.eq(WechatIpadToken::getUseFlag,0);
        qw.last("limit 1");
        return dataDAO.selectOne(qw);
    }

    public WechatIpadToken getByUserId(String uid) {
        LambdaQueryWrapper<WechatIpadToken> qw = new LambdaQueryWrapper<>();
        qw.eq(WechatIpadToken::getUseFlag,1);
        qw.eq(WechatIpadToken::getBotUserId,uid);
        qw.last("limit 1");
        return dataDAO.selectOne(qw);
    }
}
