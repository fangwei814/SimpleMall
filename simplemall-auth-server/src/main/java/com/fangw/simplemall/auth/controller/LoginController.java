package com.fangw.simplemall.auth.controller;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fangw.common.constant.AuthServerConstant;
import com.fangw.common.exception.BizCodeEnum;
import com.fangw.common.utils.R;
import com.fangw.simplemall.auth.feign.ThirdPartyFeignService;

@Controller
public class LoginController {
    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 发送短信
     * 
     * @param phone
     * @return
     */
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        // 1.查询redis中的验证码，用redis防止非法大量调用验证码接口
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.isNotBlank(redisCode)) {
            long codeLong = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - codeLong < 60000) {
                // 60秒内不能重复发送
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        // 2.缓存到redis
        String code = UUID.randomUUID().toString().substring(0, 5);
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone,
            code + "_" + System.currentTimeMillis(), 5, TimeUnit.MINUTES);

        thirdPartyFeignService.sendCode(phone, code);
        return R.ok();
    }
    // @GetMapping("/login.html")
    // public String loginPage() {
    // return "login";
    // }
    //
    // @GetMapping("/reg.html")
    // public String regPage() {
    // return "reg";
    // }
}
