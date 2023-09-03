package com.fanw.simplemall.thirdparty;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.fanw.simplemall.thirdparty.util.HttpUtils;

@SpringBootTest
public class AliyunMsgTest {
    @Test
    public void sendSms() {
        String host = "https://dfsns.market.alicloudapi.com";
        String path = "/data/send_sms";
        String method = "POST";
        String appcode = "***";
        Map<String, String> headers = new HashMap<String, String>();
        // 最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        // 根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("content", "code:hgw689,expire_at:2");
        bodys.put("phone_number", "13858469662");
        bodys.put("template_id", "TPL_0001");

        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            // 获取response的body
            // System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
