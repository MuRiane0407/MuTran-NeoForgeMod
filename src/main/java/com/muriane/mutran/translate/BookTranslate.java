package com.muriane.mutran.translate;

import com.muriane.mutran.api.IBookViewScreen;
import com.muriane.mutran.config.Config;
import com.muriane.mutran.mixin.BookEditScreenMixinInterface;
import com.muriane.mutran.mixin.BookViewScreenMixinInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static com.muriane.mutran.translate.BookTranslate.BookTranslateHolder.translateBook;
import static com.muriane.mutran.translate.Translator.translateAsync;

public class BookTranslate {
    @EventBusSubscriber
    public static class BookTranslateHolder{
        @SubscribeEvent
        private static void onScreenInit(ScreenEvent.Init.Post event){
            if (Config.COMMON.ENABLE_TRANSLATION_MAIN.get() && Config.COMMON.ENABLE_TRANSLATION_BOOK.get()){
                Screen screen = event.getScreen();
                if (screen instanceof BookEditScreen || screen instanceof BookViewScreen){
                    BookTranslateButton button = new BookTranslateButton(screen.width/2 - 100, 160, 20, 20, screen, event);

                    event.addListener(button);
                }
            }
        }

        public static void translateBook(Screen screen){
            String text = "";
            if (screen instanceof BookEditScreen bookEditScreen){
                text = ((BookEditScreenMixinInterface) bookEditScreen).invokerGetCurrentPageText();
            }else if (screen instanceof BookViewScreen bookViewScreen){
                text = ((IBookViewScreen) bookViewScreen).getText$mutran().getString();
            }
            BookTranslateButton button = screen.renderables.stream().filter(renderable -> renderable instanceof BookTranslateButton).map(renderable -> (BookTranslateButton) renderable).findFirst().orElse(null);
            if (button != null) {
                button.addTranslation(screen, null);
            }

            translateAsync(text,
                    result -> {
                        if (Minecraft.getInstance().player != null) {
                            if (result != null){
                                if (button != null){
                                    button.addTranslation(screen, result);
                                }
                            }else{
                                Minecraft.getInstance().player.sendSystemMessage(Component.literal(Config.COMMON.TRANSLATION_PROVIDER.get().getDisplayName() + Component.translatable("mutran.error.cant_translate").getString()).withColor(0xFB5454));
                            }
                        }
            });
        }
    }

    public static class BookTranslateButton extends Button{
        private final Map<Integer, String> translation = new HashMap<>();

        protected BookTranslateButton(int x, int y, int width, int height, Screen screen, ScreenEvent.Init.Post event) {
            super(x, y, width, height, Component.translatable("mutran.button.translate"), btn -> {
                translateBook(screen);
            }, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            addTranslationString(guiGraphics);
        }

        public void addTranslationString(GuiGraphics guiGraphics){
            Minecraft minecraft = Minecraft.getInstance();
            Screen screen = Minecraft.getInstance().screen;

            if (screen instanceof BookEditScreen || screen instanceof BookViewScreen){
                int page = 0;
                if (screen instanceof BookEditScreen bookEditScreen){
                    page = ((BookEditScreenMixinInterface) bookEditScreen).getCurrentPage();
                }else if (screen instanceof BookViewScreen bookViewScreen){
                    page = ((BookViewScreenMixinInterface) bookViewScreen).getCurrentPage();
                }

                if (translation.containsKey(page)){
                    if (Config.COMMON.BOOK_TRANSLATION_DISPLAY_MODE.get() == Config.BookTranslationDisplayMode.Expand) {
                        boolean tooNarrow = (screen.width-192)/2 + 144 + 166 > screen.width;
                        guiGraphics.blit(BookViewScreen.BOOK_LOCATION, tooNarrow ? screen.width-166 : (screen.width-192)/2 + 144, 2, 0, 0, 192, 192);
                    }

                    int strX = (screen.width - 192) / 2 + 180;
                    guiGraphics.drawString(minecraft.font, Component.translatable("mutran.translation.translation_info.expand"), (screen.width-192)/2+180, 18, Color.GRAY.getRGB(), false);
                    if (translation.get(page) != null){
                        String[] strings = translation.get(page).split("\n");
                        for (int index = 0 ; index < strings.length ; index++){
                            int strY = 32 + index*9;
                            guiGraphics.drawString(minecraft.font, strings[index], strX, strY, 0, false);
                        }
                        guiGraphics.drawString(minecraft.font, Component.translatable("mutran.translation.translation_info.redo_button"), (screen.width-192)/2+180, 163, Color.GRAY.getRGB(), false);
                    }else{
                        guiGraphics.drawString(minecraft.font, Component.translatable("mutran.translation.translation_info.translating"), (screen.width-192)/2+180, 163, Color.GRAY.getRGB(), false);
                    }
                }
            }
        }

        public void addTranslation(Screen screen, String text){
            int page = 0;
            if (screen instanceof BookEditScreen bookEditScreen){
                page = ((BookEditScreenMixinInterface) bookEditScreen).getCurrentPage();
            }else if (screen instanceof BookViewScreen bookViewScreen){
                page = ((BookViewScreenMixinInterface) bookViewScreen).getCurrentPage();
            }
            translation.put(page, text);
        }
    }
}
