package net.dungeonz.access;

import net.minecraft.util.math.BlockPos;

public interface BossEntityAccess {
    void setBoss(BlockPos portalPos, String worldRegistryKey);
    void setDungeonData(String dungeonType, String difficulty);
}