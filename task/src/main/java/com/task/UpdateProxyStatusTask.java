package com.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.beans.BotUser;
import com.beans.Proxy;
import com.service.BotUserService;
import com.service.ProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@EnableAsync
public class UpdateProxyStatusTask {
    @Autowired
    private ProxyService proxyService;

    /**
     * 零点执行-更新代理过期数据
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Async
    public void task(){
        LambdaUpdateWrapper<Proxy> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(Proxy::getStatus, 2);
        lambdaUpdateWrapper.lt(Proxy::getDueTime,new Date());
        proxyService.update(lambdaUpdateWrapper);
    }
}
