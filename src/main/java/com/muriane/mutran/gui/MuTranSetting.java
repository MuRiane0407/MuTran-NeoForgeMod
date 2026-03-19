package com.muriane.mutran.gui;

import com.muriane.mutran.MusTranslate;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import org.lwjgl.glfw.GLFW;

public class MuTranSetting {
    private static ModContainer modContainer = null;

    public static void setModContainer(ModContainer mc){
        modContainer = mc;
    }

    // 按键相关
    @EventBusSubscriber(modid = MusTranslate.MODID, value = Dist.CLIENT)
    public static class KeyHolder{
        private static final KeyMapping GUI_KEY = new KeyMapping(
                "mutran.key.open_gui",
                GLFW.GLFW_KEY_UNKNOWN,
                "mutran.key.category"
        );

        @SubscribeEvent
        private static void inputKey(InputEvent.Key event){
            if (event.getKey() == GUI_KEY.getKey().getValue() && event.getAction() == 1){
                // 没打开背包的时候为null
                if (Minecraft.getInstance().screen == null) {
                    if (modContainer != null){
                        Minecraft.getInstance().setScreen(new ConfigurationScreen(modContainer, Minecraft.getInstance().screen));
                    }
                }
            }
        }

        @SubscribeEvent
        private static void registerKeys(RegisterKeyMappingsEvent event){
            event.register(GUI_KEY);
        }
    }
}
