package com.muriane.mutran.translate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.muriane.mutran.MusTranslate;
import com.muriane.mutran.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import oshi.util.tuples.Pair;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.muriane.mutran.translate.ChatTranslate.ChatTranslateHolder.sendMessageWithSign;

public class Translator {
    @EventBusSubscriber
    public static class translationHolder{ // 用于限制并发速度防止报错
        private static final Pair<List<String>, List<TranslationCallback>> translationRequests = new Pair<>(new ArrayList<>(), new ArrayList<>());
        private static int minRequestWaitTime = 1000;
        private static long lastRequestTime = 0;

        @SubscribeEvent
        public static void onTick(ClientTickEvent.Pre event){
            if (minRequestWaitTime != Config.COMMON.TRANSLATION_WAIT_TIME.getAsInt()){
                minRequestWaitTime = Config.COMMON.TRANSLATION_WAIT_TIME.getAsInt();
            }

            if (!translationRequests.getA().isEmpty() && System.currentTimeMillis() - lastRequestTime >= minRequestWaitTime){
                if (Config.COMMON.ENABLE_MERGE_TRANSLATION.get()){
                    MusTranslate.LOGGER.info("MergeTranslateAsync");
                    lastRequestTime = System.currentTimeMillis();
                    List<String> stringList = new ArrayList<>(translationRequests.getA());
                    List<TranslationCallback> callbackList = new ArrayList<>(translationRequests.getB());
                    translationRequests.getA().clear();
                    translationRequests.getB().clear();

                    TRANSLATION_POOL.submit(() -> {
                        Pair<String, List<Integer>> pair = mergeString(stringList); // 合并并获取每个String有多少句

                        String result = Translator.translate(pair.getA()); // 翻译

                        if (result != null){
                            List<String> splitStringList = splitString(result, pair.getB()); // 根据merge时的数据拆分整个句子

                            for (int index = 0; index < splitStringList.size() ; index++){
                                int finalIndex = index;
                                TranslationCallback callback = callbackList.get(finalIndex);
                                Minecraft.getInstance().execute(() -> callback.onComplete(splitStringList.get(finalIndex)));
                            }
                        }else{
                            if (Minecraft.getInstance().player != null) {
                                sendMessageWithSign(Minecraft.getInstance().player, Component.literal(Config.COMMON.TRANSLATION_PROVIDER.get().getDisplayName() + Component.translatable("mutran.error.cant_translate").getString()).withColor(0xFB5454));
                            }
                        }
                    });
                }else{
                    MusTranslate.LOGGER.info("TranslateAsync");
                    lastRequestTime = System.currentTimeMillis();

                    TRANSLATION_POOL.submit(() -> {
                        String string = translationRequests.getA().getFirst();
                        translationRequests.getA().removeFirst();
                        String result = Translator.translate(string);

                        TranslationCallback callback = translationRequests.getB().getFirst();
                        translationRequests.getB().removeFirst();
                        Minecraft.getInstance().execute(() -> callback.onComplete(result));
                    });
                }
            }
        }

        public static List<String> splitString(String string, List<Integer> list){
            List<String> splitList = List.of(string.split("\n"));
            List<String> stringList = new ArrayList<>();
            int index = 0;
            for (int count : list){
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0 ; i < count ; i++, index++){
                    stringBuilder.append(splitList.get(index));
                    if (i != count-1){
                        stringBuilder.append("\n");
                    }
                }
                stringList.add(new String(stringBuilder));
            }

            return stringList;
        }

        // 将n个String并为一条，提前计算好每一条String内有多少"\n"，再以"\n"为分界合并
        public static Pair<String, List<Integer>> mergeString(List<String> list){
            List<Integer> integerList = new ArrayList<>();
            StringBuilder new_string = new StringBuilder();
            for (String string : list){
                if (string != null){
                    String[] strings = string.split("\n");
                    integerList.add(strings.length);

                    new_string.append(string).append("\n");
                }else{
                    integerList.add(0);
                }
            }

            return new Pair<>(new_string.toString(), integerList);
        }

