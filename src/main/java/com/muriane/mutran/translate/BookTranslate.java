package com.muriane.mutran.translate;

import com.muriane.mutran.api.IBookViewScreen;
import com.muriane.mutran.config.Config;
import com.muriane.mutran.mixin.BookEditScreenMixinInterface;
import com.muriane.mutran.mixin.BookViewScreenMixinInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
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
        private static void onScreenInit(ScreenEvent.Init.Pre event){
            if (Config.COMMON.ENABLE_TRANSLATION_MAIN.get() && Config.COMMON.ENABLE_TRANSLATION_BOOK.get()){
                Screen screen = event.getScreen();
                if (screen instanceof BookEditScreen || screen instanceof BookViewScreen){
                    BookTranslateButton button = new BookTranslateButton(screen.width/2 - 100, 160, 20, 20, screen, event);

                    event.addListener(button);
                }
            }
        }

        @SubscribeEvent
        private static void onTick(ClientTickEvent.Pre event){
            if (Config.COMMON.ENABLE_TRANSLATION_MAIN.get() && Config.COMMON.ENABLE_TRANSLATION_BOOK.get() && Config.COMMON.AUTO_TRANSLATE_BOOK.get()){
                Screen screen = Minecraft.getInstance().screen;
                if (screen instanceof BookEditScreen || screen instanceof BookViewScreen){
                    for (Renderable renderable : screen.renderables){
                        if (renderable instanceof BookTranslateButton button){
                            int page = 0;
                            if (screen instanceof BookEditScreen bookEditScreen){
                                page = ((BookEditScreenMixinInterface) bookEditScreen).getCurrentPage();
                            }else if (screen instanceof BookViewScreen bookViewScreen){
                                page = ((BookViewScreenMixinInterface) bookViewScreen).getCurrentPage();
                            }

                            if (!button.translation.containsKey(page)){
                                translateBook(screen);
                            }
                        }
                    }
                }
            }
        }

        public static void translateBook(Screen screen){
            String text = "";
            if (screen instanceof BookEditScreen bookEditScreen){
                text = ((BookEditScreenMixinInterface) bookEditScreen).getPages().get(((BookEditScreenMixinInterface) bookEditScreen).getCurrentPage());
            }else if (screen instanceof BookViewScreen bookViewScreen){
                FormattedText formattedText = ((IBookViewScreen) bookViewScreen).getText$mutran();
                if (formattedText == null) return;
                text = formattedText.getString();
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
                                if (button != null) {
                                    button.addTranslation(screen, Component.literal(Config.COMMON.TRANSLATION_PROVIDER.get().getDisplayName() + Component.translatable("mutran.error.cant_translate").getString()).withColor(0xFB5454).getString());
                                }
                            }
                        }
            });
        }
    }

    public static class BookTranslateButton extends Button.Plain {
        private final Map<Integer, String> translation = new HashMap<>();

        protected BookTranslateButton(int x, int y, int width, int height, Screen screen, ScreenEvent.Init.Pre event) {
            super(x, y, width, height, Component.translatable("mutran.button.translate"), btn -> {
                translateBook(screen);
            }, DEFAULT_NARRATION);
        }

        @Override
        protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderContents(guiGraphics, mouseX, mouseX, partialTick);
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
                        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BookViewScreen.BOOK_LOCATION, tooNarrow ? screen.width-166 : (screen.width - 192)/2 + 144, 2, 0, 0, 192, 192, 256, 256);
                    }

                    int strX = (screen.width - 192) / 2 + 180;
                    guiGraphics.drawString(minecraft.font, Component.translatable("mutran.translation.translation_info.expand"), (screen.width-192)/2+180, 18, Color.GRAY.getRGB(), false);
                    if (translation.get(page) != null){
                        String[] strings = translation.get(page).split("\n");
                        for (int index = 0 ; index < strings.length ; index++){
                            int strY = 32 + index*9;
                            guiGraphics.drawString(minecraft.font, strings[index], strX, strY, Color.BLACK.getRGB(), false);
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
