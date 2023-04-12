package net.dungeonz.access;

import net.minecraft.util.math.BlockPos;

public interface BossEntityAccess {

    // public void setBoss(String dungeonType, String difficulty, String lootTableId, BlockPos lootChestPos);
    public void setBoss(BlockPos portalPos, String worldRegistryKey);
}
