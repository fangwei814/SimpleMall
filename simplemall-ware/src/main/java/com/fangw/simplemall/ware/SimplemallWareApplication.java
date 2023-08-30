package com.fangw.simplemall.ware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableDiscoveryClient
@EnableTransactionManagement
@EnableFeignClients(basePackages = "com.fangw.simplemall.ware.feign")
public class SimplemallWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimplemallWareApplication.class, args);
    }

}
