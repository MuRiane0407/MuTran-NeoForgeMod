package com.muriane.mutran.translate;

import com.muriane.mutran.MusTranslate;
import com.muriane.mutran.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatTranslate {
    @EventBusSubscriber(modid = MusTranslate.MODID, value = Dist.CLIENT)
    public static class ChatTranslateHolder{
        @SubscribeEvent
        public static void ReceiveChat(ClientChatReceivedEvent event){
            if (Config.COMMON.ENABLE_TRANSLATION.get()){
                if (event.isSystem()){
                    return;
                }else{
                    if (Minecraft.getInstance().level != null){
                        // 如果启用了聊天替换，则直接不显示原文
                        if (Config.COMMON.CHAT_REPLACEMENT_MODE.get()){
                            event.setCanceled(true);
                        }

                        List<Component> components = event.getMessage().toFlatList();
                        String chat = components.getLast().getString();
                        Style chat_style = components.getLast().getStyle();
                        translateAsync(chat,
                                result -> {
                                if (Minecraft.getInstance().player != null) {
                                    if (result != null){
                                        MutableComponent component = Component.literal("");
                                        for (int index = 0 ; index < components.size()-1 ; index++) component.append(components.get(index));
                                        component.append(Component.literal(result).setStyle(chat_style));

                                        Minecraft.getInstance().player.sendSystemMessage(component);
                                    }else{
                                        Minecraft.getInstance().player.sendSystemMessage(Component.literal(Config.COMMON.TRANSLATION_PROVIDER.get().getDisplayName() + Component.translatable("mutran.error.cant_translate").getString()).withColor(0xFB5454));
                                    }

                                }
                            }
                        );
                    }
                }
            }else{
                return;
            }
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
        TRANSLATION_POOL.submit(() -> {
            String result = Translator.translate(text, "auto", "zh-CHS");
            // 回到主线程执行回调
            Minecraft.getInstance().execute(() -> callback.onComplete(result));
        });
    }

    // 回调接口
    public interface TranslationCallback {
        void onComplete(String result);
    }
}
