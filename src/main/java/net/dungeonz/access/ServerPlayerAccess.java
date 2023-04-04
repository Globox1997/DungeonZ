package net.dungeonz.access;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface ServerPlayerAccess {

    public void setDungeonInfo(ServerWorld world, BlockPos portalPos, BlockPos playerPos);

    public ServerWorld getOldServerWorld();

    public BlockPos getDungeonPortalBlockPos();

    public BlockPos getDungeonSpawnBlockPos();
}
