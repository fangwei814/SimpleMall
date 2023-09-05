package com.fangw.simplemall.order;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SimplemallOrderApplicationTests {
    @Autowired
    AmqpAdmin amqpAdmin;

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
