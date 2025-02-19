package net.dungeonz.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dungeonz.DungeonzMain;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.init.ConfigInit;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DungeonLoader implements SimpleSynchronousResourceReloadListener {

    @Override
    public Identifier getFabricId() {
        return Identifier.of("dungeonz", "dungeon_loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        manager.findResources("dungeon", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                String dungeonTypeId = data.get("dungeon_type").getAsString();
                if (!ConfigInit.CONFIG.defaultDungeons && (dungeonTypeId.equals("dark_dungeon") || dungeonTypeId.equals("temple_dungeon"))) {
                    return;
                }
                int maxGroupSize = data.get("max_group_size").getAsInt();
                int minGroupSize = data.has("min_group_size") ? data.get("min_group_size").getAsInt() : 0;
                int requiredLevel = data.has("required_level") ? data.get("required_level").getAsInt() : 0;
                int cooldown = data.get("cooldown").getAsInt();
                boolean allowElytra = data.has("elytra") ? data.get("elytra").getAsBoolean() : false;
                boolean allowRespawn = data.has("respawn") ? data.get("respawn").getAsBoolean() : true;
                boolean keepInventory = data.has("keep_inventory") ? data.get("keep_inventory").getAsBoolean() : false;
                Identifier dungeonBackgroundId = data.has("background_texture") && !data.get("background_texture").getAsString().isEmpty() ? Identifier.of(data.get("background_texture").getAsString()) : null;
                Identifier dungeonStructurePoolId = Identifier.of(data.get("dungeon_structure_pool_id").getAsString());

                List<String> difficulties = new ArrayList<String>();
                JsonObject difficultyObject = data.get("difficulty").getAsJsonObject();
                Iterator<String> difficultyIterator = difficultyObject.keySet().iterator();

                HashMap<String, Float> difficultyMobModificator = new HashMap<String, Float>();
                HashMap<String, List<String>> difficultyLootTableIds = new HashMap<String, List<String>>();
                HashMap<String, Float> difficultyBossModificator = new HashMap<String, Float>();
                HashMap<String, String> difficultyBossLootTable = new HashMap<String, String>();

                while (difficultyIterator.hasNext()) {
                    String difficulty = difficultyIterator.next();
                    difficulties.add(difficulty);
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
                HashMap<Integer, HashMap<String, Float>> blockIdEntitySpawnChance = new HashMap<Integer, HashMap<String, Float>>();
                HashMap<Integer, Integer> blockIdBlockReplacement = new HashMap<Integer, Integer>();
                int bossBlockId = -1;
                int bossLootBlockId = -1;
                int exitBlockId = -1;
                EntityType<?> bossEntityType = null;
                NbtCompound bossNbtCompound = null;

                while (blockIterator.hasNext()) {
                    String block = blockIterator.next();
                    if (Registries.BLOCK.get(Identifier.of(block)).toString().equals("Block{minecraft:air}")) {
                        DungeonzMain.LOGGER.warn("{} is not a valid block identifier", block);
                        continue;
                    }
                    int rawBlockId = Registries.BLOCK.getRawId(Registries.BLOCK.get(Identifier.of(block)));

                    JsonObject specificBlockObject = blockObject.get(block).getAsJsonObject();

                    if (specificBlockObject.has("spawns")) {
                        List<EntityType<?>> entityTypes = new ArrayList<EntityType<?>>();
                        for (int i = 0; i < specificBlockObject.get("spawns").getAsJsonArray().size(); i++) {
                            if (!Registries.ENTITY_TYPE.containsId(Identifier.of(specificBlockObject.get("spawns").getAsJsonArray().get(i).getAsString()))) {
                                DungeonzMain.LOGGER.warn("{} is not a valid entity identifier", specificBlockObject.get("spawns").getAsJsonArray().get(i).getAsString());
                                continue;
                            }
                            entityTypes.add(Registries.ENTITY_TYPE.get(Identifier.of(specificBlockObject.get("spawns").getAsJsonArray().get(i).getAsString())));
                        }
                        blockIdEntityMap.put(rawBlockId, entityTypes);

                        HashMap<String, Float> difficultyChance = new HashMap<String, Float>();
                        for (String difficulty : difficulties) {
                            difficultyChance.put(difficulty, specificBlockObject.get("chance").getAsJsonObject().get(difficulty).getAsFloat());
                        }
                        blockIdEntitySpawnChance.put(rawBlockId, difficultyChance);

                    } else if (specificBlockObject.has("boss_entity")) {
                        if (!Registries.ENTITY_TYPE.containsId(Identifier.of(specificBlockObject.get("boss_entity").getAsString()))) {
                            DungeonzMain.LOGGER.warn("{} is not a valid entity identifier", specificBlockObject.get("boss_entity").getAsString());
                        }
                        bossEntityType = Registries.ENTITY_TYPE.get(Identifier.of(specificBlockObject.get("boss_entity").getAsString()));
                        bossNbtCompound = tryReadNbtData(specificBlockObject);
                        bossBlockId = rawBlockId;
                    } else if (specificBlockObject.has("exit_block") && specificBlockObject.get("exit_block").getAsBoolean()) {
                        exitBlockId = rawBlockId;
                    } else if (specificBlockObject.has("boss_loot_block") && specificBlockObject.get("boss_loot_block").getAsBoolean()) {
                        bossLootBlockId = rawBlockId;
                    } else {
                        DungeonzMain.LOGGER.warn("{} has no set spawns nor exit block nor boss loot block nor boss entity", blockIterator);
                    }

                    if (!specificBlockObject.get("replace").isJsonNull()) {
                        Identifier blockIdentifier = Identifier.of(specificBlockObject.get("replace").getAsString());
                        if (!blockIdentifier.toString().equals("minecraft:air") && Registries.BLOCK.get(blockIdentifier).toString().equals("Block{minecraft:air}")) {
                            DungeonzMain.LOGGER.warn("{} is not a valid block identifier", specificBlockObject.get("replace").getAsString());
                            continue;
                        }
                        blockIdBlockReplacement.put(rawBlockId, Registries.BLOCK.getRawId(Registries.BLOCK.get(blockIdentifier)));
                    } else {
                        blockIdBlockReplacement.put(rawBlockId, -1);
                    }
                }

                JsonObject spawnerObject = data.get("spawner").getAsJsonObject();
                Iterator<String> spawnerIterator = spawnerObject.keySet().iterator();

                HashMap<Integer, Integer> spawnerEntityIdCountMap = new HashMap<Integer, Integer>();

                while (spawnerIterator.hasNext()) {
                    String entityString = spawnerIterator.next();
                    Identifier entityIdentifier = Identifier.of(entityString);

                    if (Registries.ENTITY_TYPE.get(entityIdentifier).toString().equals("entity.minecraft.pig")) {
                        DungeonzMain.LOGGER.warn("{} is not a valid entity identifier", entityString);
                        continue;
                    }
                    spawnerEntityIdCountMap.put(Registries.ENTITY_TYPE.getRawId(Registries.ENTITY_TYPE.get(entityIdentifier)), spawnerObject.get(entityString).getAsInt());
                }
                List<Integer> breakableBlockIds = new ArrayList<Integer>();
                if (data.has("breakable")) {
                    for (int i = 0; i < data.get("breakable").getAsJsonArray().size(); i++) {
                        Identifier blockIdentifier = Identifier.of(data.get("breakable").getAsJsonArray().get(i).getAsString());
                        if (Registries.BLOCK.get(blockIdentifier).toString().equals("Block{minecraft:air}")) {
                            DungeonzMain.LOGGER.warn("{} is not a valid block identifier", data.get("breakable").getAsJsonArray().get(i).getAsString());
                            continue;
                        }
                        breakableBlockIds.add(Registries.BLOCK.getRawId(Registries.BLOCK.get(blockIdentifier)));
                    }
                }
                List<Integer> placeableBlockIds = new ArrayList<Integer>();
                if (data.has("placeable")) {
                    for (int i = 0; i < data.get("placeable").getAsJsonArray().size(); i++) {
                        Identifier blockIdentifier = Identifier.of(data.get("placeable").getAsJsonArray().get(i).getAsString());
                        if (Registries.BLOCK.get(blockIdentifier).toString().equals("Block{minecraft:air}")) {
                            DungeonzMain.LOGGER.warn("{} is not a valid block identifier", data.get("placeable").getAsJsonArray().get(i).getAsString());
                            continue;
                        }
                        placeableBlockIds.add(Registries.BLOCK.getRawId(Registries.BLOCK.get(blockIdentifier)));
                    }
                }

                JsonObject requiredObject = data.get("required").getAsJsonObject();
                Iterator<String> requiredIterator = requiredObject.keySet().iterator();

                HashMap<String, HashMap<Integer, Integer>> difficultyRequiredItemCountMap = new HashMap<>();

                while (requiredIterator.hasNext()) {
                    String difficulty = requiredIterator.next();
                    if (!difficulties.contains(difficulty)) {
                        DungeonzMain.LOGGER.warn("{} is not a valid difficulty at the required list", difficulty);
                        continue;
                    }

                    HashMap<Integer, Integer> requiredItemCountMap = new HashMap<>();

                    for (String itemString : requiredObject.get(difficulty).getAsJsonObject().keySet()) {
                        Identifier itemIdentifier = Identifier.of(itemString);
                        if (Registries.ITEM.get(itemIdentifier).toString().equals("air")) {
                            DungeonzMain.LOGGER.warn("{} is not a valid item identifier", itemString);
                            continue;
                        }
                        requiredItemCountMap.put(Registries.ITEM.getRawId(Registries.ITEM.get(itemIdentifier)), requiredObject.get(difficulty).getAsJsonObject().get(itemString).getAsInt());
                    }
                    difficultyRequiredItemCountMap.put(difficulty, requiredItemCountMap);
                }
                for (String difficulty : difficulties) {
                    if (!difficultyRequiredItemCountMap.containsKey(difficulty)) {
                        difficultyRequiredItemCountMap.put(difficulty, new HashMap<>());
                    }
                }

                if (bossEntityType == null) {
                    DungeonzMain.LOGGER.error("{} has no set boss", data);
                    return;
                }

                Dungeon.addDungeon(new Dungeon(dungeonTypeId, blockIdEntityMap, blockIdEntitySpawnChance, blockIdBlockReplacement, spawnerEntityIdCountMap, difficultyRequiredItemCountMap, breakableBlockIds,
                        placeableBlockIds, difficultyMobModificator, difficultyLootTableIds, difficultyBossModificator, difficultyBossLootTable, bossEntityType, bossNbtCompound, bossBlockId,
                        bossLootBlockId, exitBlockId, allowRespawn, allowElytra, keepInventory, maxGroupSize, minGroupSize, requiredLevel, cooldown, dungeonBackgroundId, dungeonStructurePoolId));
            } catch (Exception e) {
                DungeonzMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });
    }

    @Nullable
    private static NbtCompound tryReadNbtData(JsonObject json) {
        if (json.has("data") && json.get("data") != null && !json.get("data").getAsString().equals("")) {
            try {
                return new StringNbtReader(new StringReader(json.get("data").getAsString())).parseCompound();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
                throw new JsonParseException("Failed to load nbt data of json object " + json);
            }
        }
        return null;
    }

}
