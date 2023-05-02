package net.dungeonz.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.dungeonz.init.DimensionInit;
import net.dungeonz.util.DungeonHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    // @Inject(method = "interactItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I", ordinal = 0), cancellable = true)
    // private void interactItemMixin(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> info) {
    // System.out.println("INTERACT");
    // if (world.getRegistryKey() == DimensionInit.DUNGEON_WORLD && !player.isCreative() && stack.getItem() instanceof BlockItem) {
    // System.out.println("INT " + DungeonHelper.getCurrentDungeon(player).getplaceableBlockIdList().contains(Registry.BLOCK.getRawId(((BlockItem) stack.getItem()).getBlock())));
    // if (DungeonHelper.getCurrentDungeon(player) != null
    // && !DungeonHelper.getCurrentDungeon(player).getplaceableBlockIdList().contains(Registry.BLOCK.getRawId(((BlockItem) stack.getItem()).getBlock()))) {
    // info.setReturnValue(ActionResult.PASS);
    // }
    // }
    // }

    @Shadow
    @Mutable
    @Final
    protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
    private void tryBreakBlockMixin(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        if (!player.world.isClient && player.world.getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
            if (!player.isCreative() && DungeonHelper.getCurrentDungeon(player) != null
                    && !DungeonHelper.getCurrentDungeon(player).getBreakableBlockIdList().contains(Registry.BLOCK.getRawId(player.world.getBlockState(pos).getBlock()))) {
                info.setReturnValue(false);
            } else {
                if (DungeonHelper.getDungeonPortalEntity(player) != null && !DungeonHelper.getDungeonPortalEntity(player).getReplaceBlockIdMap().containsKey(pos)) {
                    DungeonHelper.getDungeonPortalEntity(player).addReplaceBlockId(pos, player.world.getBlockState(pos).getBlock());
                    DungeonHelper.getDungeonPortalEntity(player).markDirty();
                }
            }
        }
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;isCreative()Z"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void interactBlockMixin(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> info, BlockPos blockPos,
            BlockState blockState, boolean bl, boolean bl2, ItemStack itemStack, ItemUsageContext itemUsageContext) {
        if (world.getRegistryKey() == DimensionInit.DUNGEON_WORLD && !player.isCreative() && stack.getItem() instanceof BlockItem) {
            if (DungeonHelper.getCurrentDungeon(player) != null) {
                if (!DungeonHelper.getCurrentDungeon(player).getplaceableBlockIdList().contains(Registry.BLOCK.getRawId(((BlockItem) stack.getItem()).getBlock()))) {
                    info.setReturnValue(ActionResult.PASS);
                } else if (DungeonHelper.getDungeonPortalEntity(player) != null) {
                    if (!DungeonHelper.getDungeonPortalEntity(player).getReplaceBlockIdMap().containsKey(itemUsageContext.getBlockPos().offset(itemUsageContext.getSide()))) {
                        DungeonHelper.getDungeonPortalEntity(player).addReplaceBlockId(itemUsageContext.getBlockPos().offset(itemUsageContext.getSide()), Blocks.AIR);
                        DungeonHelper.getDungeonPortalEntity(player).markDirty();
                    }
                }
            }
        }
    }
}