        public static void addRequest(String text, TranslationCallback callback){
            translationHolder.translationRequests.getA().add(text);
            translationHolder.translationRequests.getB().add(callback);
        }
    }

    // 创建一个线程池处理翻译请求
    private static final ExecutorService TRANSLATION_POOL = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "Translation Thread");
        t.setDaemon(true);
        return t;
    });

    // 异步翻译文本
    public static void translateAsync(String text, TranslationCallback callback) {
        MusTranslate.LOGGER.info("RequestTranslate");
        translationHolder.addRequest(text, callback);
    }

    // 回调接口
    public interface TranslationCallback {
        void onComplete(String result);
    }

    public static String translate(String query){
        String result = null;
        if (Config.COMMON.TRANSLATION_PROVIDER.get() == Config.TranslationProvider.Youdao){
            result = YoudaoTranslation.youdaoTranslate(query);
        }else if (Config.COMMON.TRANSLATION_PROVIDER.get() == Config.TranslationProvider.Baidu){
            result = BaiduTranslation.baiduTranslate(query);
        }else{
            MusTranslate.LOGGER.error("Translation provider not find");
        }
        return result;
    }

    public static class YoudaoTranslation {
        // 尝试翻译
        public static String youdaoTranslate(String query) {
            String app_id = Config.COMMON.APP_ID.get();
            String app_secret = Config.COMMON.APP_SECRET.get();

            String from = Config.COMMON.FROM_LANGUAGE.get().getYoudao();
            String to = Config.COMMON.TO_LANGUAGE.get().getYoudao();
            if (Config.COMMON.AUTO_DETECT_LANGUAGE.get()) {
                from = "auto";
            }

            String salt = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            String curtime = String.valueOf(System.currentTimeMillis() / 1000);

            String input = query.length() <= 20 ? query : (query.substring(0, 10) + query.length() + query.substring(query.length() - 10));
            String signStr = app_id + input + salt + curtime + app_secret; // 签名生成方法如下： signType=v3； sign=sha256(应用ID+input+salt+curtime+应用密钥)； 其中，input的计算方式为：input=q前10个字符 + q长度 + q后10个字符（当q长度大于20）或 input=q字符串（当q长度小于等于20）；
            String sign = sha256(signStr);

            try (HttpClient client = HttpClient.newHttpClient()) {
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

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(Config.COMMON.TRANSLATION_PROVIDER.get().getApiUrl()))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(params.toString()))
                        .timeout(java.time.Duration.ofSeconds(10))
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                return youdaoParseResponse(response.body());
            } catch (Exception e) {
                MusTranslate.LOGGER.error("Youdao Translation fail: {}", e.getMessage());
            }
            return null;
        }

        // 解析响应json
        private static String youdaoParseResponse(String jsonResponse) {
            try {
                JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

                String errorCode = json.get("errorCode").getAsString();
                if (!"0".equals(errorCode)) {
                    MusTranslate.LOGGER.error("Youdao Translation error code: {}", errorCode);
                    return null;
                }

                if (json.has("translation")) {
                    return json.getAsJsonArray("translation").get(0).getAsString();
                }
            } catch (Exception e) {
                MusTranslate.LOGGER.error("Youdao Translation parse fail: {}", e.getMessage());
            }
            return null;
        }
    }

    public static class BaiduTranslation {
        public static String baiduTranslate(String query) {
            String app_id = Config.COMMON.APP_ID.get();
            String app_secret = Config.COMMON.APP_SECRET.get();

            String from = Config.COMMON.FROM_LANGUAGE.get().getBaidu();
            String to = Config.COMMON.TO_LANGUAGE.get().getBaidu();
            if (Config.COMMON.AUTO_DETECT_LANGUAGE.get()) {
                from = "auto";
            }

            String salt = UUID.randomUUID().toString().replace("-", "").substring(0, 10);

            String signStr = app_id + query + salt + app_secret; // appid+q+salt+密钥 的MD5值
            String sign = md5(signStr);

            try (HttpClient client = HttpClient.newHttpClient()) {
                StringBuilder params = new StringBuilder();
                params.append("q=").append(java.net.URLEncoder.encode(query, StandardCharsets.UTF_8));
                params.append("&from=").append(from);
                params.append("&to=").append(to);
                params.append("&appid=").append(app_id);
                params.append("&salt=").append(salt);
                params.append("&sign=").append(sign);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(Config.COMMON.TRANSLATION_PROVIDER.get().getApiUrl()))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(params.toString()))
                        .timeout(java.time.Duration.ofSeconds(10))
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                return baiduParseResponse(response.body());
            } catch (Exception e) {
                MusTranslate.LOGGER.error("Baidu Translation fail: {}", e.getMessage());
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
                    MusTranslate.LOGGER.error("Baidu Translation error code: [{}] {}", errorCode_str, errorMessage_str);
                    return null;
                }

                if (json.has("trans_result")) {
                    JsonObject object = (JsonObject) json.getAsJsonArray("trans_result").get(0);
                    return object.get("dst").getAsString();
                }
            } catch (Exception e) {
                MusTranslate.LOGGER.error("Baidu Translation parse fail: {}", e.getMessage());
            }
            return null;
        }
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
            throw new RuntimeException("SHA-256 Error: ", e);
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
            throw new RuntimeException("MD5 Error: ", e);
        }
    }
}
