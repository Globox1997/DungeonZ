package net.dungeonz.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dungeonz.init.DimensionInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WardenEntity;

@Mixin(WardenEntity.class)
public class WardenEntityMixin {

    @Inject(method = "isValidTarget", at = @At("HEAD"), cancellable = true)
    private void isValidTargetMixin(@Nullable Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (entity != null && entity.world.getRegistryKey() == DimensionInit.DUNGEON_WORLD && entity instanceof HostileEntity) {
            info.setReturnValue(false);
        }
    }
}
