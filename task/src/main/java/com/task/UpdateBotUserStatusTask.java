package com.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.beans.BotUser;
import com.service.BotUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@EnableAsync
public class UpdateBotUserStatusTask {

    @Autowired
    private BotUserService botUserService;

    /**
     * 零点执行-更新机器人过期数据
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Async
    public void task(){
        LambdaUpdateWrapper<BotUser> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(BotUser::getStatus, 2);
        lambdaUpdateWrapper.lt(BotUser::getDueDate,new Date());
        botUserService.update(lambdaUpdateWrapper);
    }
}
