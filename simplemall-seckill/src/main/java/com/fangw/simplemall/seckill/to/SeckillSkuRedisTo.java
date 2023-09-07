package com.fangw.simplemall.seckill.to;

import java.math.BigDecimal;

import com.fangw.simplemall.seckill.vo.SkuInfoVo;

import lombok.Data;

@Data
public class SeckillSkuRedisTo {
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 商品秒杀的随机码
     */
    private String randomCode;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;
    /**
     * sku的详细信息
     */
    private SkuInfoVo skuInfo;
    /**
     * 当前商品秒杀活动的开始时间
     */
    private Long startTime;
    /**
     * 当前商品秒杀活动的结束时间
     */
    private Long endTime;
}
