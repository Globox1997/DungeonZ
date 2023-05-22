package net.dungeonz.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.dungeonz.access.ServerPlayerAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dimension.DungeonPlacementHandler;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.init.DimensionInit;
import net.dungeonz.network.DungeonServerPacket;
import net.dungeonz.util.DungeonHelper;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void onPlayerConnectMixin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
        if (player.world.getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
            if (DungeonHelper.getCurrentDungeon(player) != null && DungeonHelper.getDungeonPortalEntity(player).getDungeonPlayerUUIDs().contains(player.getUuid())) {
                Dungeon dungeon = DungeonHelper.getCurrentDungeon(player);
                DungeonServerPacket.writeS2CDungeonInfoPacket(player, dungeon.getBreakableBlockIdList(), dungeon.getplaceableBlockIdList(), dungeon.isElytraAllowed());
            } else {
                ServerWorld oldWorld = ((ServerPlayerAccess) player).getOldServerWorld();
                if (oldWorld != null) {
                    FabricDimensions.teleport(player, oldWorld, DungeonPlacementHandler.leave(player, oldWorld));
                } else {
                    Vec3d spawnPos = null;
                    if (player.getSpawnPointPosition() != null) {
                        spawnPos = new Vec3d(player.getSpawnPointPosition().getX(), player.getSpawnPointPosition().getY(), player.getSpawnPointPosition().getZ());
                    } else {
                        spawnPos = PlayerEntity.findRespawnPosition(player.server.getWorld(player.getSpawnPointDimension()), ((ServerPlayerAccess) player).getDungeonSpawnBlockPos(), 0.0f, true, true)
                                .get();
                    }
                    FabricDimensions.teleport(player, player.server.getWorld(player.getSpawnPointDimension()), new TeleportTarget(spawnPos, new Vec3d(0.0D, 0.0D, 0.0D), 0.0f, 0.0f));
                }
            }
        }
    }

    @Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onPlayerRespawned(Lnet/minecraft/server/network/ServerPlayerEntity;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void respawnPlayerMixin(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> info, BlockPos blockPos, float f, boolean bl, ServerWorld serverWorld,
            Optional<Vec3d> optional2, ServerWorld serverWorld2, ServerPlayerEntity serverPlayerEntity) {
        if (!alive && oldPlayer.world.getRegistryKey() == DimensionInit.DUNGEON_WORLD && DungeonHelper.getDungeonPortalEntity(oldPlayer) != null) {
            DungeonPortalEntity dungeonPortalEntity = DungeonHelper.getDungeonPortalEntity(oldPlayer);
            dungeonPortalEntity.getDungeonPlayerUUIDs().remove(oldPlayer.getUuid());
            dungeonPortalEntity.addDeadDungeonPlayerUUIDs(serverPlayerEntity.getUuid());
            if (dungeonPortalEntity.getDungeonPlayerCount() == 0) {
                dungeonPortalEntity.setCooldown(dungeonPortalEntity.getDungeon().getCooldown());
            }
            dungeonPortalEntity.markDirty();
        }
    }
}
