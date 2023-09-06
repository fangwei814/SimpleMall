package com.fangw.simplemall.auth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fangw.common.utils.R;
import com.fangw.simplemall.auth.vo.SocialUser;
import com.fangw.simplemall.auth.vo.UserLoginVo;
import com.fangw.simplemall.auth.vo.UserRegistVo;

@FeignClient("simplemall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    public R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R oauth2Login(SocialUser socialUser) throws Exception;
}
