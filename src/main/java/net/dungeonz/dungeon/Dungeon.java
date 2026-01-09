package net.dungeonz.dungeon;

import net.dungeonz.DungeonzMain;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class Dungeon {

    private final String dungeonTypeId;

    private final HashMap<Integer, List<EntityType<?>>> blockIdEntityMap;
    private final HashMap<Integer, HashMap<String, Float>> blockIdEntitySpawnChance;
    private final HashMap<Integer, Integer> blockIdBlockReplacement;

    private final HashMap<Integer, Integer> spawnerEntityIdCountMap;

    private final HashMap<String, HashMap<Integer, Integer>> difficultyRequiredItemCountMap;

    private final List<Integer> breakableBlockIds;
    private final List<Integer> placeableBlockIds;

    private final List<String> difficultyList;
    private final HashMap<String, Float> difficultyMobHealthModificator;
    private final HashMap<String, Float> difficultyMobDamageModificator;
    private final HashMap<String, Float> difficultyMobProtectionModificator;
    private final HashMap<String, Float> difficultyMobSpeedModificator;
    private final HashMap<String, List<String>> difficultyLootTableIds;
    private final HashMap<String, Float> difficultyBossHealthModificator;
    private final HashMap<String, Float> difficultyBossDamageModificator;
    private final HashMap<String, Float> difficultyBossProtectionModificator;
    private final HashMap<String, Float> difficultyBossSpeedModificator;
    private final HashMap<String, String> difficultyBossLootTable;

    private final EntityType<?> bossEntityType;
    @Nullable
    private final NbtCompound bossNbtCompound;
    private final int bossBlockId;
    private final int bossLootBlockId;

    private final int exitBlockId;

    private final boolean allowRespawn;
    private final boolean allowElytra;
    private final boolean keepInventory;
    private final boolean allowEnderPearl;
    private final boolean allowPositiveEffects;

    private final int maxGroupSize;
    private final int minGroupSize;
    private final int requiredLevel;
    private final int cooldown;

    @Nullable
    private final Identifier dungeonBackgroundId;
    private final Identifier dungeonStructurePoolId;

    public Dungeon(String dungeonTypeId, HashMap<Integer, List<EntityType<?>>> blockIdEntityMap, HashMap<Integer, HashMap<String, Float>> blockIdEntitySpawnChance,
                    HashMap<Integer, Integer> blockIdBlockReplacement, HashMap<Integer, Integer> spawnerEntityIdCountMap, HashMap<String, HashMap<Integer, Integer>> difficultyRequiredItemCountMap, List<Integer> breakableBlockIds,
                    List<Integer> placeableBlockIds, List<String> difficultyList, HashMap<String, Float> difficultyMobHealthModificator, HashMap<String, Float> difficultyMobDamageModificator, HashMap<String, Float> difficultyMobProtectionModificator, HashMap<String, Float> difficultyMobSpeedModificator,
                    HashMap<String, List<String>> difficultyLootTableIds, HashMap<String, Float> difficultyBossHealthModificator, HashMap<String, Float> difficultyBossDamageModificator, HashMap<String, Float> difficultyBossProtectionModificator, HashMap<String, Float> difficultyBossSpeedModificator,
                    HashMap<String, String> difficultyBossLootTable, EntityType<?> bossEntityType, @Nullable NbtCompound bossNbtCompound, int bossBlockId, int bossLootBlockId, int exitBlockId, boolean allowRespawn,
                    boolean allowElytra, boolean keepInventory, boolean allowEnderPearl, boolean allowPositiveEffects, int maxGroupSize, int minGroupSize, int requiredLevel, int cooldown, @Nullable Identifier dungeonBackgroundId, Identifier dungeonStructurePoolId) {
        this.dungeonTypeId = dungeonTypeId;
        this.blockIdEntityMap = blockIdEntityMap;
        this.blockIdEntitySpawnChance = blockIdEntitySpawnChance;
        this.blockIdBlockReplacement = blockIdBlockReplacement;
        this.spawnerEntityIdCountMap = spawnerEntityIdCountMap;
        this.difficultyRequiredItemCountMap = difficultyRequiredItemCountMap;
        this.breakableBlockIds = breakableBlockIds;
        this.placeableBlockIds = placeableBlockIds;
        this.difficultyList = difficultyList;
        this.difficultyMobHealthModificator = difficultyMobHealthModificator;
        this.difficultyMobDamageModificator = difficultyMobDamageModificator;
        this.difficultyMobProtectionModificator = difficultyMobProtectionModificator;
        this.difficultyMobSpeedModificator = difficultyMobSpeedModificator;
        this.difficultyLootTableIds = difficultyLootTableIds;
        this.difficultyBossHealthModificator = difficultyBossHealthModificator;
        this.difficultyBossDamageModificator = difficultyBossDamageModificator;
        this.difficultyBossProtectionModificator = difficultyBossProtectionModificator;
        this.difficultyBossSpeedModificator = difficultyBossSpeedModificator;
        this.difficultyBossLootTable = difficultyBossLootTable;
        this.bossEntityType = bossEntityType;
        this.bossNbtCompound = bossNbtCompound;
        this.bossBlockId = bossBlockId;
        this.bossLootBlockId = bossLootBlockId;
        this.exitBlockId = exitBlockId;
        this.allowRespawn = allowRespawn;
        this.allowElytra = allowElytra;
        this.keepInventory = keepInventory;
        this.allowEnderPearl = allowEnderPearl;
        this.allowPositiveEffects = allowPositiveEffects;
        this.maxGroupSize = maxGroupSize;
        this.minGroupSize = minGroupSize;
        this.requiredLevel = requiredLevel;
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

    @Nullable
    public Identifier getBackgroundId() {
        return this.dungeonBackgroundId;
    }

    public List<String> getDifficultyList() {
        return this.difficultyList;
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

    public HashMap<String, Float> getDifficultyMobHealthModificatorMap() {
        return this.difficultyMobHealthModificator;
    }

    public HashMap<String, Float> getDifficultyMobDamageModificatorMap() {
        return this.difficultyMobDamageModificator;
    }

    public HashMap<String, Float> getDifficultyMobProtectionModificatorMap() {
        return this.difficultyMobProtectionModificator;
    }

    public HashMap<String, Float> getDifficultyMobSpeedModificatorMap() {
        return this.difficultyMobSpeedModificator;
    }

    public HashMap<String, List<String>> getDifficultyLootTableIdMap() {
        return this.difficultyLootTableIds;
    }

    public HashMap<String, Float> getDifficultyBossHealthModificatorMap() {
        return this.difficultyBossHealthModificator;
    }

    public HashMap<String, Float> getDifficultyBossDamageModificatorMap() {
        return this.difficultyBossDamageModificator;
    }

    public HashMap<String, Float> getDifficultyBossProtectionModificatorMap() {
        return this.difficultyBossProtectionModificator;
    }

    public HashMap<String, Float> getDifficultyBossSpeedModificatorMap() {
        return this.difficultyBossSpeedModificator;
    }

    public HashMap<String, String> getDifficultyBossLootTableMap() {
        return this.difficultyBossLootTable;
    }

    public HashMap<Integer, Integer> getSpawnerEntityIdMap() {
        return this.spawnerEntityIdCountMap;
    }

    public HashMap<String, HashMap<Integer, Integer>> getDifficultyRequiredItemCountMap() {
        return this.difficultyRequiredItemCountMap;
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

    @Nullable
    public NbtCompound getBossNbtCompound() {
        return this.bossNbtCompound;
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

    public int getMinGroupSize() {
        return this.minGroupSize;
    }

    public int getRequiredLevel() {
        return this.requiredLevel;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public boolean isElytraAllowed() {
        return this.allowElytra;
    }

    public boolean isRespawnAllowed() {
        return this.allowRespawn;
    }

    public boolean isKeepInventory() {
        return keepInventory;
    }

    public boolean isEnderPearlAllowed() {
        return allowEnderPearl;
    }

    public boolean isPositiveEffectsAllowed() {
        return allowPositiveEffects;
    }

    public boolean containsBlockId(int blockId) {
        if (this.blockIdEntityMap.containsKey(blockId)) {
            return true;
        }
        return false;
    }

    public static void addDungeon(Dungeon dungeon) {
        if (!DungeonzMain.DUNGEONS.stream().anyMatch(d -> d.getDungeonTypeId().equals(dungeon.getDungeonTypeId()))) {
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
