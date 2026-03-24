package com.muriane.mutran.mixin;

import com.muriane.mutran.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.muriane.mutran.translate.ChatTranslate.ChatTranslateHolder.sendMessageWithSign;
import static com.muriane.mutran.translate.OtherVanillaTranslate.ScoreboardTranslate.scoreboardPlayerNameTranslation;
import static com.muriane.mutran.translate.Translator.translateAsync;

@Mixin(PlayerScoreEntry.class)
public class PlayerScoreEntryMixinClass {
    @Shadow
    @Final
    private String owner;

    @Inject(method = "ownerName",
            at = @At(value = "RETURN"), cancellable = true)
    public void ownerName(CallbackInfoReturnable<Component> cir){
        if (Config.COMMON.ENABLE_TRANSLATION_MAIN.get() && Config.COMMON.ENABLE_TRANSLATION_SCOREBOARD.get() && Config.COMMON.ENABLE_SCOREBOARD_PLAYER_TRANSLATION.get()){
            if (!scoreboardPlayerNameTranslation.containsKey(this.owner)){
                scoreboardPlayerNameTranslation.put(this.owner, Component.literal(this.owner));
                translateAsync(this.owner,
                        result -> {
                            if (result != null){
                                scoreboardPlayerNameTranslation.put(this.owner, Component.literal(result));
                            }else{
                                if (Minecraft.getInstance().player != null) {
                                    sendMessageWithSign(Minecraft.getInstance().player, Component.literal(Config.COMMON.TRANSLATION_PROVIDER.get().getDisplayName() + Component.translatable("mutran.error.cant_translate").getString()).withColor(0xFB5454));
                                }
                            }
                });
            }else{
                cir.setReturnValue(scoreboardPlayerNameTranslation.get(this.owner));
            }
        }
    }
}
