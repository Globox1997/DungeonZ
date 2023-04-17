package net.dungeonz.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.dungeonz.DungeonzMain;
import net.dungeonz.dungeon.Dungeon;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class DungeonLoader implements SimpleSynchronousResourceReloadListener {

    @Override
    public Identifier getFabricId() {
        return new Identifier("dungeonz", "dungeon_loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        // restricted
        manager.findResources("dungeon", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                String dungeonTypeId = data.get("dungeon_type").getAsString();
                int maxGroupSize = data.get("max_group_size").getAsInt();
                int cooldown = data.get("cooldown").getAsInt();
                Identifier dungeonStructurePoolId = new Identifier(data.get("dungeon_structure_pool_id").getAsString());

                JsonObject difficultyObject = data.get("difficulty").getAsJsonObject();
                Iterator<String> difficultyIterator = difficultyObject.keySet().iterator();

                HashMap<String, Float> difficultyMobModificator = new HashMap<String, Float>();
                HashMap<String, List<String>> difficultyLootTableIds = new HashMap<String, List<String>>();
                HashMap<String, Float> difficultyBossModificator = new HashMap<String, Float>();
                HashMap<String, String> difficultyBossLootTable = new HashMap<String, String>();

                while (difficultyIterator.hasNext()) {
                    String difficulty = difficultyIterator.next();
                    JsonObject specificDifficultyObject = difficultyObject.get(difficulty).getAsJsonObject();

                    difficultyMobModificator.put(difficulty, specificDifficultyObject.get("mob_modificator").getAsFloat());
                    List<String> lootTableIds = new ArrayList<String>();
                    for (int i = 0; i < specificDifficultyObject.get("loot_table_ids").getAsJsonArray().size(); i++) {
                        lootTableIds.add(specificDifficultyObject.get("loot_table_ids").getAsJsonArray().get(i).getAsString());
                    }
                    difficultyLootTableIds.put(difficulty, lootTableIds);
                    difficultyBossModificator.put(difficulty, specificDifficultyObject.get("boss_modificator").getAsFloat());
                    difficultyBossLootTable.put(difficulty, specificDifficultyObject.get("boss_loot_table_id").getAsString());
                }

                JsonObject blockObject = data.get("blocks").getAsJsonObject();
                Iterator<String> blockIterator = blockObject.keySet().iterator();

                HashMap<Integer, List<EntityType<?>>> blockIdEntityMap = new HashMap<Integer, List<EntityType<?>>>();
                HashMap<Integer, Float> blockIdEntitySpawnChance = new HashMap<Integer, Float>();
                HashMap<Integer, Integer> blockIdBlockReplacement = new HashMap<Integer, Integer>();
                int bossBlockId = -1;
                int bossLootBlockId = -1;
                int exitBlockId = -1;
                EntityType<?> bossEntityType = null;

                while (blockIterator.hasNext()) {
                    String block = blockIterator.next();
                    if (Registry.BLOCK.get(new Identifier(block)).toString().equals("Block{minecraft:air}")) {
                        DungeonzMain.LOGGER.warn("{} is not a valid block identifier", block);
                        continue;
                    }
                    int rawBlockId = Registry.BLOCK.getRawId(Registry.BLOCK.get(new Identifier(block)));

                    JsonObject specificBlockObject = blockObject.get(block).getAsJsonObject();

                    if (specificBlockObject.has("spawns")) {
                        List<EntityType<?>> entityTypes = new ArrayList<EntityType<?>>();
                        for (int i = 0; i < specificBlockObject.get("spawns").getAsJsonArray().size(); i++) {
                            if (!Registry.ENTITY_TYPE.containsId(new Identifier(specificBlockObject.get("spawns").getAsJsonArray().get(i).getAsString()))) {
                                DungeonzMain.LOGGER.warn("{} is not a valid entity identifier", specificBlockObject.get("spawns").getAsJsonArray().get(i).getAsString());
                                continue;
                            }
                            entityTypes.add(Registry.ENTITY_TYPE.get(new Identifier(specificBlockObject.get("spawns").getAsJsonArray().get(i).getAsString())));
                        }
                        blockIdEntityMap.put(rawBlockId, entityTypes);
                        blockIdEntitySpawnChance.put(rawBlockId, specificBlockObject.get("chance").getAsFloat());
                    } else if (specificBlockObject.has("boss_entity")) {
                        if (!Registry.ENTITY_TYPE.containsId(new Identifier(specificBlockObject.get("boss_entity").getAsString()))) {
                            DungeonzMain.LOGGER.warn("{} is not a valid entity identifier", specificBlockObject.get("boss_entity").getAsString());
                        }
                        bossEntityType = Registry.ENTITY_TYPE.get(new Identifier(specificBlockObject.get("boss_entity").getAsString()));
                        bossBlockId = rawBlockId;
                    } else if (specificBlockObject.has("exit_block") && specificBlockObject.get("exit_block").getAsBoolean()) {
                        exitBlockId = rawBlockId;
                    } else if (specificBlockObject.has("boss_loot_block") && specificBlockObject.get("boss_loot_block").getAsBoolean()) {
                        bossLootBlockId = rawBlockId;
                        // blockIdBlockReplacement.put(rawBlockId, -1);
                        // continue;
                        // System.out.println("SET BOSS LOOT BLOCK YEJJ " + bossLootBlockId);// DBUSBDÜSUBDUSDBÜISBDÜISBDÜSBDSÜIDVBSÜVDBISVDSZVDPSVDPSVD

                    } else {
                        DungeonzMain.LOGGER.warn("{} has no set spawns nor exit block nor boss loot block nor boss entity", blockIterator);
                    }

                    if (!specificBlockObject.get("replace").isJsonNull()) {
                        Identifier blockIdentifier = new Identifier(specificBlockObject.get("replace").getAsString());
                        if (!blockIdentifier.toString().equals("minecraft:air") && Registry.BLOCK.get(blockIdentifier).toString().equals("Block{minecraft:air}")) {
                            DungeonzMain.LOGGER.warn("{} is not a valid block identifier", specificBlockObject.get("replace").getAsString());
                        }
                        blockIdBlockReplacement.put(rawBlockId, Registry.BLOCK.getRawId(Registry.BLOCK.get(blockIdentifier)));
                    } else {
                        blockIdBlockReplacement.put(rawBlockId, -1);
                    }
                }

                JsonObject spawnerObject = data.get("spawner").getAsJsonObject();
                Iterator<String> spawnerIterator = spawnerObject.keySet().iterator();

                HashMap<Integer, Integer> spawnerEntityIdCountMap = new HashMap<Integer, Integer>();

                while (spawnerIterator.hasNext()) {
                    String entityString = spawnerIterator.next();
                    Identifier entityIdentifier = new Identifier(entityString);

                    if (Registry.ENTITY_TYPE.get(entityIdentifier).toString().equals("entity.minecraft.pig")) {
                        DungeonzMain.LOGGER.warn("{} is not a valid entity identifier", entityString);
                        continue;
                    }
                    spawnerEntityIdCountMap.put(Registry.ENTITY_TYPE.getRawId(Registry.ENTITY_TYPE.get(entityIdentifier)), spawnerObject.get(entityString).getAsInt());
                }
                if (bossEntityType == null) {
                    DungeonzMain.LOGGER.warn("{} has no set boss", data);
                    return;
                }
                Dungeon.addDungeon(
                        new Dungeon(dungeonTypeId, blockIdEntityMap, blockIdEntitySpawnChance, blockIdBlockReplacement, difficultyMobModificator, difficultyLootTableIds, difficultyBossModificator,
                                difficultyBossLootTable, bossEntityType, bossBlockId, bossLootBlockId, exitBlockId, maxGroupSize, cooldown, dungeonStructurePoolId, spawnerEntityIdCountMap));
            } catch (Exception e) {
                DungeonzMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });
    }

}
