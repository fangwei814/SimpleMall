package com.fangw.simplemall.seckill.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.fangw.common.utils.R;

@FeignClient("simplemall-product")
public interface ProductFeignService {
    /**
     * 获取sku信息
     * 
     * @param skuId
     * @return
     */
    @GetMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);
}
