package com.fangw.simplemall.order.controller;

import java.util.Date;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fangw.simplemall.order.entity.OrderEntity;

@Controller
public class HelloController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    // ......
    @ResponseBody
    @GetMapping("/test/createOrder")
    public String createOrderTest() {
        // 此处模拟：省略订单下单成功，并保存到数据库
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(UUID.randomUUID().toString());
        entity.setModifyTime(new Date());
        // 给MQ发送消息
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", entity);
        return "ok";
    }
}
