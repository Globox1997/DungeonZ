package net.dungeonz.util;

import org.jetbrains.annotations.Nullable;

import net.dungeonz.access.ServerPlayerAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.init.DimensionInit;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class DungeonHelper {

    @Nullable
    public static Dungeon getCurrentDungeon(ServerPlayerEntity playerEntity) {
        if (playerEntity.world.getRegistryKey() == DimensionInit.DUNGEON_WORLD && ((ServerPlayerAccess) playerEntity).getOldServerWorld() != null) {
            BlockEntity blockEntity = ((ServerPlayerAccess) playerEntity).getOldServerWorld().getBlockEntity(((ServerPlayerAccess) playerEntity).getDungeonPortalBlockPos());
            if (blockEntity == null) {
                return null;
            }
            if (blockEntity instanceof DungeonPortalEntity) {
                return ((DungeonPortalEntity) blockEntity).getDungeon();
            }
        }
        return null;
    }

    @Nullable
    public static DungeonPortalEntity getDungeonPortalEntity(ServerPlayerEntity playerEntity) {
        if (((ServerPlayerAccess) playerEntity).getOldServerWorld() != null) {
            BlockEntity blockEntity = ((ServerPlayerAccess) playerEntity).getOldServerWorld().getBlockEntity(((ServerPlayerAccess) playerEntity).getDungeonPortalBlockPos());
            if (blockEntity == null) {
                return null;
            }
            if (blockEntity instanceof DungeonPortalEntity) {
                return (DungeonPortalEntity) blockEntity;
            }
        }
        return null;
    }

}
