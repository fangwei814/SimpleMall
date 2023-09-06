package com.fangw.simplemall.product;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class RedisTemplateTest {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    public void testStringRedisTemplate() {
        // 存入一个 hello world
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        // 保存数据
        ops.set("hello", "world_" + UUID.randomUUID().toString());
        // 查询数据
        System.out.println("****************************之前保存的数据：" + ops.get("hello"));
    }
}
