package com.fangw.simplemall.ware.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MyRabbitConfig {
    // @Autowired
    // RabbitTemplate rabbitTemplate;

    // @RabbitListener(queues = "stock.release.stock.queue")
    // public void handle(Message message) {
    //
    // }

    /**
     * 使用JSON序列化机制，进行消息转换 https://blog.csdn.net/qq_36565692/article/details/124554505
     *
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 库存服务默认的交换机
     *
     * @return
     */
    @Bean
    public Exchange exchange() {
        return new TopicExchange("stock-event-exchange", true, false);
    }

    /**
     * 普通队列
     *
     * @return
     */
    @Bean
    public Queue stockReleaseStockQueue() {
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    /**
     * 延迟队列
     */
    @Bean
    public Queue stockDelayQueue() {
        // String name, boolean durable, boolean exclusive, boolean autoDelete,
        // @Nullable Map<String, Object> arguments
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        arguments.put("x-message-ttl", 120000);
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }

    /**
     * 交换机和普通队列绑定
     *
     * @return
     */
    @Bean
    public Binding stockReleaseStockBinding() {
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "stock-event-exchange",
            "stock.release.#", new HashMap<>());
    }

    /**
     * 交换机和延迟队列绑定
     *
     * @return
     */
    @Bean
    public Binding orderLockedBinding() {
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE, "stock-event-exchange", "stock.locked",
            new HashMap<>());
    }
}
