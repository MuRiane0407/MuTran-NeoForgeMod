package com.muriane.mutran.translate;

import com.muriane.mutran.MusTranslate;
import com.muriane.mutran.config.Config;
import com.muriane.mutran.mixin.BossHealthMixinInterface;
import com.muriane.mutran.mixin.GuiMixinInterface;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.muriane.mutran.MusTranslate.KEY_CATEGORY;
import static com.muriane.mutran.translate.ChatTranslate.ChatTranslateHolder.sendMessageWithSign;
import static com.muriane.mutran.translate.Translator.translateAsync;

public class OtherVanillaTranslate {
    // Key
    @EventBusSubscriber
    public static class KeyHolder{
        public static final KeyMapping RELOAD_KEY = new KeyMapping(
                "mutran.key.reload",
                GLFW.GLFW_KEY_UNKNOWN,
                KEY_CATEGORY
        );

        @SubscribeEvent
        private static void inputKey(InputEvent.Key event){
            if (Config.COMMON.ENABLE_TRANSLATION_MAIN.get()){
                if (event.getKey() == RELOAD_KEY.getKey().getValue() && event.getAction() == 1){
                    BossbarTranslate.reload();
                    ScoreboardTranslate.reload();
                    EntityNameTranslate.reload();
                }
            }
        }

        @SubscribeEvent
        private static void registerKeys(RegisterKeyMappingsEvent event){
            event.register(RELOAD_KEY);
        }
    }

    // Title and subtitle
    @EventBusSubscriber
    public static class TitleTranslate{
        private static Component lastTranslateTitle = null;
        private static Component lastTranslateTitleResult = null;
        private static Component lastTranslateSubtitle = null;
        private static Component lastTranslateSubtitleResult = null;

        @SubscribeEvent
        public static void onTick(ClientTickEvent.Pre event){
            if (Config.COMMON.ENABLE_TRANSLATION_MAIN.get() && Config.COMMON.ENABLE_TRANSLATION_TITLE.get()){
                Gui gui = Minecraft.getInstance().gui;

                Component title = ((GuiMixinInterface) gui).getTitle();
                if (title != null && (title != lastTranslateTitle && title != lastTranslateTitleResult)){
                    lastTranslateTitle = title;
                    translateAsync(title.getString(),
                            result -> {
                                if (result != null) {
                                    Component component = Component.literal(result).setStyle(title.getStyle());
                                    lastTranslateTitleResult = component;
                                    gui.setTitle(component);
                                }else{
                                    if (Minecraft.getInstance().player != null) {
                                        sendMessageWithSign(Minecraft.getInstance().player, Component.literal(Config.COMMON.TRANSLATION_PROVIDER.get().getDisplayName() + Component.translatable("mutran.error.cant_translate").getString()).withColor(0xFB5454));
                                    }
                                }
                    });
                }

                Component subtitle = ((GuiMixinInterface) gui).getSubtitle();
                if (subtitle != null && (subtitle != lastTranslateSubtitle && subtitle != lastTranslateSubtitleResult)){
                    lastTranslateSubtitle = subtitle;
                    translateAsync(subtitle.getString(),
                            result -> {
                                if (result != null) {
                                    Component component = Component.literal(result).setStyle(subtitle.getStyle());
                                    lastTranslateSubtitleResult = component;
                                    gui.setSubtitle(component);
                                }else{
                                    if (Minecraft.getInstance().player != null) {
                                        sendMessageWithSign(Minecraft.getInstance().player, Component.literal(Config.COMMON.TRANSLATION_PROVIDER.get().getDisplayName() + Component.translatable("mutran.error.cant_translate").getString()).withColor(0xFB5454));
                                    }
                                }
                    });
                }
            }
        }
    }

    // Actionbar
    @EventBusSubscriber
    public static class ActionbarTranslate{
        private static Component lastTranslateActionbar = null;
        private static Component lastTranslateActionbarResult = null;

