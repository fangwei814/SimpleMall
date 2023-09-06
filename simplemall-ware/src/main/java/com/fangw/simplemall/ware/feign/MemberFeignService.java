package com.fangw.simplemall.ware.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fangw.common.utils.R;

@FeignClient("simplemall-member")
public interface MemberFeignService {

    /**
     * 根据地址id查询地址的详细信息
     * 
     * @param id
     * @return
     */
    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R addrInfo(@PathVariable("id") Long id);
}
