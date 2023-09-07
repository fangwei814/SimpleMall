package com.fangw.simplemall.seckill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fangw.simplemall.seckill.scheduled.SeckillSkuScheduled;

@SpringBootTest
class SimplemallSeckillApplicationTests {
    @Autowired
    SeckillSkuScheduled skuScheduled;

    @Test
    void scheduledTest() {
        skuScheduled.uploadSeckillSkuLatest3Days();
    }

    @Test
    void contextLoads() {}

}
