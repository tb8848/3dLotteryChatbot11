package com;

import com.alibaba.dubbo.spring.boot.annotation.EnableDubboConfiguration;
import com.util.PasswordUtil;
import com.util.Tools;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ComponentScan({"com.action","com.service", "com.config","com.dao","com.auth"})
@MapperScan({"com.dao"})
@ImportResource("classpath:spring-transaction.xml")
@EnableScheduling
@EnableSwagger2
@EnableDubboConfiguration
public class VipApiApplication {
    public static void main(String[] args) {
        System.out.println(PasswordUtil.jiami("test2022"));

        System.out.println(Tools.substractDigit("上分1000"));
        SpringApplication.run(VipApiApplication.class, args);
    }
}
