package net.dungeonz.dimension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import net.dungeonz.DungeonzMain;
import net.dungeonz.access.BossEntityAccess;
import net.dungeonz.access.ServerPlayerAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.block.entity.DungeonSpawnerEntity;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.init.BlockInit;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;

public class DungeonPlacementHandler {

    public static TeleportTarget enter(ServerPlayerEntity serverPlayerEntity, ServerWorld dungeonWorld, ServerWorld oldWorld, DungeonPortalEntity portalEntity, BlockPos portalPos) {
        ((ServerPlayerAccess) serverPlayerEntity).setDungeonInfo(oldWorld, portalPos, serverPlayerEntity.getBlockPos());

        BlockPos newPos = new BlockPos(0, 0, 0).add(portalPos.getX() * 4, 100, portalPos.getZ());

        // System.out.println("ENTER: " + portalPos.getX() + " : " + portalPos.getZ());
        // System.out.println("NEW POS " + newPos);

        if (!portalEntity.isDungeonStructureGenerated()) {
            portalEntity.setDungeonStructureGenerated();
            spawnDungeonStructure(dungeonWorld, newPos, portalEntity);
        }
        if (portalEntity.getDungeonPlayerCount() == 0) {
            // set difficulty here
            refreshDungeon(serverPlayerEntity.server, dungeonWorld, portalEntity, portalEntity.getDungeon(), "easy");
        }
        portalEntity.joinDungeon(serverPlayerEntity.getUuid());

        return new TeleportTarget(Vec3d.of(newPos).add(0.5, 0, 0.5), Vec3d.ZERO, 0, 0);
    }

    public static TeleportTarget leave(ServerPlayerEntity serverPlayerEntity, ServerWorld serverWorld) {
        if (serverWorld.getBlockEntity(((ServerPlayerAccess) serverPlayerEntity).getDungeonPortalBlockPos()) != null) {
            ((DungeonPortalEntity) serverWorld.getBlockEntity(((ServerPlayerAccess) serverPlayerEntity).getDungeonPortalBlockPos())).leaveDungeon(serverPlayerEntity.getUuid());
        }
        if (((DungeonPortalEntity) serverWorld.getBlockEntity(((ServerPlayerAccess) serverPlayerEntity).getDungeonPortalBlockPos())).getDungeonPlayerCount() == 0) {
            // Break boss chest and back portal and maybe kill boss
            // maybe add a leave command

        }
        return new TeleportTarget(Vec3d.of(((ServerPlayerAccess) serverPlayerEntity).getDungeonSpawnBlockPos()).add(0.5, 0, 0.5), Vec3d.ZERO, serverWorld.random.nextFloat() * 360F, 0);
    }

    private static void spawnDungeonStructure(World world, BlockPos pos, DungeonPortalEntity portalEntity) {
        Registry<StructurePool> registry = world.getRegistryManager().get(Registry.STRUCTURE_POOL_KEY);

        // has to be the template_pool name
        RegistryEntry<StructurePool> registryEntry = registry.entryOf(RegistryKey.of(Registry.STRUCTURE_POOL_KEY, portalEntity.getDungeon().getStructurePoolId()));
        // has to be the first jigsaw block to generate of
        generate((ServerWorld) world, portalEntity, portalEntity.getDungeon(), registryEntry, new Identifier("dungeonz:spawn"), 16, pos, false);
    }

