package com.muriane.mutran.screen;

import com.muriane.mutran.MusTranslate;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import org.lwjgl.glfw.GLFW;

public class MuTranSetting {
    private static ModContainer modContainer = null;
    private static boolean needGrab = false;

    public static void setModContainer(ModContainer mc){
        modContainer = mc;
    }

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
                        Minecraft.getInstance().setScreen(new ConfigurationScreen(modContainer, null));
                    }
                }
            }
        }

        // 解决直接回到游戏画面失焦的问题
        @SubscribeEvent
        private static void onClose(ScreenEvent.Closing event){
            if (event.getScreen() instanceof ConfigurationScreen){
                needGrab = true;
            }
        }

        @SubscribeEvent
        private static void onClientTick(ClientTickEvent.Post event){
            Minecraft minecraft = Minecraft.getInstance();
            if (needGrab && minecraft.screen == null){
                minecraft.mouseHandler.grabMouse();
                if (minecraft.mouseHandler.isMouseGrabbed()){
                    needGrab = false;
                }
            }
        }

        @SubscribeEvent
        private static void registerKeys(RegisterKeyMappingsEvent event){
            event.register(GUI_KEY);
        }
    }
}
