package com.fangw.simplemall.order.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.fangw.simplemall.order.vo.OrderItemVo;

@FeignClient("simplemall-cart")
public interface CartFeignService {
    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();
}
