package net.dungeonz.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import net.dungeonz.access.ServerPlayerAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.init.DimensionInit;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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

    public static List<ItemStack> getRequiredItemStackList(Dungeon dungeon) {
        List<ItemStack> requiredItemStackList = new ArrayList<ItemStack>();
        Iterator<Entry<Integer, Integer>> requiredItemIterator = dungeon.getRequiredItemCountMap().entrySet().iterator();
        while (requiredItemIterator.hasNext()) {
            Entry<Integer, Integer> entry = requiredItemIterator.next();
            requiredItemStackList.add(new ItemStack(Registry.ITEM.get(entry.getKey()), entry.getValue()));
        }
        return requiredItemStackList;
    }

    public static Map<String, List<ItemStack>> getPossibleLootItemStackMap(Dungeon dungeon, MinecraftServer server) {
        HashMap<String, List<ItemStack>> possibleLootItemStackMap = new HashMap<String, List<ItemStack>>();
        Iterator<Entry<String, String>> lootTableIterator = dungeon.getDifficultyBossLootTableMap().entrySet().iterator();
        while (lootTableIterator.hasNext()) {
            Entry<String, String> entry = lootTableIterator.next();

            LootTable lootTable = server.getLootManager().getTable(new Identifier(entry.getValue()));

            LootContext.Builder builder = new LootContext.Builder(server.getOverworld())
                    .parameter(LootContextParameters.ORIGIN, server.getOverworld().getPlayers().get(server.getOverworld().getRandom().nextInt(server.getOverworld().getPlayers().size())).getPos())
                    .random(server.getOverworld().getRandom().nextLong());
            Inventory inventory = new SimpleInventory(27);
            lootTable.supplyInventory(inventory, builder.build(LootContextTypes.CHEST));

            List<ItemStack> itemStacks = new ArrayList<ItemStack>();
            for (int i = 0; i < inventory.size(); i++) {
                if (!inventory.getStack(i).isEmpty()) {
                    boolean contains = false;
                    for (int u = 0; u < itemStacks.size(); u++) {
                        if (ItemStack.areItemsEqual(itemStacks.get(u), inventory.getStack(i))) {
                            itemStacks.get(u).increment(inventory.getStack(i).getCount());
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        itemStacks.add(inventory.getStack(i));
                    }
                }
            }
            possibleLootItemStackMap.put(entry.getKey(), itemStacks);
        }

        return possibleLootItemStackMap;
    }

}
