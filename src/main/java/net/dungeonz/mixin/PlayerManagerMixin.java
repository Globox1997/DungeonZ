package net.dungeonz.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.init.DimensionInit;
import net.dungeonz.network.DungeonServerPacket;
import net.dungeonz.util.DungeonHelper;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void onPlayerConnectMixin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
        if (player.getWorld().getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
            if (DungeonHelper.getCurrentDungeon(player) != null && DungeonHelper.getDungeonPortalEntity(player).getDungeonPlayerUuids().contains(player.getUuid())
                    && !DungeonHelper.getDungeonPortalEntity(player).isOnCooldown((int) player.getWorld().getTime())) {
                Dungeon dungeon = DungeonHelper.getCurrentDungeon(player);
                DungeonServerPacket.writeS2CDungeonInfoPacket(player, dungeon.getBreakableBlockIdList(), dungeon.getplaceableBlockIdList(), dungeon.isElytraAllowed());
            } else {
                DungeonHelper.teleportOutOfDungeon(player);
            }
        }
    }

    @Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onPlayerRespawned(Lnet/minecraft/server/network/ServerPlayerEntity;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void respawnPlayerMixin(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> info, BlockPos blockPos, float f, boolean bl, ServerWorld serverWorld,
            Optional<Vec3d> optional2, ServerWorld serverWorld2, ServerPlayerEntity serverPlayerEntity) {
        if (!alive && oldPlayer.getWorld().getRegistryKey() == DimensionInit.DUNGEON_WORLD && DungeonHelper.getDungeonPortalEntity(oldPlayer) != null) {
            DungeonPortalEntity dungeonPortalEntity = DungeonHelper.getDungeonPortalEntity(oldPlayer);
            dungeonPortalEntity.getDungeonPlayerUuids().remove(oldPlayer.getUuid());
            dungeonPortalEntity.addDeadDungeonPlayerUuids(serverPlayerEntity.getUuid());
            if (dungeonPortalEntity.getDungeonPlayerCount() == 0) {
                dungeonPortalEntity.setCooldownTime(dungeonPortalEntity.getDungeon().getCooldown() + (int) serverWorld.getTime());
            }
            dungeonPortalEntity.markDirty();
        }
    }

}
