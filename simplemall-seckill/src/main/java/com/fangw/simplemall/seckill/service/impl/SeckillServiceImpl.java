package com.fangw.simplemall.seckill.service.impl;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fangw.common.to.mq.SeckillOrderTo;
import com.fangw.common.utils.R;
import com.fangw.common.vo.MemberRespVo;
import com.fangw.simplemall.seckill.feign.CouponFeignService;
import com.fangw.simplemall.seckill.feign.ProductFeignService;
import com.fangw.simplemall.seckill.interceptor.LoginUserInterceptor;
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
    @Autowired
    private RabbitTemplate rabbitTemplate;

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
                List<String> collect = session.getRelationSkus().stream()
                    .map(item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString())
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

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        // 1.确定秒杀场次
        // 用当前时间来判断
        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSION_CACHE_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SESSION_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long start = Long.parseLong(s[0]);
            long end = Long.parseLong(s[1]);

            if (time >= start && time < end) {
                // 2.获取商品信息
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = hashOps.multiGet(range);
                if (Objects.nonNull(list)) {
                    List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                        SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject((String)item, SeckillSkuRedisTo.class);
                        // seckillSkuRedisTo.setRandomCode(null);
                        return seckillSkuRedisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        // 1.找到所有需要参与秒杀的key
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        Set<String> keys = hashOps.keys();
        if (Objects.nonNull(keys) && !keys.isEmpty()) {
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SeckillSkuRedisTo skuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);

                    long current = new Date().getTime();
                    Long startTime = skuRedisTo.getStartTime();
                    Long endTime = skuRedisTo.getEndTime();
                    if (current >= startTime && current <= endTime) {
                        // 在秒杀活动时
                    } else {
                        // 不在秒杀活动时不应该传递随机码
                        skuRedisTo.setRandomCode("");
                    }
                    return skuRedisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        // 登录信息
        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();

        // 1.获取秒杀商品详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String json = hashOps.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        } else {
            SeckillSkuRedisTo redis = JSON.parseObject(json, SeckillSkuRedisTo.class);
            // 2.校验合法性
            long time = new Date().getTime();
            Long startTime = redis.getStartTime();
            Long endTime = redis.getEndTime();

            long ttl = endTime - time;
            // 校验时间
            if (time >= startTime && time <= endTime) {
                // 校验随机码和商品id是否正确
                String randomCode = redis.getRandomCode();
                String skuId = redis.getPromotionSessionId() + "_" + redis.getSkuId();
                if (randomCode.equals(key) && killId.equals(skuId)) {
                    // 验证购物车数量是否合理
                    if (num <= redis.getSeckillLimit().intValue()) {
                        // 验证这个人是否购买过，幂等性：如果只要秒杀成功，就去占位 userId_SessionId_skuId
                        String redisKey = respVo.getId() + "_" + skuId;

                        // 自动过期
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl,
                            TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            // 占位成功说明从来没有买过
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                            try {
                                boolean tryAcquire = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);

                                // 秒杀成功
                                // 3.快速下单，给MQ发送消息
                                String timeId = IdWorker.getTimeId();
                                SeckillOrderTo orderTo = new SeckillOrderTo();
                                orderTo.setOrderSn(timeId);
                                orderTo.setMemberId(respVo.getId());
                                orderTo.setNum(num);
                                orderTo.setPromotionSessionId(redis.getPromotionSessionId());
                                orderTo.setSkuId(redis.getSkuId());
                                orderTo.setSeckillPrice(redis.getSeckillPrice());

                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);

                                return timeId;
                            } catch (InterruptedException e) {
                                return null;
                            }
                        }
                    } else {
                        // 说明已经买过了
                        return null;
                    }
                }
            } else {
                return null;
            }
        }
        return null;
    }
}
