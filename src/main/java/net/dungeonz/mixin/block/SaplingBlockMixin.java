package net.dungeonz.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.init.ConfigInit;
import net.dungeonz.init.DimensionInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

@Mixin(SaplingBlock.class)
public class SaplingBlockMixin {

    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    private void generateMixin(ServerWorld world, BlockPos pos, BlockState state, Random random, CallbackInfo info) {
        if (world.getRegistryKey() == DimensionInit.DUNGEON_WORLD || ConfigInit.CONFIG.devMode) {
            info.cancel();
        }
    }

}
