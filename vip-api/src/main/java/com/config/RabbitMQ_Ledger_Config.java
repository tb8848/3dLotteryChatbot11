package com.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分类账消息队列配置
 */
@Configuration
public class RabbitMQ_Ledger_Config {

    @Bean("exchange_ledger")
    public FanoutExchange defaultExchange() {
        return new FanoutExchange("exchange_ledger");
    }

    @Bean("binding_ledger")
    public Binding bindingLedger(@Qualifier("exchange_ledger") FanoutExchange exchange, @Qualifier ("messageQuen_ledger") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean("messageQuen_ledger")
    public Queue getQueue(){
        return new Queue("messageQuen_ledger") ;
    }

}
