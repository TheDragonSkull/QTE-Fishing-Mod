package net.thedragonskull.qtefishingmod.mixin;

import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingHook.class)
public interface FishingHookAccessor {
    @Accessor("nibble")
    int getNibble();

    @Accessor("luck")
    int getLuck();
}
