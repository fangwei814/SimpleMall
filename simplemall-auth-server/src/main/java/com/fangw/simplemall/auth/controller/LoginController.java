package com.fangw.simplemall.auth.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fangw.common.utils.R;
import com.fangw.simplemall.auth.feign.ThirdPartyFeignService;

@Controller
public class LoginController {
    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    /**
     * 发送短信
     * 
     * @param phone
     * @return
     */
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        String code = UUID.randomUUID().toString().substring(0, 5);

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
