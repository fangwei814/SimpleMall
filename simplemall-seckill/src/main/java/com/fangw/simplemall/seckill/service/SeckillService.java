package com.fangw.simplemall.seckill.service;

public interface SeckillService {

    /**
     * 远程查询最近 3 天内秒杀的活动 以及 秒杀活动的关联的商品信息
     */
    void uploadSeckillSkuLatest3Days();
}
