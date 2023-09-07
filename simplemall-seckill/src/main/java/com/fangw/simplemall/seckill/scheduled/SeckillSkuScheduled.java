package com.fangw.simplemall.seckill.scheduled;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fangw.simplemall.seckill.service.SeckillService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SeckillSkuScheduled {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedissonClient redissonClient;

    private final String upload_lock = "seckill:upload:lock";

    // TODO 幂等性处理
    @Scheduled(cron = "* * 3 * * ?")
    public void uploadSeckillSkuLatest3Days() {
        // 1、重复上架无需处理
        log.info("上架秒杀商品的信息");
        // 分布式锁。锁的业务执行完成，状态已经更新完成。释放锁以后，其他人获取到就会拿到最新的状态
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
    }

}
