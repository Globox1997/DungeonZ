package net.dungeonz.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.init.ConfigInit;
import net.dungeonz.init.DimensionInit;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

@Mixin(BambooBlock.class)
public class BambooBlockMixin {

    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BambooBlock;updateLeaves(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;I)V"), cancellable = true)
    private void randomTickMixin(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo info) {
        if (world.getRegistryKey() == DimensionInit.DUNGEON_WORLD || ConfigInit.CONFIG.devMode) {
            info.cancel();
        }
    }
}
