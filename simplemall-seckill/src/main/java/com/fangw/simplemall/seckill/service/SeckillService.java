package com.fangw.simplemall.seckill.service;

import java.util.List;

import com.fangw.simplemall.seckill.to.SeckillSkuRedisTo;

public interface SeckillService {

    /**
     * 远程查询最近 3 天内秒杀的活动 以及 秒杀活动的关联的商品信息
     */
    void uploadSeckillSkuLatest3Days();

    /**
     * 获取当前参与秒杀的商品
     * 
     * @return
     */
    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    /**
     * 获取某个商品的秒杀预告信息
     * 
     * @param skuId
     * @return
     */
    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    /**
     * 秒杀处理，发送消息给MQ
     * 
     * @param killId
     *            存放的key
     * @param key
     *            随机码
     * @param num
     *            购买数量
     * @return 生成的订单号
     */
    String kill(String killId, String key, Integer num);
}
