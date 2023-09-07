package com.fangw.simplemall.seckill.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.fangw.common.utils.R;

@FeignClient("simplemall-coupon")
public interface CouponFeignService {
    /**
     * 远程查询最近 3 天内秒杀的活动 以及 秒杀活动的关联的商品信息
     * 
     * @return
     */
    @GetMapping("/coupon/seckillsession/lates3DaySession")
    R getLates3DaySession();
}
