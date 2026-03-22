package com.muriane.mutran.mixin;

import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@OnlyIn(Dist.CLIENT)
@Mixin(BookEditScreen.class)
public interface BookEditScreenMixinInterface {
    @Accessor("currentPage")
    int getCurrentPage();

    @Invoker("getCurrentPageText")
    String invokerGetCurrentPageText();
}
