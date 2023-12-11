package com.config.mybatisplus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySqlInjectorConfig {

    @Bean
    public MySqlInjector sqlInjector() {
        return new MySqlInjector();
    }

}
