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

import java.awt.*;
import java.util.List;

import static com.muriane.mutran.translate.Translator.translateAsync;

public class ChatTranslate {
    @EventBusSubscriber(modid = MusTranslate.MODID, value = Dist.CLIENT)
    public static class ChatTranslateHolder{
        @SubscribeEvent
        public static void ReceiveChat(ClientChatReceivedEvent event){
            if (Config.COMMON.ENABLE_TRANSLATION_MAIN.get() && Config.COMMON.ENABLE_TRANSLATION_CHAT.get()){
                if (event.isSystem()){
                    if (Config.COMMON.TRANSLATE_SYSTEM_MESSAGE.get()){
                        if (Config.COMMON.CHAT_TRANSLATION_DISPLAY_MODE.get() == Config.ChatTranslationDisplayMode.Replace || Config.COMMON.CHAT_TRANSLATION_DISPLAY_MODE.get() == Config.ChatTranslationDisplayMode.Expand){
                            event.setCanceled(true);
                        }

                        List<Component> components = event.getMessage().toFlatList();
                        if (Config.COMMON.TRANSLATE_WITH_STYLE.get()){
                            StringBuilder stringBuilder = new StringBuilder();
                            for (Component component : components){
                                stringBuilder.append(component.getString()).append("\n");
                            }

                            translateAsync(new String(stringBuilder),
                                    result -> {
                                        if (Minecraft.getInstance().player != null) {
                                            if (result != null){
                                                MutableComponent new_chat = Component.literal("");
                                                String[] strings = result.split("\n");
                                                for (int index = 0 ; index < strings.length ; index++){
                                                    new_chat.append(Component.literal(strings[index]).setStyle(components.get(Math.min(index, components.size()-1)).getStyle()));
                                                }

                                                MutableComponent component = Component.literal("");
                                                if (Config.COMMON.CHAT_TRANSLATION_DISPLAY_MODE.get() == Config.ChatTranslationDisplayMode.Follow){
                                                    component.append(Component.translatable("mutran.translation.translation_info.expand").withColor(Color.GRAY.getRGB()));
                                                    component.append(" ");
                                                    component.append(new_chat);
                                                }else if (Config.COMMON.CHAT_TRANSLATION_DISPLAY_MODE.get() == Config.ChatTranslationDisplayMode.Replace) {
                                                    component.append(new_chat);
                                                }else if (Config.COMMON.CHAT_TRANSLATION_DISPLAY_MODE.get() == Config.ChatTranslationDisplayMode.Expand){
                                                    component.append(event.getMessage());
                                                    component.append(" (");
                                                    component.append(new_chat);
                                                    component.append(")");
                                                }

                                                Minecraft.getInstance().player.sendSystemMessage(component);
                                            }else{
                                                Minecraft.getInstance().player.sendSystemMessage(Component.literal(Config.COMMON.TRANSLATION_PROVIDER.get().getDisplayName() + Component.translatable("mutran.error.cant_translate").getString()).withColor(0xFB5454));
                                            }
                                        }
                                    });
                        }else{
                            StringBuilder stringBuilder = new StringBuilder();
                            for (Component component : components){
                                stringBuilder.append(component.getString());
                            }

                            translateAsync(new String(stringBuilder),
                                    result -> {
                                        if (Minecraft.getInstance().player != null) {
                                            if (result != null){
                                                MutableComponent component = Component.literal("");
                                                if (Config.COMMON.CHAT_TRANSLATION_DISPLAY_MODE.get() == Config.ChatTranslationDisplayMode.Follow){
                                                    component.append(Component.translatable("mutran.translation.translation_info.expand").withColor(Color.GRAY.getRGB()));
                                                    component.append(" ");
                                                    component.append(result);
                                                }else if (Config.COMMON.CHAT_TRANSLATION_DISPLAY_MODE.get() == Config.ChatTranslationDisplayMode.Replace) {
                                                    component.append(result);
                                                }else if (Config.COMMON.CHAT_TRANSLATION_DISPLAY_MODE.get() == Config.ChatTranslationDisplayMode.Expand){
                                                    component.append(event.getMessage());
                                                    component.append(" (");
                                                    component.append(result);
                                                    component.append(")");
                                                }

                                                Minecraft.getInstance().player.sendSystemMessage(component);
                                            }else{
                                                Minecraft.getInstance().player.sendSystemMessage(Component.literal(Config.COMMON.TRANSLATION_PROVIDER.get().getDisplayName() + Component.translatable("mutran.error.cant_translate").getString()).withColor(0xFB5454));
                                            }
                                        }
                                    });
                        }
                    }
                }else{
                    if (Minecraft.getInstance().level != null){
                        if (Config.COMMON.CHAT_TRANSLATION_DISPLAY_MODE.get() == Config.ChatTranslationDisplayMode.Replace || Config.COMMON.CHAT_TRANSLATION_DISPLAY_MODE.get() == Config.ChatTranslationDisplayMode.Expand){
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
                                            if (Config.COMMON.CHAT_TRANSLATION_DISPLAY_MODE.get() == Config.ChatTranslationDisplayMode.Follow){
                                                component.append(Component.translatable("mutran.translation.translation_info.expand").withColor(Color.GRAY.getRGB()));
                                                component.append(" ");
                                                for (int index = 0 ; index < components.size()-1 ; index++) component.append(components.get(index));
                                                component.append(Component.literal(result).setStyle(chat_style));
                                            }else if (Config.COMMON.CHAT_TRANSLATION_DISPLAY_MODE.get() == Config.ChatTranslationDisplayMode.Replace) {
                                                for (int index = 0 ; index < components.size()-1 ; index++) component.append(components.get(index));
                                                component.append(Component.literal(result).setStyle(chat_style));
                                            }else if (Config.COMMON.CHAT_TRANSLATION_DISPLAY_MODE.get() == Config.ChatTranslationDisplayMode.Expand){
                                                component.append(event.getMessage());
                                                component.append(" (");
                                                component.append(Component.literal(result).setStyle(chat_style));
                                                component.append(")");
                                            }

                                            Minecraft.getInstance().player.sendSystemMessage(component);
                                        }else{
                                            Minecraft.getInstance().player.sendSystemMessage(Component.literal(Config.COMMON.TRANSLATION_PROVIDER.get().getDisplayName() + Component.translatable("mutran.error.cant_translate").getString()).withColor(0xFB5454));
                                        }
                                    }
                        });
                    }
                }
            }
        }
    }
}
