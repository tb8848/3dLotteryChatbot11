package com.config;


import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;

@Configuration
public class IDSnowflakeConfig {

    private  long workerId;
    private  long datacenterId;


    @PostConstruct
    public void init()
    {
        try {
            workerId   = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr());
            workerId = workerId%31;
//            System.out.println("当前机器的workerId:{}" + workerId);
        } catch (Exception e) {
//            System.out.println("当前机器的workerId获取失败" + e);
            workerId = NetUtil.getLocalhostStr().hashCode();
            workerId = workerId%31;
//            System.out.println("当前机器 workId:{}" + workerId);
        }
    }

    @Bean
    @Primary
    public Snowflake getSnowflake()
    {
        return  IdUtil.createSnowflake(workerId,datacenterId);
    }



}
