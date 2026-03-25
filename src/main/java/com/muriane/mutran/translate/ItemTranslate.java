package com.muriane.mutran.translate;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.muriane.mutran.MusTranslate;
import com.muriane.mutran.config.Config;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.ClientTooltipFlag;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

import static com.muriane.mutran.MusTranslate.KEY_CATEGORY;
import static com.muriane.mutran.translate.Translator.translateAsync;

public class ItemTranslate {
    @EventBusSubscriber(modid = MusTranslate.MODID, value = Dist.CLIENT)
    public static class ItemTranslateHolder {
        private static final Map<String, Integer> translation_progress = new HashMap<>(); // 0=提取tooltips，不添加额外提示tooltip 1=正在翻译 null=未翻译/已翻译

        // 即时重载缓存
        @SubscribeEvent
        private static void onConfigChange(ModConfigEvent.Reloading event){
            ItemTranslationDataManager manager = ItemTranslationDataManager.getInstance();
            manager.clearCache();
            manager.loadData();
        }

        @SubscribeEvent
        private static void onInventoryClose(ScreenEvent.Closing event){
            ItemTranslationDataManager manager = ItemTranslationDataManager.getInstance();
            if (Config.COMMON.ITEM_TRANSLATION_SAVE_MODE.get() == Config.ItemTranslationSaveMode.Inventory && event.getScreen() instanceof AbstractContainerScreen<?>){
                manager.clear();
            }
        }

        @SubscribeEvent
        private static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event){
            ItemTranslationDataManager manager = ItemTranslationDataManager.getInstance();
            if (Config.COMMON.ITEM_TRANSLATION_SAVE_MODE.get() == Config.ItemTranslationSaveMode.Server || Config.COMMON.ITEM_TRANSLATION_SAVE_MODE.get() == Config.ItemTranslationSaveMode.Inventory){
                manager.clear();
            }
        }

        @SubscribeEvent
        private static void onItemTooltip(ItemTooltipEvent event){
            if (Config.COMMON.ENABLE_TRANSLATION_MAIN.get() && Config.COMMON.ENABLE_TRANSLATION_ITEM.get()){
                ItemStack stack = event.getItemStack();
                List<Component> tooltips = event.getToolTip();
                ItemTranslationDataManager manager = ItemTranslationDataManager.getInstance();

                if (Config.COMMON.AUTO_TRANSLATE_ITEM.get() && !manager.containsKey(itemStackKey(stack))){
                    // 由于没有找到区分自动加载和鼠标指向的方法，暂时无效
                }

                if (manager.containsKey(itemStackKey(stack))){
                    List<Component> new_tooltips = new ArrayList<>();
                    List<String> new_tooltips_str = manager.get(itemStackKey(stack)); // 用String是因为方便存储，若能解析Component就换用Component

                    if (new_tooltips_str != null){
                        if (!Objects.equals(new_tooltips_str.getFirst(), Component.translatable("mutran.error.info").getString())){
                            for (int index = 0 ; index < new_tooltips_str.size() ; index++){
                                new_tooltips.add(Component.literal(new_tooltips_str.get(index)).withStyle(tooltips.get(Math.min(index, tooltips.size()-1)).getStyle()));
                            }
                        }else{
                            for (String string : new_tooltips_str) {
                                new_tooltips.add(Component.literal(string).withColor(0xFB5454));
                            }
                        }
                    }

                    if (Config.COMMON.ITEM_TRANSLATION_DISPLAY_MODE.get() == Config.ItemTranslationDisplayMode.Follow){
                        tooltips.add(Component.empty());
                        tooltips.add(Component.translatable("mutran.translation.translation_info.expand").withColor(Color.GRAY.getRGB()));
                        tooltips.addAll(new_tooltips);
                        tooltips.add(Component.translatable("mutran.translation.translation_info.redo_key", TRANSLATE_KEY.getTranslatedKeyMessage()).withColor(Color.GRAY.getRGB()));
                    }else{
                        tooltips.clear();
                        tooltips.addAll(new_tooltips);
                        tooltips.add(Component.translatable("mutran.translation.translation_info.redo_key", TRANSLATE_KEY.getTranslatedKeyMessage()).withColor(Color.GRAY.getRGB()));
                    }
                }else if (!translation_progress.containsKey(itemStackKey(stack))){
                    tooltips.add(Component.translatable("mutran.translation.translation_info.translate", TRANSLATE_KEY.getTranslatedKeyMessage()).withColor(Color.GRAY.getRGB()));
                }else if (translation_progress.get(itemStackKey(stack)) == 1){
                    tooltips.add(Component.translatable("mutran.translation.translation_info.translating").withColor(Color.GRAY.getRGB()));
                }
            }
        }

        public static final KeyMapping TRANSLATE_KEY = new KeyMapping(
                "mutran.key.translate",
                GLFW.GLFW_KEY_UNKNOWN,
                KEY_CATEGORY
        );

        @SubscribeEvent
        private static void inputKey(InputEvent.Key event){
            if (Config.COMMON.ENABLE_TRANSLATION_MAIN.get() && Config.COMMON.ENABLE_TRANSLATION_ITEM.get()) {
                if (event.getKey() == TRANSLATE_KEY.getKey().getValue() && event.getAction() == 1) {
                    translateItem();
                }
            }
        }

