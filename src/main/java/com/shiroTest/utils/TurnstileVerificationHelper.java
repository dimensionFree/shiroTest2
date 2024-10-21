package com.shiroTest.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "turnstile")
@Component
public class TurnstileVerificationHelper {

    // Cloudflare secret key (从 Cloudflare 仪表盘获取)
    @Setter
    private String secret;

    // 验证 Turnstile 令牌的方法
    public boolean verifyTurnstileToken(String token, String userIP) {
        try {
            // Cloudflare Turnstile 验证端点
            String url = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

            // 创建请求体
            String params = "secret=" + secret + "&response=" + token;
            if (userIP != null && !userIP.isEmpty()) {
                params += "&remoteip=" + userIP;  // 可选项
            }

            // 创建 HttpURLConnection 对象
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 将请求参数发送到 Cloudflare
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = params.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 读取 Cloudflare 的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }


            Map<String, Object> stringObjectMap = JsonUtil.toMap(response.toString());

            // 解析 JSON 响应
            return Boolean.valueOf(stringObjectMap.get("success").toString());

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }// 验证 Turnstile 令牌的方法
    public boolean verifyTurnstileToken(String token) {
        return verifyTurnstileToken(token,null);
    }
}
