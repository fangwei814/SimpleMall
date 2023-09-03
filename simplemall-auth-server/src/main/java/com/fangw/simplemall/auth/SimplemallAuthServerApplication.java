package com.fangw.simplemall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.fangw.simplemall.auth.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class SimplemallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimplemallAuthServerApplication.class, args);
    }

}
