package net.dungeonz.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dungeonz.init.ConfigInit;
import net.dungeonz.init.DimensionInit;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Mixin(FireBlock.class)
public class FireBlockMixin {

    @Inject(method = "areBlocksAroundFlammable", at = @At("HEAD"), cancellable = true)
    private void areBlocksAroundFlammableMixin(BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        if ((world instanceof ServerWorld && ((ServerWorld) world).getRegistryKey() == DimensionInit.DUNGEON_WORLD) || ConfigInit.CONFIG.devMode) {
            info.cancel();
        }
    }

}
