package com.muriane.mutran.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    public static final ModConfigSpec commonSpec;
    public static final Common COMMON;

    static {
        final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Common {
        // main
        public final ModConfigSpec.BooleanValue ENABLE_TRANSLATION_MAIN;
        public final ModConfigSpec.BooleanValue AUTO_DETECT_LANGUAGE;
        public final ModConfigSpec.EnumValue<TranslationLanguage> FROM_LANGUAGE;
        public final ModConfigSpec.EnumValue<TranslationLanguage> TO_LANGUAGE;
        public final ModConfigSpec.EnumValue<TranslationProvider> TRANSLATION_PROVIDER;
        public final ModConfigSpec.ConfigValue<String> APP_ID;
        public final ModConfigSpec.ConfigValue<String> APP_SECRET;
        // chat
        public final ModConfigSpec.BooleanValue ENABLE_TRANSLATION_CHAT;
        public final ModConfigSpec.EnumValue<ChatTranslationDisplayMode> CHAT_TRANSLATION_DISPLAY_MODE;
        // item
        public final ModConfigSpec.BooleanValue ENABLE_TRANSLATION_ITEM;
        public final ModConfigSpec.EnumValue<ItemTranslationDisplayMode> ITEM_TRANSLATION_DISPLAY_MODE;

        Common(ModConfigSpec.Builder builder){
            // main
            ENABLE_TRANSLATION_MAIN = builder
                    .comment("Whether translation is enabled")
                    .translation("mutran.configuration.enable_translation_main")
                    .define("enable_translation_main", false);
            AUTO_DETECT_LANGUAGE = builder
                    .comment("Whether to enable automatic language detection")
                    .translation("mutran.configuration.auto_detect_language")
                    .define("auto_detect_language", true);
            FROM_LANGUAGE = builder
                    .comment("What language to translate from")
                    .translation("mutran.configuration.from_language")
                    .defineEnum("from_language", TranslationLanguage.English);
            TO_LANGUAGE = builder
                    .comment("What language to translate into")
                    .translation("mutran.configuration.to_language")
                    .defineEnum("to_language", TranslationLanguage.Chinese);
            TRANSLATION_PROVIDER = builder
                    .comment("Choose your translation provider")
                    .translation("mutran.configuration.translation_provider")
                    .defineEnum("translation_provider", TranslationProvider.Youdao);
            APP_ID = builder
                    .comment("The app id your translation provider need")
                    .translation("mutran.configuration.app_id")
                    .define("app_id", "");
            APP_SECRET = builder
                    .comment("The app secret your translation provider need")
                    .translation("mutran.configuration.app_secret")
                    .define("app_secret", "");

            // chat
            ENABLE_TRANSLATION_CHAT = builder
                    .comment("Whether chat translation is enabled")
                    .translation("mutran.configuration.enable_translation_chat")
                    .define("enable_translation_chat", false);
            CHAT_TRANSLATION_DISPLAY_MODE = builder
                    .comment("How to display chat translation")
                    .translation("mutran.configuration.chat_translation_display_mode")
                    .defineEnum("chat_translation_display_mode", ChatTranslationDisplayMode.Extend);

            // item
            ENABLE_TRANSLATION_ITEM = builder
                    .comment("Whether item translation is enabled")
                    .translation("mutran.configuration.enable_translation_item")
                    .define("enable_translation_item", false);
            ITEM_TRANSLATION_DISPLAY_MODE = builder
                    .comment("How to display item translation")
                    .translation("mutran.configuration.item_translation_display_mode")
                    .defineEnum("item_translation_display_mode", ItemTranslationDisplayMode.Extend);
        }
    }

    public enum TranslationProvider{
        Youdao("mutran.configuration.translation_provider.enum.youdao", "https://openapi.youdao.com/api"),
        Baidu("mutran.configuration.translation_provider.enum.baidu", "https://fanyi-api.baidu.com/api/trans/vip/translate");

        private final String displayName;
        private final String apiUrl;

        TranslationProvider(String displayName, String apiUrl){
            this.displayName = displayName;
            this.apiUrl = apiUrl;
        }

        public String getDisplayName() {
            return Component.translatable(displayName).getString();
        }

        public String getApiUrl() {
            return apiUrl;
        }
    }

    public enum TranslationLanguage{
        English("en", "en"),
        Chinese("zh-CHS", "zh"),
        Chinese_Traditional("zh-CHT", "cht"),
        Japanese("ja", "jp"),
        French("fr", "fra"),
        German("de", "de"),
        Russian("ru", "ru"),
        Spanish("es", "spa"),
        Arabic("ar", "ara");

        private final String youdao;
        private final String baidu;

        TranslationLanguage(String youdao, String baidu){
            this.youdao = youdao;
            this.baidu = baidu;
        }

        public String getYoudao(){
            return youdao;
        }

        public String getBaidu() {
            return baidu;
        }
    }

    public enum ChatTranslationDisplayMode {
        Extend(0),
        Replace(1);

        private final int mode;

        ChatTranslationDisplayMode(int mode) {
            this.mode = mode;
        }

        public int getMode() {
            return mode;
        }
    }

    public enum ItemTranslationDisplayMode {
        Extend(0),
        Replace(1);

        private final int mode;

        ItemTranslationDisplayMode(int mode) {
            this.mode = mode;
        }

        public int getMode() {
            return mode;
        }
    }

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
