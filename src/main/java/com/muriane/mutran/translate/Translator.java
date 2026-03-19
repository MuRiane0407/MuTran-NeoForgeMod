package com.muriane.mutran.translate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.muriane.mutran.config.Config;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class Translator {
    public static String translate(String query, String from, String to){
        String result;
        if (Config.COMMON.TRANSLATION_PROVIDER.get() == Config.TranslationProvider.YOUDAO){
            result = youdaoTranslate(query, from, to);
        }else{
            return "ERROR_TRANSLATION_NOT_FIND";
        }

        return result;
    }

    public static String youdaoTranslate(String query, String from, String to){
        String app_key = Config.COMMON.API_KEY.get();
        String app_secret = Config.COMMON.API_SECRET.get();

        String salt = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String curtime = String.valueOf(System.currentTimeMillis() / 1000);

        String input = query.length() <= 20 ? query : (query.substring(0, 9) + query.length() + query.substring(query.length()-10, query.length()-1));
        String signStr = app_key + input + salt + curtime + app_secret; // 签名生成方法如下： signType=v3； sign=sha256(应用ID+input+salt+curtime+应用密钥)； 其中，input的计算方式为：input=q前10个字符 + q长度 + q后10个字符（当q长度大于20）或 input=q字符串（当q长度小于等于20）；
        String sign = sha256(signStr);

        try(CloseableHttpClient client = HttpClients.createDefault()){
            HttpPost post = new HttpPost(Config.COMMON.TRANSLATION_PROVIDER.get().getApiUrl());

            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            StringBuilder params = new StringBuilder();
            params.append("q=").append(java.net.URLEncoder.encode(query, StandardCharsets.UTF_8));
            params.append("&from=").append(from);
            params.append("&to=").append(to);
            params.append("&appKey=").append(app_key);
            params.append("&salt=").append(salt);
            params.append("&sign=").append(sign);
            params.append("&signType=").append("v3");
            params.append("&curtime=").append(curtime);
            params.append("&strict=").append(true);

            StringEntity entity = new StringEntity(params.toString(), StandardCharsets.UTF_8);
            post.setEntity(entity);

            String response = EntityUtils.toString(client.execute(post).getEntity(), StandardCharsets.UTF_8);

            return youdaoParseResponse(response);
        } catch (Exception e) {
            System.err.println("有道翻译失败：" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // 解析响应json
    private static String youdaoParseResponse(String jsonResponse) {
        try {
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

            String errorCode = json.get("errorCode").getAsString();
            if (!"0".equals(errorCode)) {
                System.err.println("有道翻译返回错误码：" + errorCode);
                return null;
            }

            if (json.has("translation")) {
                return json.getAsJsonArray("translation").get(0).getAsString();
            }
        } catch (Exception e) {
            System.err.println("解析翻译结果失败：" + e.getMessage());
        }
        return null;
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }
}
