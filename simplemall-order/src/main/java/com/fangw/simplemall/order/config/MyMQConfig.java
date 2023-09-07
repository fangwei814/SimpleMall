package com.fangw.simplemall.order.config;

import java.util.HashMap;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MyMQConfig {

    // @RabbitListener(queues = "order.seckill.order.queue")
    // public void listener(SeckillOrderTo entity, Channel channel, Message message) throws IOException {
    // System.out.println("收到");
    // }

    // @RabbitListener(queues = "order.release.order.queue")
    // public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
    // log.info("收到过期的订单信息：准备关闭订单！" + entity.getOrderSn());
    // channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    // }

    /**
     * 反序列化
     * 
     * @param objectMapper
     * @return
     */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * Spring中注入Bean之后，容器中的Binding、Queue、Exchange 都会自动创建（前提是RabbitMQ中没有） RabbitMQ 只要有，@Bean属性发生变化也不会覆盖
     * 
     * @return Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
     */
    @Bean
    public Queue orderDelayQueue() {
        HashMap<String, Object> arguments = new HashMap<>();
        /**
         * x-dead-letter-exchange ：order-event-exchange 设置死信路由 x-dead-letter-routing-key : order.release.order 设置死信路由键
         * x-message-ttl ：60000
         */
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 30000);

        Queue queue = new Queue("order.delay.queue", true, false, false, arguments);
        return queue;
    }

    @Bean
    public Queue orderReleaseOrderQueue() {
        return new Queue("order.release.order.queue", true, false, false);
    }

    @Bean
    public Exchange orderEventExchange() {
        // TopicExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
        return new TopicExchange("order-event-exchange", true, false);
    }

    @Bean
    public Binding orderCreateOrder() {
        // Binding(String destination, Binding.DestinationType destinationType, String exchange, String routingKey,
        // Map<String, Object> arguments)
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange",
            "order.create.order", null);
    }

    @Bean
    public Binding orderReleaseOrder() {
        // Binding(String destination, Binding.DestinationType destinationType, String exchange, String routingKey,
        // Map<String, Object> arguments)
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE, "order-event-exchange",
            "order.release.order", null);
    }

    /**
     * 订单释放直接和库存释放进行绑定
     * 
     * @return
     */
    @Bean
    public Binding orderReleaseOtherBingding() {
        // Binding(String destination, Binding.DestinationType destinationType, String exchange, String routingKey,
        // Map<String, Object> arguments)
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "order-event-exchange",
            "order.release.other.#", null);
    }
}