    private static boolean generate(ServerWorld world, DungeonPortalEntity portalEntity, Dungeon dungeon, RegistryEntry<StructurePool> structurePool, Identifier id, int size, BlockPos pos,
            boolean keepJigsaws) {
        ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
        StructureTemplateManager structureTemplateManager = world.getStructureTemplateManager();
        StructureAccessor structureAccessor = world.getStructureAccessor();
        Random random = world.getRandom();
        Structure.Context context = new Structure.Context(world.getRegistryManager(), chunkGenerator, chunkGenerator.getBiomeSource(), world.getChunkManager().getNoiseConfig(),
                structureTemplateManager, world.getSeed(), new ChunkPos(pos), world, registryEntry -> true);
        Optional<Structure.StructurePosition> optional = StructurePoolBasedGenerator.generate(context, structurePool, Optional.of(id), size, pos, false, Optional.empty(), 128);
        if (optional.isPresent()) {
            HashMap<Integer, ArrayList<BlockPos>> blockIdPosMap = new HashMap<Integer, ArrayList<BlockPos>>();
            ArrayList<BlockPos> chestPosList = new ArrayList<BlockPos>();
            ArrayList<BlockPos> exitPosList = new ArrayList<BlockPos>();
            HashMap<BlockPos, Integer> spawnerPosEntityIdMap = new HashMap<BlockPos, Integer>();
            Block exitBlock = Registry.BLOCK.get(dungeon.getExitBlockId());
            Block bossLootBlock = Registry.BLOCK.get(dungeon.getBossLootBlockId());

            StructurePiecesCollector structurePiecesCollector = optional.get().generate();
            for (StructurePiece structurePiece : structurePiecesCollector.toList().pieces()) {
                if (!(structurePiece instanceof PoolStructurePiece)) {
                    continue;
                }
                PoolStructurePiece poolStructurePiece = (PoolStructurePiece) structurePiece;
                poolStructurePiece.generate((StructureWorldAccess) world, structureAccessor, chunkGenerator, random, BlockBox.infinite(), pos, keepJigsaws);

                for (int i = poolStructurePiece.getBoundingBox().getMinX(); i <= poolStructurePiece.getBoundingBox().getMaxX(); i++) {
                    for (int u = poolStructurePiece.getBoundingBox().getMinY(); u <= poolStructurePiece.getBoundingBox().getMaxY(); u++) {
                        for (int o = poolStructurePiece.getBoundingBox().getMinZ(); o <= poolStructurePiece.getBoundingBox().getMaxZ(); o++) {
                            BlockPos checkPos = new BlockPos(i, u, o);
                            if (!world.getBlockState(checkPos).isAir()) {
                                int blockId = Registry.BLOCK.getRawId(world.getBlockState(checkPos).getBlock());
                                if (dungeon.containsBlockId(blockId)) {
                                    if (!blockIdPosMap.containsKey(blockId)) {
                                        ArrayList<BlockPos> newList = new ArrayList<BlockPos>();
                                        newList.add(checkPos);
                                        blockIdPosMap.put(blockId, newList);
                                    } else {
                                        blockIdPosMap.get(blockId).add(checkPos);
                                    }
                                } else if (dungeon.getBossBlockId() == blockId) {
                                    portalEntity.setBossBlockPos(checkPos);
                                } else if (world.getBlockState(checkPos).isOf(Blocks.CHEST)) {
                                    chestPosList.add(checkPos);
                                } else if (world.getBlockState(checkPos).isOf(exitBlock)) {
                                    exitPosList.add(checkPos);
                                } else if (world.getBlockState(checkPos).isOf(bossLootBlock)) {
                                    portalEntity.setBossLootBlockPos(checkPos);
                                } else if (world.getBlockState(checkPos).isOf(BlockInit.DUNGEON_SPAWNER)) {
                                    spawnerPosEntityIdMap.put(checkPos, ((DungeonSpawnerEntity) world.getBlockEntity(checkPos)).getLogic().getEntityId());
                                }
                            }
                        }
                    }
                }
            }
            portalEntity.setMaxGroupSize(dungeon.getMaxGroupSize());
            portalEntity.setChestPosList(chestPosList);
            portalEntity.setExitPosList(exitPosList);
            portalEntity.setBlockMap(blockIdPosMap);
            portalEntity.setSpawnerPosEntityIdMap(spawnerPosEntityIdMap);
            return true;
        }
        return false;
    }

