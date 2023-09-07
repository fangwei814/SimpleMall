package com.fangw.simplemall.seckill.config;

import java.io.IOException;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedissionConfig {
    /**
     * 所有对Redisson的使用都是通过RedissonClient对象
     * 
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() throws IOException {
        // 1、创建配置
        Config config = new Config();
        // 可以使用 "rediss://"来启用SSL安全连接
        config.useSingleServer().setAddress("redis://192.168.56.10:6379");

        // 2、根据Config创建出实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