        @SubscribeEvent
        public static void onTick(ClientTickEvent.Pre event){
            if (Config.COMMON.ENABLE_TRANSLATION_MAIN.get() && Config.COMMON.ENABLE_TRANSLATION_ACTIONBAR.get()){
                Gui gui = Minecraft.getInstance().gui;

                Component actionbar = ((GuiMixinInterface) gui).getActionbar();
                if (actionbar != null && (actionbar != lastTranslateActionbar && actionbar != lastTranslateActionbarResult)){
                    lastTranslateActionbar = actionbar;
                    translateAsync(actionbar.getString(),
                            result -> {
                                if (result != null) {
                                    Component component = Component.literal(result).setStyle(actionbar.getStyle());
                                    lastTranslateActionbarResult = component;
                                    gui.setOverlayMessage(component, false);
                                }else{
                                    if (Minecraft.getInstance().player != null) {
                                        sendMessageWithSign(Minecraft.getInstance().player, Component.literal(Config.COMMON.TRANSLATION_PROVIDER.get().getDisplayName() + Component.translatable("mutran.error.cant_translate").getString()).withColor(0xFB5454));
                                    }
                                }
                            });
                }
            }
        }
    }

    // Bossbar
    @EventBusSubscriber
    public static class BossbarTranslate{
        private static final Map<UUID, Component> bossbar = new HashMap<>();

        @SubscribeEvent
        public static void onTick(ClientTickEvent.Pre event){
            if (Config.COMMON.ENABLE_TRANSLATION_MAIN.get() && Config.COMMON.ENABLE_TRANSLATION_BOSSBAR.get()){
                Gui gui = Minecraft.getInstance().gui;

                Map<UUID, LerpingBossEvent> lerpingBossEventMap = ((BossHealthMixinInterface) gui.getBossOverlay()).getEvents();
                for (UUID uuid : lerpingBossEventMap.keySet()){
                    Component name = lerpingBossEventMap.get(uuid).getName();
                    if (!bossbar.containsKey(uuid)){
                        bossbar.put(uuid, name);

                        translateAsync(name.getString(),
                                result -> {
                                    if (result != null) {
                                        Component component = Component.literal(result).setStyle(name.getStyle());
                                        lerpingBossEventMap.get(uuid).setName(component);
                                    }else{
                                        if (Minecraft.getInstance().player != null) {
                                            sendMessageWithSign(Minecraft.getInstance().player, Component.literal(Config.COMMON.TRANSLATION_PROVIDER.get().getDisplayName() + Component.translatable("mutran.error.cant_translate").getString()).withColor(0xFB5454));
                                        }
                                    }
                        });
                    }
                }
            }else{
                reload();
            }
        }

        public static void reload(){
            Gui gui = Minecraft.getInstance().gui;
            Map<UUID, LerpingBossEvent> lerpingBossEventMap = ((BossHealthMixinInterface) gui.getBossOverlay()).getEvents();

            for (UUID uuid : lerpingBossEventMap.keySet()){
                if (bossbar.containsKey(uuid)){
                    lerpingBossEventMap.get(uuid).setName(bossbar.get(uuid));
                    bossbar.remove(uuid);
                }
            }
        }
    }

    // Scoreboard
    @EventBusSubscriber
    public static class ScoreboardTranslate{
        private static final Map<Objective, Component> scoreboardName = new HashMap<>();
        public static final Map<String, Component> scoreboardPlayerNameTranslation = new HashMap<>(); // 这部分翻译见PlayerScoreEntryMixinClass.java

