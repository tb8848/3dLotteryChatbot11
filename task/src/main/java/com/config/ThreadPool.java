package com.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPool {

    /**
     * 线程池
     * @return
     */
    @Bean
    public ThreadPoolExecutor pool(){
        BlockingDeque deque  = new LinkedBlockingDeque<>(10000);
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(20,50,5000, TimeUnit.MILLISECONDS,deque);
        return poolExecutor;
//        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
//                60L, TimeUnit.SECONDS,
//                new SynchronousQueue<Runnable>());
    }

}
