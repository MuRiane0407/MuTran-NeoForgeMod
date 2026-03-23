package com.muriane.mutran.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@OnlyIn(Dist.CLIENT)
@Mixin(Gui.class)
public interface GuiMixinInterface {
    @Accessor("title")
    Component getTitle();

    @Accessor("subtitle")
    Component getSubtitle();

    @Accessor("overlayMessageString")
    Component getActionbar();
}