        @SubscribeEvent
        public static void onTick(ClientTickEvent.Pre event){
            if (Config.COMMON.ENABLE_TRANSLATION_MAIN.get() && Config.COMMON.ENABLE_TRANSLATION_SCOREBOARD.get()){
                if (Minecraft.getInstance().level != null) {
                    Scoreboard scoreboard = Minecraft.getInstance().level.getScoreboard();

                    for (DisplaySlot slot : DisplaySlot.values()){
                        Objective objective = scoreboard.getDisplayObjective(slot);
                        if (objective != null) {
                            if (!scoreboardName.containsKey(objective)) {
                                Component displayName = objective.getDisplayName();
                                scoreboardName.put(objective, displayName);

                                translateAsync(displayName.getString(),
                                        result -> {
                                            if (result != null) {
                                                Component component = Component.literal(result).setStyle(displayName.getStyle());
                                                objective.setDisplayName(component);
                                            } else {
                                                if (Minecraft.getInstance().player != null) {
                                                    sendMessageWithSign(Minecraft.getInstance().player, Component.literal(Config.COMMON.TRANSLATION_PROVIDER.get().getDisplayName() + Component.translatable("mutran.error.cant_translate").getString()).withColor(0xFB5454));
                                                }
                                            }
                                });
                            }
                        }
                    }
                }
            }else{
                reload();
            }
        }

        public static void reload(){
            if (Minecraft.getInstance().level != null) {
                Scoreboard scoreboard = Minecraft.getInstance().level.getScoreboard();

                for (DisplaySlot slot : DisplaySlot.values()){
                    Objective objective = scoreboard.getDisplayObjective(slot);
                    if (objective != null && scoreboardName.containsKey(objective)){
                        objective.setDisplayName(scoreboardName.get(objective));
                        scoreboardName.remove(objective);
                    }
                }
            }

            scoreboardPlayerNameTranslation.clear();
        }
    }

    @EventBusSubscriber
    public static class EntityNameTranslate{
        private final static Map<UUID, Component> entityname = new HashMap<>();

        @SubscribeEvent
        public static void onTick(ClientTickEvent.Pre event){
            if (Config.COMMON.ENABLE_TRANSLATION_MAIN.get() && Config.COMMON.ENABLE_TRANSLATION_ENTITYNAME.get()){
                Player player = Minecraft.getInstance().player;
                ClientLevel level = Minecraft.getInstance().level;
                int distance = Config.COMMON.ENTITYNAME_TRANSLATION_DISTANCE.getAsInt();
                if (player != null && level != null){
                    AABB aabb = new AABB(
                            player.getX()-distance, player.getY()-distance, player.getZ()-distance,
                            player.getX()+distance, player.getY()+distance, player.getZ()+distance);
                    List<Entity> entities = level.getEntitiesOfClass(Entity.class, aabb);

                    for (Entity entity : entities){
                        if (!(entity instanceof Player) && !entityname.containsKey(entity.getUUID())){
                            Component name = entity.getDisplayName();
                            entityname.put(entity.getUUID(), name);
                            translateAsync(name.getString(),
                                    result -> {
                                        if (result != null) {
                                            Component component = Component.literal(result).setStyle(name.getStyle());
                                            entity.setCustomName(component);
                                        }else{
                                            if (Minecraft.getInstance().player != null) {
                                                sendMessageWithSign(Minecraft.getInstance().player, Component.literal(Config.COMMON.TRANSLATION_PROVIDER.get().getDisplayName() + Component.translatable("mutran.error.cant_translate").getString()).withColor(0xFB5454));
                                            }
                                        }
                            });
                        }
                    }
                }
            }else{
                reload();
            }
        }

        public static void reload(){
            Player player = Minecraft.getInstance().player;
            ClientLevel level = Minecraft.getInstance().level;
            int distance = Config.COMMON.ENTITYNAME_TRANSLATION_DISTANCE.getAsInt();
            if (player != null && level != null) {
                AABB aabb = new AABB(
                        player.getX() - distance, player.getY() - distance, player.getZ() - distance,
                        player.getX() + distance, player.getY() + distance, player.getZ() + distance);
                List<Entity> entities = level.getEntitiesOfClass(Entity.class, aabb);

                for (Entity entity : entities) {
                    if (!(entity instanceof Player) && entityname.containsKey(entity.getUUID())) {
                        entity.setCustomName(entityname.get(entity.getUUID()));
                        entityname.remove(entity.getUUID());
                    }
                }
            }
        }
    }
}
