package net.dungeonz.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.dungeonz.access.ClientPlayerAccess;
import net.dungeonz.init.DimensionInit;
import net.dungeonz.util.DungeonHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "canPlaceOn", at = @At(value = "HEAD"), cancellable = true)
    public void canPlaceOnMixin(BlockPos pos, Direction facing, ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        PlayerEntity playerEntity = (PlayerEntity) (Object) this;
        if (playerEntity != null && !playerEntity.isCreative() && playerEntity.world.getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
            info.setReturnValue(false);
        }
    }

    // used on client and server
    // currently only handled on server
    @Inject(method = "checkFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    public void checkFallFlyingMixin(CallbackInfoReturnable<Boolean> info) {
        PlayerEntity playerEntity = (PlayerEntity) (Object) this;
        // if (playerEntity != null && !playerEntity.world.isClient && !playerEntity.isCreative() && playerEntity.world.getRegistryKey() == DimensionInit.DUNGEON_WORLD
        // && DungeonHelper.getCurrentDungeon((ServerPlayerEntity) playerEntity) != null && !DungeonHelper.getCurrentDungeon((ServerPlayerEntity) playerEntity).isElytraAllowed()) {
        // info.setReturnValue(false);
        // }
        if (playerEntity != null && !playerEntity.isCreative() && playerEntity.world.getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
            if (!playerEntity.world.isClient) {
                if (DungeonHelper.getCurrentDungeon((ServerPlayerEntity) playerEntity) != null && !DungeonHelper.getCurrentDungeon((ServerPlayerEntity) playerEntity).isElytraAllowed()) {
                    info.setReturnValue(false);
                }
            } else {
                if (!((ClientPlayerAccess) playerEntity).isElytraAllowed()) {
                    info.setReturnValue(false);
                }
            }
        }
    }

    // used on client and server
    // currently only handled on server -> leads to Mismatch in destroy block error message
    // @Inject(method = "isBlockBreakingRestricted", at = @At("HEAD"), cancellable = true)
    // private void isBlockBreakingRestrictedMixin(World world, BlockPos pos, GameMode gameMode, CallbackInfoReturnable<Boolean> info) {
    // if (!world.isClient && world.getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
    // PlayerEntity playerEntity = (PlayerEntity) (Object) this;
    // if (!playerEntity.isCreative() && DungeonHelper.getCurrentDungeon((ServerPlayerEntity) playerEntity) != null
    // && !DungeonHelper.getCurrentDungeon((ServerPlayerEntity) playerEntity).getDestroyableBlockIdList().contains(Registry.BLOCK.getRawId(world.getBlockState(pos).getBlock()))) {
    // info.setReturnValue(true);
    // } else {
    // System.out.println("RESTRICTED " + pos);
    // // if (DungeonHelper.getDungeonPortalEntity((ServerPlayerEntity) playerEntity) != null
    // // && !DungeonHelper.getDungeonPortalEntity((ServerPlayerEntity) playerEntity).getReplaceBlockIdMap().containsKey(pos)) {
    // // DungeonHelper.getDungeonPortalEntity((ServerPlayerEntity) playerEntity).addReplaceBlockId(pos, world.getBlockState(pos).getBlock());
    // // }
    // }
    // }
    // }

}