        public static void translateItem(){
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                Slot slot = containerScreen.getSlotUnderMouse();
                if (slot != null && !slot.getItem().isEmpty()) {
                    Level level = Minecraft.getInstance().level;
                    Player player = Minecraft.getInstance().player;
                    ItemStack itemStack = slot.getItem().copy();
                    ItemTranslationDataManager manager = ItemTranslationDataManager.getInstance();

                    boolean flag = player != null && player.isCreative() && slot.container == player.getInventory();
                    TooltipFlag.Default tooltipflag$default = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
                    TooltipFlag tooltipflag = flag ? tooltipflag$default.asCreative() : tooltipflag$default;

                    translation_progress.put(itemStackKey(itemStack), 0);
                    manager.remove(itemStackKey(itemStack));

                    List<Component> tooltips = slot.getItem().getTooltipLines(Item.TooltipContext.of(level), player, ClientTooltipFlag.of(tooltipflag)); // 这里创造模式物品栏的显示物品来源依旧不正常，之后再修复
                    translation_progress.put(itemStackKey(itemStack), 1);
                    StringBuilder str = new StringBuilder(); // 把需要翻译的工具提示都丢到一句内，减少请求量
                    for (Component tooltip : tooltips) {
                        str.append(tooltip.getString()).append("\n");
                    }
                    List<String> new_tooltips = new ArrayList<>();
                    translateAsync(str.toString(),
                            result -> {
                                if (result != null){
                                    String[] strings = result.split("\n");
                                    new_tooltips.addAll(Arrays.asList(strings));

                                    manager.put(itemStackKey(itemStack), new_tooltips);
                                }else{
                                    List<String> error_tooltips = new ArrayList<>();
                                    error_tooltips.add(Component.translatable("mutran.error.info").getString());
                                    error_tooltips.add(Component.literal(Config.COMMON.TRANSLATION_PROVIDER.get().getDisplayName() + Component.translatable("mutran.error.cant_translate").getString()).getString());
                                    manager.put(itemStackKey(itemStack), error_tooltips);
                                }
                                translation_progress.remove(itemStackKey(itemStack));
                            });
                }
            }
        }

        private static String itemStackKey(ItemStack stack){
            return stack.getItem() + Objects.requireNonNull(stack.getComponents().get(DataComponents.LORE)).toString() + Objects.requireNonNull(stack.getComponents().get(DataComponents.ENCHANTMENTS));
        }

        @SubscribeEvent
        private static void registerKeys(RegisterKeyMappingsEvent event){
            event.register(TRANSLATE_KEY);
        }
    }

    public static class ItemTranslationDataManager {
        private static Map<String, List<String>> translationCache = new HashMap<>();

        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        private static final Path DATA_PATH = FMLPaths.CONFIGDIR.get().resolve("mutran-item_translation.json");

        private static ItemTranslationDataManager instance;

        private ItemTranslationDataManager(){
            loadData();
        }

        public static ItemTranslationDataManager getInstance() {
            if (instance == null) {
                instance = new ItemTranslationDataManager();
            }
            return instance;
        }

        public void loadData() {
            if (Files.exists(DATA_PATH)) {
                try (Reader reader = Files.newBufferedReader(DATA_PATH)) {
                    if (Config.COMMON.ITEM_TRANSLATION_SAVE_MODE.get() == Config.ItemTranslationSaveMode.Permanent){
                        Type type = new TypeToken<Map<String, List<String>>>(){}.getType();
                        translationCache = GSON.fromJson(reader, type);
                    }
                    if (translationCache == null) {
                        translationCache = new HashMap<>();
                    }
                } catch (IOException e) {
                    System.err.println("加载数据失败: " + e.getMessage());
                    translationCache = new HashMap<>();
                }
            }
        }

        public void saveData() {
            try (Writer writer = Files.newBufferedWriter(DATA_PATH)) {
                GSON.toJson(translationCache, writer);
            } catch (IOException e) {
                System.err.println("保存数据失败: " + e.getMessage());
            }
        }

        public boolean containsKey(String key) {
            return translationCache.containsKey(key);
        }

        public void put(String key, List<String> entry) {
            translationCache.put(key, entry);
            if (Config.COMMON.ITEM_TRANSLATION_SAVE_MODE.get() == Config.ItemTranslationSaveMode.Permanent){
                saveData();
            }
        }

        public void putAll(Map<String, List<String>> map) {
            translationCache.putAll(map);
            if (Config.COMMON.ITEM_TRANSLATION_SAVE_MODE.get() == Config.ItemTranslationSaveMode.Permanent){
                saveData();
            }
        }

        public List<String> get(String key) {
            return translationCache.get(key);
        }

        public void remove(String key) {
            translationCache.remove(key);
            if (Config.COMMON.ITEM_TRANSLATION_SAVE_MODE.get() == Config.ItemTranslationSaveMode.Permanent){
                saveData();
            }
        }

        public void clear() {
            translationCache.clear();
            if (Config.COMMON.ITEM_TRANSLATION_SAVE_MODE.get() == Config.ItemTranslationSaveMode.Permanent){
                saveData();
            }
        }

        public void clearCache(){
            translationCache.clear();
        }
    }
}
