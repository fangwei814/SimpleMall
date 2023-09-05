package com.fangw.simplemall.order.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.fangw.simplemall.order.vo.MemberAddressVo;

@FeignClient("simplemall-member")

public interface MemberFeignService {
    @GetMapping("/member/memberreceiveaddress/{memberId}/address")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long id);
}
