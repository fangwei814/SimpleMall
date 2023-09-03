package com.fangw.simplemall.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fangw.common.utils.HttpUtils;
import com.fangw.common.utils.R;
import com.fangw.simplemall.auth.feign.MemberFeignService;
import com.fangw.simplemall.auth.vo.MemberRespVo;
import com.fangw.simplemall.auth.vo.SocialUser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class OAuth2Controller {
    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 社交登陆回调，表面是weibo其实是gitee
     * 
     * @param code
     * @return
     * @throws Exception
     */
    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code) throws Exception {
        Map<String, String> header = new HashMap<>();
        Map<String, String> query = new HashMap<>();

        // 1、根据code换取accessToken
        HashMap<String, String> map = new HashMap<>();
        map.put("client_id", "4beffc212972ae36aaad7c3e062f6a0ba084b3cdde8275f5c64597f7b67f9af2");
        map.put("client_secret", "928e61b92f161f7c35d09bc1de13173c92aacf83e1219c71053ba43f1a6aa55c");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.simplemall.com/oauth2.0/weibo/success");
        map.put("code", code);
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", header, query, map);

        // 2、处理
        if (response.getStatusLine().getStatusCode() == 200) {
            // 获取到了accessToken
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            // 知道当前是哪个社交用户
            // 1）、当前用户如果是第一次登录，则自动注册进来（为当前社交用户生成一个会员信息，以后这个社交账号就对应指定的会员）
            // 登录或者注册
            R oauth2Login = memberFeignService.oauth2Login(socialUser);
            if (oauth2Login.getCode() == 0) {
                // 3、登录成功提取信息并跳回首页
                MemberRespVo data = oauth2Login.getData("data", new TypeReference<MemberRespVo>() {});
                log.info("登录成功：用户:{}", data.toString());
                return "redirect:http://simplemall.com";
            } else {
                return "redirect:http://auth.simplemall.com/login.html";
            }
        } else {
            return "redirect:http://auth.simplemall.com/login.html";
        }
    }
}
