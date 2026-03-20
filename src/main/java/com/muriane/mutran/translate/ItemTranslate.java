package com.muriane.mutran.translate;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.muriane.mutran.MusTranslate;
import com.muriane.mutran.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.ClientTooltipFlag;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
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

import static com.muriane.mutran.translate.ChatTranslate.translateAsync;

public class ItemTranslate {
    @EventBusSubscriber(modid = MusTranslate.MODID, value = Dist.CLIENT)
    public static class ItemTranslateHolder {
        @SubscribeEvent
        private static void onItemTooltip(ItemTooltipEvent event){
            if (Config.COMMON.ENABLE_TRANSLATION_MAIN.get() && Config.COMMON.ENABLE_TRANSLATION_ITEM.get()){
                ItemStack stack = event.getItemStack();
                List<Component> tooltips = event.getToolTip();
                ItemTranslationDataManager manager = ItemTranslationDataManager.getInstance();

                if (manager.containsKey(itemStackKey(stack))){
                    if (Config.COMMON.ITEM_TRANSLATION_DISPLAY_MODE.get() == Config.ItemTranslationDisplayMode.Extend){
                        tooltips.add(Component.empty());
                        tooltips.add(Component.translatable("mutran.item_translation.translation_info.extend").withColor(Color.WHITE.getRGB()).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE));
                        tooltips.addAll(manager.get(itemStackKey(stack)));
                    }else{
                        tooltips.clear();
                        tooltips.addAll(manager.get(itemStackKey(stack)));
                    }
                }
            }
        }

        private static final KeyMapping ITEM_TRANSLATE_KEY = new KeyMapping(
                "mutran.key.item_translate",
                GLFW.GLFW_KEY_UNKNOWN,
                "mutran.key.category"
        );

        @SubscribeEvent
        private static void inputKey(InputEvent.Key event){
            if (Config.COMMON.ENABLE_TRANSLATION_MAIN.get() && Config.COMMON.ENABLE_TRANSLATION_ITEM.get()) {
                if (event.getKey() == ITEM_TRANSLATE_KEY.getKey().getValue() && event.getAction() == 1) {
                    Screen screen = Minecraft.getInstance().screen;
                    if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                        Slot slot = containerScreen.getSlotUnderMouse();
                        if (slot != null) {
                            Level level = Minecraft.getInstance().level;
                            Player player = Minecraft.getInstance().player;
                            ItemStack itemStack = slot.getItem().copy();
                            ItemTranslationDataManager manager = ItemTranslationDataManager.getInstance();

                            boolean flag = player != null && player.isCreative() && slot.container == player.getInventory();
                            TooltipFlag.Default tooltipflag$default = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
                            TooltipFlag tooltipflag = flag ? tooltipflag$default.asCreative() : tooltipflag$default;

                            manager.remove(itemStackKey(itemStack));

                            List<Component> tooltips = slot.getItem().getTooltipLines(Item.TooltipContext.of(level), player, ClientTooltipFlag.of(tooltipflag)); // 这里创造模式物品栏的显示物品来源依旧不正常，之后再修复
                            StringBuilder str = new StringBuilder(); // 把需要翻译的工具提示都丢到一句内，减少请求量
                            for (Component tooltip : tooltips) {
                                str.append(tooltip.getString()).append("\n");
                            }
                            List<Component> new_tooltips = new ArrayList<>();
                            translateAsync(str.toString(),
                                    result -> {
                                        String[] strings = result.split("\n");
                                        for (int index = 0; index < strings.length; index++) {
                                            new_tooltips.add(Component.literal(strings[index]).withStyle(tooltips.get(index).getStyle()));
                                        }

                                        manager.put(itemStackKey(itemStack), new_tooltips);
                                    });
                        }
                    }
                }
            }
        }

        private static String itemStackKey(ItemStack stack){
            return stack.getItem() + Objects.requireNonNull(stack.getComponents().get(DataComponents.LORE)).toString() + Objects.requireNonNull(stack.getComponents().get(DataComponents.ENCHANTMENTS)).toString();
        }

        @SubscribeEvent
        private static void registerKeys(RegisterKeyMappingsEvent event){
            event.register(ITEM_TRANSLATE_KEY);
        }
    }

    public static class ItemTranslationDataManager {
        private static Map<String, List<Component>> translationCache = new HashMap<>();

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
//                    Type type = new TypeToken<Map<String, List<Component>>>(){}.getType();
//                    translationCache = GSON.fromJson(reader, type);
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
//                GSON.toJson(translationCache, writer);
            } catch (IOException e) {
                System.err.println("保存数据失败: " + e.getMessage());
            }
        }

        public boolean containsKey(String key) {
            return translationCache.containsKey(key);
        }

        public void put(String key, List<Component> entry) {
            translationCache.put(key, entry);
//            saveData(); // 立即保存
        }

        public void putAll(Map<String, List<Component>> map) {
            translationCache.putAll(map);
//            saveData();
        }

        public List<Component> get(String key) {
            return translationCache.get(key);
        }

        public void remove(String key) {
            translationCache.remove(key);
//            saveData();
        }

        public void clear() {
            translationCache.clear();
//            saveData();
        }
    }

//    public class ComponentTypeAdapter implements JsonSerializer<Component>, JsonDeserializer<Component> {
//        @Override
//        public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
//            // 将 Component 序列化为字符串
//            if (Minecraft.getInstance().level != null) {
//                return new JsonPrimitive(Component.Serializer.toJson(src, Minecraft.getInstance().level.registryAccess()));
//            }else{
//                return null;
//            }
//        }
//
//        @Override
//        public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//            // 从字符串反序列化为 Component
//            if (Minecraft.getInstance().level != null) {
//                return Component.Serializer.fromJson(json.getAsString(), Minecraft.getInstance().level.registryAccess());
//            }else{
//                return null;
//            }
//        }
//    }
}