    private static void refreshDungeon(MinecraftServer server, ServerWorld world, DungeonPortalEntity portalEntity, Dungeon dungeon, String difficulty) {
        portalEntity.setDifficulty(difficulty);
        portalEntity.getBlockMap().forEach((blockId, list) -> {
            for (int i = 0; i < list.size(); i++) {
                if (dungeon.getBlockIdBlockReplacementMap().get(blockId) != -1) {
                    if (dungeon.getBlockIdBlockReplacementMap().get(blockId) == 0) {
                        world.removeBlock(list.get(i), false);
                    } else {
                        world.setBlockState(list.get(i), Registry.BLOCK.get(dungeon.getBlockIdBlockReplacementMap().get(blockId)).getDefaultState(), 3);
                    }
                }
                // dungeon.getBlockIdEntitySpawnChanceMap().containsKey(blockId) &&
                if (dungeon.getBlockIdEntitySpawnChanceMap().get(blockId) <= world.getRandom().nextFloat()) {
                    MobEntity mobEntity = createMob(world, dungeon.getBlockIdEntityMap().get(blockId).get(world.getRandom().nextInt(dungeon.getBlockIdEntityMap().get(blockId).size())));
                    // hopefully initialize doesn't lead to problems
                    mobEntity.initialize(world, world.getLocalDifficulty(list.get(i)), SpawnReason.STRUCTURE, null, null);
                    mobEntity.setPersistent();
                    strengthenMob(mobEntity, dungeon, difficulty, false);
                    dungeon.getBlockIdBlockReplacementMap().get(blockId);
                    mobEntity.refreshPositionAndAngles(list.get(i), 360f * world.getRandom().nextFloat(), 0.0f);
                    world.spawnEntity(mobEntity);
                }
            }
        });
        MobEntity bossEntity = createMob(world, dungeon.getBossEntityType());
        bossEntity.setPersistent();
        // world.getRegistryKey().getValue().toString()
        ((BossEntityAccess) bossEntity).setBoss(portalEntity.getPos(), portalEntity.getWorld().getRegistryKey().getValue().toString());
        // ((BossEntityAccess) bossEntity).setBoss(dungeon.getDungeonTypeId(), difficulty, dungeon.getDifficultyBossLootTableMap().get(difficulty));
        strengthenMob(bossEntity, dungeon, difficulty, true);
        if (dungeon.getBlockIdBlockReplacementMap().get(dungeon.getBossBlockId()) != -1) {
            if (dungeon.getBlockIdBlockReplacementMap().get(dungeon.getBossBlockId()) == 0) {
                world.removeBlock(portalEntity.getBossBlockPos(), false);
            } else {
                world.setBlockState(portalEntity.getBossBlockPos(), Registry.BLOCK.get(dungeon.getBlockIdBlockReplacementMap().get(dungeon.getBossBlockId())).getDefaultState(), 3);
            }
        }
        bossEntity.refreshPositionAndAngles(portalEntity.getBossBlockPos(), 360f * world.getRandom().nextFloat(), 0.0f);

        world.spawnEntity(bossEntity);

        for (int i = 0; i < portalEntity.getChestPosList().size(); i++) {
            String lootTableString = dungeon.getDifficultyLootTableIdMap().get(difficulty).get(world.getRandom().nextInt(dungeon.getDifficultyLootTableIdMap().get(difficulty).size()));
            fillChestWithLoot(server, world, portalEntity.getChestPosList().get(i), lootTableString);
        }

        for (int i = 0; i < portalEntity.getExitPosList().size(); i++) {
            world.setBlockState(portalEntity.getExitPosList().get(i),
                    dungeon.getBlockIdBlockReplacementMap().containsKey(dungeon.getExitBlockId()) && dungeon.getBlockIdBlockReplacementMap().get(dungeon.getExitBlockId()) != -1
                            ? Registry.BLOCK.get(dungeon.getBlockIdBlockReplacementMap().get(dungeon.getExitBlockId())).getDefaultState()
                            : Registry.BLOCK.get(dungeon.getExitBlockId()).getDefaultState(),
                    3);
        }

        world.setBlockState(portalEntity.getBossLootBlockPos(),
                dungeon.getBlockIdBlockReplacementMap().containsKey(dungeon.getBossLootBlockId()) && dungeon.getBlockIdBlockReplacementMap().get(dungeon.getBossLootBlockId()) != -1
                        ? Registry.BLOCK.get(dungeon.getBlockIdBlockReplacementMap().get(dungeon.getBossLootBlockId())).getDefaultState()
                        : Registry.BLOCK.get(dungeon.getBossLootBlockId()).getDefaultState(),
                3);
        Iterator<Entry<BlockPos, Integer>> iterator = portalEntity.getSpawnerPosEntityIdMap().entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<BlockPos, Integer> entry = iterator.next();
            world.setBlockState(entry.getKey(), BlockInit.DUNGEON_SPAWNER.getDefaultState(), 3);
            ((DungeonSpawnerEntity) world.getBlockEntity(entry.getKey())).getLogic().setDungeonInfo(dungeon, difficulty,
                    dungeon.getSpawnerEntityIdMap().containsKey(entry.getValue()) ? dungeon.getSpawnerEntityIdMap().get(entry.getValue()) : 0, Registry.ENTITY_TYPE.get(entry.getValue()));
        }
    }

    public static void fillChestWithLoot(MinecraftServer server, ServerWorld world, BlockPos pos, String lootTableString) {
        LootTable lootTable = server.getLootManager().getTable(new Identifier(lootTableString));
        LootContext.Builder builder = new LootContext.Builder(world).parameter(LootContextParameters.ORIGIN, new Vec3d(pos.getX(), pos.getY(), pos.getZ())).random(world.getRandom().nextLong());
        ((ChestBlockEntity) world.getBlockEntity(pos)).clear();
        lootTable.supplyInventory((ChestBlockEntity) world.getBlockEntity(pos), builder.build(LootContextTypes.CHEST));
    }

    @Nullable
    private static MobEntity createMob(ServerWorld world, EntityType<?> type) {
        MobEntity mobEntity;
        try {
            Object entity = type.create(world);
            if (!(entity instanceof MobEntity)) {
                throw new IllegalStateException("Trying to spawn a non-mob: " + Registry.ENTITY_TYPE.getId(type));
            }
            mobEntity = (MobEntity) entity;
        } catch (Exception exception) {
            DungeonzMain.LOGGER.warn("Failed to create mob", exception);
            return null;
        }
        return mobEntity;
    }

    public static void strengthenMob(MobEntity mobEntity, Dungeon dungeon, String difficulty, boolean isBossEntity) {
        double mobHealth = mobEntity.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
        double mobDamage = 0.0D;
        double mobProtection = 0.0D;

        boolean hasAttackDamageAttribute = mobEntity.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        boolean hasArmorAttribute = mobEntity.getAttributes().hasAttribute(EntityAttributes.GENERIC_ARMOR);

        if (hasAttackDamageAttribute) {
            mobDamage = mobEntity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        }
        if (hasArmorAttribute) {
            mobProtection = mobEntity.getAttributeValue(EntityAttributes.GENERIC_ARMOR);
        }
        float strengthFactor = 0.0f;
        if (isBossEntity) {
            strengthFactor = dungeon.getDifficultyBossModificatorMap().get(difficulty);
        } else {
            strengthFactor = dungeon.getDifficultyMobModificatorMap().get(difficulty);
        }
        mobHealth *= strengthFactor;
        mobDamage *= strengthFactor;
        mobProtection *= strengthFactor;

        // round factor
        mobHealth = Math.round(mobHealth * 100.0D) / 100.0D;
        mobDamage = Math.round(mobDamage * 100.0D) / 100.0D;
        mobProtection = Math.round(mobProtection * 100.0D) / 100.0D;

        // Set Values
        mobEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(mobHealth);
        mobEntity.heal(mobEntity.getMaxHealth());
        if (hasAttackDamageAttribute) {
            mobEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(mobDamage);
        }
        if (hasArmorAttribute) {
            mobEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(mobProtection);
        }

    }

}
