package com.muriane.mutran.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.muriane.mutran.api.IBookViewScreen;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.FormattedText;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BookViewScreen.class)
public abstract class BookViewScreenMixinClass implements IBookViewScreen {
    @Unique
    private static FormattedText text$mutran;

    @Override
    public FormattedText getText$mutran() {
        return text$mutran;
    }

    @Inject(method = "visitText",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectVisitText(ActiveTextCollector collector, boolean clickableOnly, CallbackInfo ci, @Local FormattedText cachedPageComponents) {
        text$mutran = cachedPageComponents;
    }
}
