package net.dungeonz.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.dungeonz.access.ClientPlayerAccess;
import net.dungeonz.init.DimensionInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Nullable
    public ClientPlayerInteractionManager interactionManager;

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void doItemUseMixin(CallbackInfo info, Hand[] var1, int var2, int var3, Hand hand, ItemStack itemStack) {
        if (player != null && !player.isCreative() && itemStack.getItem() instanceof BlockItem && player.world.getRegistryKey() == DimensionInit.DUNGEON_WORLD
                && !((ClientPlayerAccess) player).getPlaceableBlockIdList().contains(Registry.BLOCK.getRawId(((BlockItem) itemStack.getItem()).getBlock()))) {
            info.cancel();
        }
    }

    @Inject(method = "handleBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/BlockHitResult;getSide()Lnet/minecraft/util/math/Direction;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void handleBlockBreakingMixin(boolean bl, CallbackInfo info, BlockHitResult blockHitResult, BlockPos blockPos) {
        if (player != null && !player.isCreative() && player.world.getRegistryKey() == DimensionInit.DUNGEON_WORLD
                && !((ClientPlayerAccess) player).getBreakableBlockIdList().contains(Registry.BLOCK.getRawId(player.world.getBlockState(blockPos).getBlock()))) {
            interactionManager.cancelBlockBreaking();
            info.cancel();
        }
    }

}
