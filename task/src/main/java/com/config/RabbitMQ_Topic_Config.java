package com.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQ_Topic_Config {


    @Bean("exchange_botUserTopic_3d")
    public TopicExchange topicExchange() {
        /**
         * topicExchange的参数说明:
         * 1. 交换机名称
         * 2. 是否持久化 true：持久化，交换机一直保留 false：不持久化，用完就删除
         * 3. 是否自动删除 false：不自动删除 true：自动删除
         */
        return new TopicExchange("exchange_botUserTopic_3d", true, false);
    }

    //机器人聊天信息队列
    @Bean("bot_user_queue_3d")
    public Queue botUserMsgQueue() {
        /**
         * Queue构造函数参数说明
         * 1. 队列名
         * 2. 是否持久化 true：持久化 false：不持久化
         */
        return new Queue("bot_user_queue_3d", true);
    }

    @Bean("bot_user_binding_3d")
    public Binding botUserMsgBinding() {
        return BindingBuilder.bind(botUserMsgQueue()).to(topicExchange()).with("botUserMsg");
    }

}
