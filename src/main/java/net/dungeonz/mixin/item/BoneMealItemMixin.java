package net.dungeonz.mixin.item;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dungeonz.init.DimensionInit;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {

    @Inject(method = "useOnFertilizable", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Fertilizable;grow(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"), cancellable = true)
    private static void useOnFertilizableMixin(ItemStack stack, World world, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        if (world.getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "useOnGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRandom()Lnet/minecraft/util/math/random/Random;"), cancellable = true)
    private static void useOnGroundMixin(ItemStack stack, World world, BlockPos blockPos, @Nullable Direction facing, CallbackInfoReturnable<Boolean> info) {
        if (world.getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
            info.setReturnValue(false);
        }
    }
}
