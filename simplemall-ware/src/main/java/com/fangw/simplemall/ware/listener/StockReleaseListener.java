package com.fangw.simplemall.ware.listener;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fangw.common.to.OrderTo;
import com.fangw.common.to.mq.StockLockedTo;
import com.fangw.simplemall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    /**
     * 库存自己过期处理
     *
     * @param to
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {

        log.info("收到解锁库存的消息");
        try {
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("消费失败", e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    /**
     * 订单关闭处理
     *
     * @param orderTo
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {

        System.out.println("订单关闭准备解锁库存");
        try {
            wareSkuService.unlockStock(orderTo);
            // 手动删除消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 解锁失败 将消息重新放回队列，让别人消费
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
