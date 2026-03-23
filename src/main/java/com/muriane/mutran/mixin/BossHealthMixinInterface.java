package com.muriane.mutran.mixin;

import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@Mixin(BossHealthOverlay.class)
public interface BossHealthMixinInterface {
    @Accessor("events")
    Map<UUID, LerpingBossEvent> getEvents();
}
