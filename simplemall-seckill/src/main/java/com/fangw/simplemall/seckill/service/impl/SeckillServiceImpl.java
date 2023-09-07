package com.fangw.simplemall.seckill.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fangw.common.utils.R;
import com.fangw.simplemall.seckill.feign.CouponFeignService;
import com.fangw.simplemall.seckill.feign.ProductFeignService;
import com.fangw.simplemall.seckill.service.SeckillService;
import com.fangw.simplemall.seckill.to.SeckillSkuRedisTo;
import com.fangw.simplemall.seckill.vo.SeckillSessionsWithSkus;
import com.fangw.simplemall.seckill.vo.SkuInfoVo;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus:";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:"; // + 商品随机码

    /**
     * 缓存活动信息
     * 
     * @param sessions
     */
    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions) {
        sessions.forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;
            Boolean flag = redisTemplate.hasKey(key);
            if (Objects.nonNull(flag) && !flag) {
                List<String> collect = session.getRelationSkus().stream().map(item -> item.getSkuId().toString())
                    .collect(Collectors.toList());
                // 缓存活动信息
                redisTemplate.opsForList().leftPushAll(key, collect);
            }
        });
    }

    /**
     * 缓存活动关联的商品信息
     * 
     * @param sessions
     */
    private void saveSessionSkuInfo(List<SeckillSessionsWithSkus> sessions) {
        sessions.forEach(session -> {
            // 准备redis hash操作
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().forEach(seckillSkuVo -> {
                String token = UUID.randomUUID().toString().replace("_", "");
                // 缓存商品
                if (Boolean.FALSE.equals(ops.hasKey(
                    seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString()))) {
                    SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();

                    // 1.sku基本数据
                    R skuInfo = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if (skuInfo.getCode() == 0) {
                        SkuInfoVo info = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
                        seckillSkuRedisTo.setSkuInfo(info);
                    }

                    // 2.sku秒杀信息
                    BeanUtils.copyProperties(seckillSkuVo, seckillSkuRedisTo);

                    // 3.设置秒杀时间
                    seckillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    seckillSkuRedisTo.setEndTime(session.getEndTime().getTime());

                    // 4.商品随机码
                    seckillSkuRedisTo.setRandomCode(token);

                    String jsonString = JSON.toJSONString(seckillSkuRedisTo);
                    ops.put(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString(),
                        jsonString);

                    // 5.引入分布式信号量限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    // 商品可以秒杀的数量作为信号量
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount().intValue());
                }
            });
        });
    }

    @Override
    public void uploadSeckillSkuLatest3Days() {
        // 1.扫描最近三天数据库需要参与秒杀的活动
        R session = couponFeignService.getLates3DaySession();
        if (session.getCode() == 0) {
            // 上架商品
            List<SeckillSessionsWithSkus> sessionData =
                session.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {});
            // 缓存到Redis
            // 1)、缓存活动信息
            saveSessionInfos(sessionData);
            // 2)、缓存活动的关联商品信息
            saveSessionSkuInfo(sessionData);
        }
    }
}
