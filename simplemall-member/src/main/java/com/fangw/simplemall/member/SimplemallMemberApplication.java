package com.fangw.simplemall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.fangw.simplemall.member.feign")
public class SimplemallMemberApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimplemallMemberApplication.class, args);
	}

}
