package com.fangw.simplemall.member.feign;

import com.fangw.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("simplemall-coupon")
public interface CouponFeignService {
    @RequestMapping("/coupon/coupon/member/list")
    R memberCoupon();
}
