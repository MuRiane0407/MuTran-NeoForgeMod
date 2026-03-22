package com.muriane.mutran.mixin;

import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@OnlyIn(Dist.CLIENT)
@Mixin(BookViewScreen.class)
public interface BookViewScreenMixinInterface {
    @Accessor("currentPage")
    int getCurrentPage();
}
