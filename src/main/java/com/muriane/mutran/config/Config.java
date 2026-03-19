package com.muriane.mutran.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForgeConfig;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    public static final ModConfigSpec commonSpec;
    public static final Common COMMON;

    static {
        final Pair<Config.Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Config.Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Common {
        public final ModConfigSpec.BooleanValue ENABLE_TRANSLATION;
        public final ModConfigSpec.BooleanValue CHAT_REPLACEMENT_MODE;
        public final ModConfigSpec.EnumValue<TranslationProvider> TRANSLATION_PROVIDER;
        public final ModConfigSpec.ConfigValue<String> API_KEY;
        public final ModConfigSpec.ConfigValue<String> API_SECRET;

        Common(ModConfigSpec.Builder builder){
            ENABLE_TRANSLATION = builder
                    .comment("Is translation enable")
                    .translation("mutran.configuration.enable_translation")
                    .define("enable_translation", true);

            CHAT_REPLACEMENT_MODE = builder
                    .comment("Replace the original chats")
                    .translation("mutran.configuration.chat_replacement_mode")
                    .define("chat_replacement_mode", false);

            TRANSLATION_PROVIDER = builder
                    .comment("Choose your translation provider")
                    .translation("mutran.configuration.translation_provider")
                    .defineEnum("translation_provider", TranslationProvider.YOUDAO);

            API_KEY = builder
                    .comment("The api key your translation provider need")
                    .translation("mutran.configuration.api_key")
                    .define("api_key", "");

            API_SECRET = builder
                    .comment("The api secret your translation provider need")
                    .translation("mutran.configuration.api_secret")
                    .define("api_secret", "");
        }
    }

    public enum TranslationProvider{
        YOUDAO("mutran.configuration.translation_provider.enum.youdao", "https://openapi.youdao.com/api");

        private final String displayName;
        private final String apiUrl;

        TranslationProvider(String displayName, String apiUrl) {
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

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
