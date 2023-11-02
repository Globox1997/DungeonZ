package net.dungeonz.dungeon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.dungeonz.DungeonzMain;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

public class Dungeon {

    private final String dungeonTypeId;

    private final HashMap<Integer, List<EntityType<?>>> blockIdEntityMap;
    private final HashMap<Integer, HashMap<String, Float>> blockIdEntitySpawnChance;
    private final HashMap<Integer, Integer> blockIdBlockReplacement;

    private final HashMap<Integer, Integer> spawnerEntityIdCountMap;

    private final HashMap<Integer, Integer> requiredItemCountMap;

    private final List<Integer> breakableBlockIds;
    private final List<Integer> placeableBlockIds;

    private final HashMap<String, Float> difficultyMobModificator;
    private final HashMap<String, List<String>> difficultyLootTableIds;
    private final HashMap<String, Float> difficultyBossModificator;
    private final HashMap<String, String> difficultyBossLootTable;

    private final EntityType<?> bossEntityType;
    private final int bossBlockId;
    private final int bossLootBlockId;

    private final int exitBlockId;

    private final boolean allowElytra;

    private final int maxGroupSize;
    private final int cooldown;

    private final Identifier dungeonBackgroundId;
    private final Identifier dungeonStructurePoolId;

    public Dungeon(String dungeonTypeId, HashMap<Integer, List<EntityType<?>>> blockIdEntityMap, HashMap<Integer, HashMap<String, Float>> blockIdEntitySpawnChance,
            HashMap<Integer, Integer> blockIdBlockReplacement, HashMap<Integer, Integer> spawnerEntityIdCountMap, HashMap<Integer, Integer> requiredItemCountMap, List<Integer> breakableBlockIds,
            List<Integer> placeableBlockIds, HashMap<String, Float> difficultyMobModificator, HashMap<String, List<String>> difficultyLootTableIds, HashMap<String, Float> difficultyBossModificator,
            HashMap<String, String> difficultyBossLootTable, EntityType<?> bossEntityType, int bossBlockId, int bossLootBlockId, int exitBlockId, boolean allowElytra, int maxGroupSize, int cooldown,
            Identifier dungeonBackgroundId, Identifier dungeonStructurePoolId) {
        this.dungeonTypeId = dungeonTypeId;
        this.blockIdEntityMap = blockIdEntityMap;
        this.blockIdEntitySpawnChance = blockIdEntitySpawnChance;
        this.blockIdBlockReplacement = blockIdBlockReplacement;
        this.spawnerEntityIdCountMap = spawnerEntityIdCountMap;
        this.requiredItemCountMap = requiredItemCountMap;
        this.breakableBlockIds = breakableBlockIds;
        this.placeableBlockIds = placeableBlockIds;
        this.difficultyMobModificator = difficultyMobModificator;
        this.difficultyLootTableIds = difficultyLootTableIds;
        this.difficultyBossModificator = difficultyBossModificator;
        this.difficultyBossLootTable = difficultyBossLootTable;
        this.bossEntityType = bossEntityType;
        this.bossBlockId = bossBlockId;
        this.bossLootBlockId = bossLootBlockId;
        this.exitBlockId = exitBlockId;
        this.allowElytra = allowElytra;
        this.maxGroupSize = maxGroupSize;
        this.cooldown = cooldown;
        this.dungeonBackgroundId = dungeonBackgroundId;
        this.dungeonStructurePoolId = dungeonStructurePoolId;
    }

    public String getDungeonTypeId() {
        return this.dungeonTypeId;
    }

    public Identifier getStructurePoolId() {
        return this.dungeonStructurePoolId;
    }

    public Identifier getBackgroundId() {
        return this.dungeonBackgroundId;
    }

    public List<String> getDifficultyList() {
        return new ArrayList<>(this.difficultyMobModificator.keySet());
    }

    public HashMap<Integer, List<EntityType<?>>> getBlockIdEntityMap() {
        return this.blockIdEntityMap;
    }

    public HashMap<Integer, HashMap<String, Float>> getBlockIdEntitySpawnChanceMap() {
        return this.blockIdEntitySpawnChance;
    }

    public HashMap<Integer, Integer> getBlockIdBlockReplacementMap() {
        return this.blockIdBlockReplacement;
    }

    public HashMap<String, Float> getDifficultyMobModificatorMap() {
        return this.difficultyMobModificator;
    }

    public HashMap<String, List<String>> getDifficultyLootTableIdMap() {
        return this.difficultyLootTableIds;
    }

    public HashMap<String, Float> getDifficultyBossModificatorMap() {
        return this.difficultyBossModificator;
    }

    public HashMap<String, String> getDifficultyBossLootTableMap() {
        return this.difficultyBossLootTable;
    }

    public HashMap<Integer, Integer> getSpawnerEntityIdMap() {
        return this.spawnerEntityIdCountMap;
    }

    public HashMap<Integer, Integer> getRequiredItemCountMap() {
        return this.requiredItemCountMap;
    }

    public List<Integer> getBreakableBlockIdList() {
        return this.breakableBlockIds;
    }

    public List<Integer> getplaceableBlockIdList() {
        return this.placeableBlockIds;
    }

    public EntityType<?> getBossEntityType() {
        return this.bossEntityType;
    }

    public int getBossBlockId() {
        return this.bossBlockId;
    }

    public int getBossLootBlockId() {
        return this.bossLootBlockId;
    }

    public int getExitBlockId() {
        return this.exitBlockId;
    }

    public int getMaxGroupSize() {
        return this.maxGroupSize;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public boolean isElytraAllowed() {
        return this.allowElytra;
    }

    public boolean containsBlockId(int blockId) {
        if (this.blockIdEntityMap.containsKey(blockId)) {
            return true;
        }
        return false;
    }

    public static void addDungeon(Dungeon dungeon) {
        if (!DungeonzMain.DUNGEONS.contains(dungeon)) {
            DungeonzMain.DUNGEONS.add(dungeon);
        }
    }

    @Nullable
    public static Dungeon getDungeon(String dungeonTypeId) {
        for (int i = 0; i < DungeonzMain.DUNGEONS.size(); i++) {
            if (DungeonzMain.DUNGEONS.get(i).getDungeonTypeId().equals(dungeonTypeId)) {
                return DungeonzMain.DUNGEONS.get(i);
            }
        }
        return null;
    }

}
