package com.fangw.simplemall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableCaching
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.fangw.simplemall.product.feign")
public class SimplemallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimplemallProductApplication.class, args);
    }

}
