package com.fangw.simplemall.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyMQConfig {
    // @RabbitListener(queues = "order.seckill.order.queue")
    // public void listener(SeckillOrderTo entity, Channel channel, Message message) throws IOException {
    // System.out.println("收到");
    // }

    @Bean
    public Queue orderSeckillOrderQueue() {
        return new Queue("order.seckill.order.queue", true, false, false);
    }

    @Bean
    public Binding orderSeckillOrderQueueBinding() {
        return new Binding("order.seckill.order.queue", Binding.DestinationType.QUEUE, "order-event-exchange",
            "order.seckill.order", null);
    }

}
