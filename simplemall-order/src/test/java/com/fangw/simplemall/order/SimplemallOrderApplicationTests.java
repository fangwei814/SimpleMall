package com.fangw.simplemall.order;

import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fangw.simplemall.order.entity.OrderEntity;

@SpringBootTest
class SimplemallOrderApplicationTests {
    @Autowired
    AmqpAdmin amqpAdmin;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public String createOrderTest() {
        // 此处模拟：省略订单下单成功，并保存到数据库
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(UUID.randomUUID().toString());
        entity.setModifyTime(new Date());
        // 给MQ发送消息
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", entity);
        return "ok";
    }

    /**
     * 创建交换机 TopicExchange FanoutExchange DirectExchange
     */
    @Test
    public void createExchange() {

        /**
         * String name 交换机名字 boolean durable 是否持久化 boolean autoDelete 是否自动删除 Map<String, Object> arguments 参数
         */
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
    }

    @Test
    void contextLoads() {}

}
