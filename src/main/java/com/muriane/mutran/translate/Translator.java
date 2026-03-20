package com.muriane.mutran.translate;

import com.google.gson.JsonElement;
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
import java.util.Objects;
import java.util.UUID;

public class Translator {
    public static String translate(String query){
        String result;
        if (Config.COMMON.TRANSLATION_PROVIDER.get() == Config.TranslationProvider.Youdao){
            result = youdaoTranslate(query);
        }else if (Config.COMMON.TRANSLATION_PROVIDER.get() == Config.TranslationProvider.Baidu){
            result = baiduTranslate(query);
        }else{
            return "Error translation provider not find";
        }

        return result;
    }

    // 尝试翻译
    public static String youdaoTranslate(String query){
        String app_id = Config.COMMON.APP_ID.get();
        String app_secret = Config.COMMON.APP_SECRET.get();

        String from = Config.COMMON.FROM_LANGUAGE.get().getYoudao();
        String to = Config.COMMON.TO_LANGUAGE.get().getYoudao();
        if (Config.COMMON.AUTO_DETECT_LANGUAGE.get()){
            from = "auto";
        }

        String salt = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String curtime = String.valueOf(System.currentTimeMillis() / 1000);

        String input = query.length() <= 20 ? query : (query.substring(0, 10) + query.length() + query.substring(query.length()-10));
        String signStr = app_id + input + salt + curtime + app_secret; // 签名生成方法如下： signType=v3； sign=sha256(应用ID+input+salt+curtime+应用密钥)； 其中，input的计算方式为：input=q前10个字符 + q长度 + q后10个字符（当q长度大于20）或 input=q字符串（当q长度小于等于20）；
        String sign = sha256(signStr);

        try(CloseableHttpClient client = HttpClients.createDefault()){
            HttpPost post = new HttpPost(Config.COMMON.TRANSLATION_PROVIDER.get().getApiUrl());

            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            StringBuilder params = new StringBuilder();
            params.append("q=").append(java.net.URLEncoder.encode(query, StandardCharsets.UTF_8));
            params.append("&from=").append(from);
            params.append("&to=").append(to);
            params.append("&appKey=").append(app_id);
            params.append("&salt=").append(salt);
            params.append("&sign=").append(sign);
            params.append("&signType=").append("v3");
            params.append("&curtime=").append(curtime);
            params.append("&strict=").append(true);
            params.append("&domain=").append("game");

            StringEntity entity = new StringEntity(params.toString(), StandardCharsets.UTF_8);
            post.setEntity(entity);

            String response = EntityUtils.toString(client.execute(post).getEntity(), StandardCharsets.UTF_8);

            return youdaoParseResponse(response);
        } catch (Exception e) {
            System.err.println("有道翻译失败: " + e.getMessage());
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
                System.err.println("有道翻译返回错误码: " + errorCode);
                return null;
            }

            if (json.has("translation")) {
                return json.getAsJsonArray("translation").get(0).getAsString();
            }
        } catch (Exception e) {
            System.err.println("解析翻译结果失败: " + e.getMessage());
        }
        return null;
    }

    public static String baiduTranslate(String query){
        String app_id = Config.COMMON.APP_ID.get();
        String app_secret = Config.COMMON.APP_SECRET.get();

        String from = Config.COMMON.FROM_LANGUAGE.get().getBaidu();
        String to = Config.COMMON.TO_LANGUAGE.get().getBaidu();
        if (Config.COMMON.AUTO_DETECT_LANGUAGE.get()){
            from = "auto";
        }

        String salt = UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        String signStr = app_id + query + salt + app_secret; // appid+q+salt+密钥 的MD5值
        String sign = md5(signStr);

        try(CloseableHttpClient client = HttpClients.createDefault()){
            HttpPost post = new HttpPost(Config.COMMON.TRANSLATION_PROVIDER.get().getApiUrl());

            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            StringBuilder params = new StringBuilder();
            params.append("q=").append(java.net.URLEncoder.encode(query, StandardCharsets.UTF_8));
            params.append("&from=").append(from);
            params.append("&to=").append(to);
            params.append("&appid=").append(app_id);
            params.append("&salt=").append(salt);
            params.append("&sign=").append(sign);

            StringEntity entity = new StringEntity(params.toString(), StandardCharsets.UTF_8);
            post.setEntity(entity);

            String response = EntityUtils.toString(client.execute(post).getEntity(), StandardCharsets.UTF_8);

            return baiduParseResponse(response);
        } catch (Exception e) {
            System.err.println("百度翻译失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static String baiduParseResponse(String jsonResponse) {
        try {
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

            JsonElement errorCode = json.get("error_code");
            if (errorCode != null) {
                String errorCode_str = errorCode.getAsString();
                String errorMessage_str = json.get("error_msg").getAsString();
                System.err.println("百度翻译返回错误码: [" + errorCode_str + "] " + errorMessage_str);
                return null;
            }

            if (json.has("trans_result")) {
                JsonObject object = (JsonObject) json.getAsJsonArray("trans_result").get(0);
                return object.get("dst").getAsString();
            }
        } catch (Exception e) {
            System.err.println("解析翻译结果失败: " + e.getMessage());
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
            throw new RuntimeException("SHA-256算法不可用: ", e);
        }
    }

    public static String md5(final String input) {
        Objects.requireNonNull(input, "input");
        try {
            final byte[] inputBytes = input.getBytes();
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            final byte[] bytes = digest.digest(inputBytes);
            final StringBuilder md5 = new StringBuilder(bytes.length * 2);
            for (final byte b : bytes) {
                md5.append(Character.forDigit((0xFF & b) >> 4, 16));
                md5.append(Character.forDigit(0x0F & b, 16));
            }
            return md5.toString();
        }
        catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用: ", e);
        }
    }
}
