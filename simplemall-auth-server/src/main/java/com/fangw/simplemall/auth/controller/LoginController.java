package com.fangw.simplemall.auth.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.TypeReference;
import com.fangw.common.constant.AuthServerConstant;
import com.fangw.common.exception.BizCodeEnum;
import com.fangw.common.utils.R;
import com.fangw.simplemall.auth.feign.MemberFeignService;
import com.fangw.simplemall.auth.feign.ThirdPartyFeignService;
import com.fangw.simplemall.auth.vo.UserLoginVo;
import com.fangw.simplemall.auth.vo.UserRegistVo;

@Controller
public class LoginController {
    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 登录
     * 
     * @param vo
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes) {
        // 调用远程接口登录
        R login = memberFeignService.login(vo);
        if (login.getCode() == 0) {
            // TODO 登录成功处理
            return "redirect:http://simplemall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>() {}));
            redirectAttributes.addFlashAttribute("errors", errors);

            // 如果校验错误，转发到注册页
            return "redirect:http://auth.simplemall.com/login.html";
        }
    }

    /**
     * 注册
     * 
     * @param vo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        // 1.参数校验结果
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);

            // 如果校验错误，转发到注册页
            return "redirect:http://auth.simplemall.com/reg.html";
        }

        // 2.验证码校验
        String code = vo.getCode();
        String codeInRedis = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (StringUtils.isNotBlank(codeInRedis)) {
            // 判断通过
            if (StringUtils.isNotBlank(code) && code.equals(codeInRedis.split("_")[0])) {
                // 删除验证码
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());

                // 注册账号，也就是调用远程服务注册到会员模块
                R r = memberFeignService.regist(vo);
                if (r.getCode() == 0) {
                    // 注册成功跳转登录页面
                    return "redirect:http://auth.simplemall.com/login.html";
                }
            } else {
                // 验证码出错
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);

                // 如果校验错误，转发到注册页
                return "redirect:http://auth.simplemall.com/reg.html";
            }
        } else {
            // 验证码出错
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);

            // 如果校验错误，转发到注册页
            return "redirect:http://auth.simplemall.com/reg.html";
        }
        return "reg";
    }

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
