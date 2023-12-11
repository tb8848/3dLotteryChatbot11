package com.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beans.BotUser;
import com.dao.BotUserDAO;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 账号服务类
 */
@Service
public class BotUserService extends ServiceImpl<BotUserDAO, BotUser> {

    @Autowired
    private BotUserDAO dataDAO;

    public List<BotUser> listBy() {
        LambdaQueryWrapper<BotUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(BotUser::getStatus, Lists.newArrayList(1,2));
        return dataDAO.selectList(lambdaQueryWrapper);
    }

    public int clearPanInfo(String botUserId) {
        LambdaUpdateWrapper<BotUser> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(BotUser::getId,botUserId);
        lambdaUpdateWrapper.set(BotUser::getLogin3dToken,"");
        return dataDAO.update(null,lambdaUpdateWrapper);
    }
}
