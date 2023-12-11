package com.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
public class UpdateDueBotUserTask {
    @Autowired
    private BotUserService botUserService;

    /**
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    @Async
    public void task(){
        LambdaQueryWrapper<BotUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.lt(BotUser::getDueDate,new Date());
        LambdaUpdateWrapper<BotUser> qw = new LambdaUpdateWrapper();
        qw.lt(BotUser::getDueDate,new Date());
        qw.set(BotUser::getStatus,2);
        botUserService.update(null,qw);

    }
}
