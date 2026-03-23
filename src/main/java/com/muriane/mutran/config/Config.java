package com.muriane.mutran.config;

import net.minecraft.network.chat.Component;
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
        public final ModConfigSpec.IntValue TRANSLATION_WAIT_TIME;
        public final ModConfigSpec.BooleanValue ENABLE_MERGE_TRANSLATION;
        // app
        public final ModConfigSpec.ConfigValue<String> APP_ID;
        public final ModConfigSpec.ConfigValue<String> APP_SECRET;
        // chat
        public final ModConfigSpec.BooleanValue ENABLE_TRANSLATION_CHAT;
        public final ModConfigSpec.BooleanValue TRANSLATE_SYSTEM_MESSAGE;
        public final ModConfigSpec.BooleanValue TRANSLATE_WITH_STYLE;
        public final ModConfigSpec.EnumValue<ChatTranslationDisplayMode> CHAT_TRANSLATION_DISPLAY_MODE;
        // item
        public final ModConfigSpec.BooleanValue ENABLE_TRANSLATION_ITEM;
        public final ModConfigSpec.BooleanValue AUTO_TRANSLATE_ITEM;
        public final ModConfigSpec.EnumValue<ItemTranslationDisplayMode> ITEM_TRANSLATION_DISPLAY_MODE;
        public final ModConfigSpec.EnumValue<ItemTranslationSaveMode> ITEM_TRANSLATION_SAVE_MODE;
        // book
        public final ModConfigSpec.BooleanValue ENABLE_TRANSLATION_BOOK;
        public final ModConfigSpec.BooleanValue AUTO_TRANSLATE_BOOK;
        public final ModConfigSpec.EnumValue<BookTranslationDisplayMode> BOOK_TRANSLATION_DISPLAY_MODE;
        // other
        public final ModConfigSpec.BooleanValue ENABLE_TRANSLATION_TITLE;
        public final ModConfigSpec.BooleanValue ENABLE_TRANSLATION_ACTIONBAR;
        public final ModConfigSpec.BooleanValue ENABLE_TRANSLATION_BOSSBAR;
        public final ModConfigSpec.BooleanValue ENABLE_TRANSLATION_SCOREBOARD;
        public final ModConfigSpec.BooleanValue ENABLE_SCOREBOARD_PLAYER_TRANSLATION;
        public final ModConfigSpec.BooleanValue ENABLE_TRANSLATION_ENTITYNAME;
        public final ModConfigSpec.IntValue ENTITYNAME_TRANSLATION_DISTANCE;

        Common(ModConfigSpec.Builder builder){
            // main
            builder.push("main_options");
            ENABLE_TRANSLATION_MAIN = builder
                    .translation("mutran.configuration.enable_translation_main")
                    .define("enable_translation_main", false);
            AUTO_DETECT_LANGUAGE = builder
                    .translation("mutran.configuration.auto_detect_language")
                    .define("auto_detect_language", true);
            FROM_LANGUAGE = builder
                    .translation("mutran.configuration.from_language")
                    .defineEnum("from_language", TranslationLanguage.English);
            TO_LANGUAGE = builder
                    .translation("mutran.configuration.to_language")
                    .defineEnum("to_language", TranslationLanguage.Chinese);
            TRANSLATION_PROVIDER = builder
                    .translation("mutran.configuration.translation_provider")
                    .defineEnum("translation_provider", TranslationProvider.Youdao);
            TRANSLATION_WAIT_TIME = builder
                    .translation("mutran.configuration.translation_wait_time")
                    .defineInRange("translation_wait_time", 1000, 200, 2000);
            ENABLE_MERGE_TRANSLATION = builder
                    .translation("mutran.configuration.enable_merge_translation")
                    .define("enable_merge_translation", true);
            builder.pop();

            // app
            builder.push("app_options");
            APP_ID = builder
                    .translation("mutran.configuration.app_id")
                    .define("app_id", "");
            APP_SECRET = builder
                    .translation("mutran.configuration.app_secret")
                    .define("app_secret", "");
            builder.pop();

            // chat
            builder.push("chat_options");
            ENABLE_TRANSLATION_CHAT = builder
                    .translation("mutran.configuration.enable_translation_chat")
                    .define("enable_translation_chat", false);
            TRANSLATE_SYSTEM_MESSAGE = builder
                    .translation("mutran.configuration.translate_system_message")
                    .define("translate_system_message", false);
            TRANSLATE_WITH_STYLE = builder
                    .translation("mutran.configuration.translate_with_style")
                    .define("translate_with_style", false);
            CHAT_TRANSLATION_DISPLAY_MODE = builder
                    .translation("mutran.configuration.chat_translation_display_mode")
                    .defineEnum("chat_translation_display_mode", ChatTranslationDisplayMode.Follow);
            builder.pop();

            // item
            builder.push("item_options");
            ENABLE_TRANSLATION_ITEM = builder
                    .translation("mutran.configuration.enable_translation_item")
                    .define("enable_translation_item", false);
            AUTO_TRANSLATE_ITEM = builder
                    .translation("mutran.configuration.auto_translate_item")
                    .define("auto_translate_item", false);
            ITEM_TRANSLATION_DISPLAY_MODE = builder
                    .translation("mutran.configuration.item_translation_display_mode")
                    .defineEnum("item_translation_display_mode", ItemTranslationDisplayMode.Follow);
            ITEM_TRANSLATION_SAVE_MODE = builder
                    .translation("mutran.configuration.item_translation_save_mode")
                    .defineEnum("item_translation_save_mode", ItemTranslationSaveMode.Permanent);
            builder.pop();

            // book
            builder.push("book_options");
            ENABLE_TRANSLATION_BOOK = builder
                    .translation("mutran.configuration.enable_translation_book")
                    .define("enable_translation_book", false);
            AUTO_TRANSLATE_BOOK = builder
                    .translation("mutran.configuration.auto_translate_book")
                    .define("auto_translate_book", false);
            BOOK_TRANSLATION_DISPLAY_MODE = builder
                    .translation("mutran.configuration.book_translation_display_mode")
                    .defineEnum("book_translation_display_mode", BookTranslationDisplayMode.Expand);
            builder.pop();

            // other
            builder.push("other_options");
            ENABLE_TRANSLATION_TITLE = builder
                    .translation("mutran.configuration.enable_translation_title")
                    .define("enable_translation_title", false);
            ENABLE_TRANSLATION_ACTIONBAR = builder
                    .translation("mutran.configuration.enable_translation_actionbar")
                    .define("enable_translation_actionbar", false);
            ENABLE_TRANSLATION_BOSSBAR = builder
                    .translation("mutran.configuration.enable_translation_bossbar")
                    .define("enable_translation_bossbar", false);
            ENABLE_TRANSLATION_SCOREBOARD = builder
                    .translation("mutran.configuration.enable_translation_scoreboard")
                    .define("enable_translation_scoreboard", false);
            ENABLE_SCOREBOARD_PLAYER_TRANSLATION = builder
                    .translation("mutran.configuration.enable_scoreboard_player_translation")
                    .define("enable_scoreboard_player_translation", false);
            ENABLE_TRANSLATION_ENTITYNAME = builder
                    .translation("mutran.configuration.enable_translation_entityname")
                    .define("enable_translation_entityname", false);
            ENTITYNAME_TRANSLATION_DISTANCE = builder
                    .translation("mutran.configuration.entityname_translation_distance")
                    .defineInRange("entityname_translation_distance", 8, 4, 32);
            builder.pop();
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
        Follow, Replace, Expand
    }

    public enum ItemTranslationDisplayMode {
        Follow, Replace
    }

    public enum BookTranslationDisplayMode {
        Expand
    }

    public enum ItemTranslationSaveMode {
        Inventory, Server, Permanent
    }
}
