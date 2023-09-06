package com.fanw.simplemall.thirdparty.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fangw.common.utils.R;
import com.fanw.simplemall.thirdparty.component.SmsComponent;

@RestController
@RequestMapping("/sms")
public class SmsSendController {
    @Autowired
    SmsComponent smsComponent;

    // @GetMapping("/hello")
    // public R hello() {
    // return R.ok().setData("hello");
    // }

    /**
     * 提供给别的服务进行调用
     *
     * @param phone
     *            手机号码
     * @param code
     *            验证码
     * @return
     */
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        smsComponent.sendSmsCode(phone, code);
        return R.ok();
    }
}
