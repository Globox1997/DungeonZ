package net.dungeonz.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.init.DimensionInit;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.Explosion.DestructionType;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow
    @Mutable
    @Final
    private World world;

    @Shadow
    @Mutable
    @Final
    private DestructionType destructionType;

    @Inject(method = "affectWorld", at = @At("HEAD"))
    private void affectWorldMixin(boolean particles, CallbackInfo info) {
        if (world.getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
            destructionType = DestructionType.KEEP;
        }
    }

}
