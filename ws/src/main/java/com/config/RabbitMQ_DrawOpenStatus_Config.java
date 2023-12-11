package com.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分类账 消息队列
 */
@Configuration
@Deprecated
public class RabbitMQ_DrawOpenStatus_Config {

    @Bean("exchange_draw_openstatus_p3")
    public FanoutExchange defaultExchange() {
        return new FanoutExchange("exchange_draw_openstatus_p3");
    }

    @Bean("binding_draw_openstatus_p3")
    public Binding binding(@Qualifier("exchange_draw_openstatus_p3") FanoutExchange exchange,
                           @Qualifier ("messageQuen_draw_openstatus_p3") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean("messageQuen_draw_openstatus_p3")
    public Queue getQueue(){
        return new Queue("messageQuen_draw_openstatus_p3") ;
    }

}
