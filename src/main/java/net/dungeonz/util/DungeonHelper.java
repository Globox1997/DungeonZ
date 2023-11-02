package net.dungeonz.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import net.dungeonz.DungeonzMain;
import net.dungeonz.access.ServerPlayerAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.dungeon.DungeonPlacementHandler;
import net.dungeonz.init.DimensionInit;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.partyaddon.access.GroupManagerAccess;
import net.partyaddon.group.GroupManager;

public class DungeonHelper {

    @Nullable
    public static Dungeon getCurrentDungeon(ServerPlayerEntity playerEntity) {
        if (playerEntity.getWorld().getRegistryKey() == DimensionInit.DUNGEON_WORLD && ((ServerPlayerAccess) playerEntity).getOldServerWorld() != null) {
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
            requiredItemStackList.add(new ItemStack(Registries.ITEM.get(entry.getKey()), entry.getValue()));
        }
        return requiredItemStackList;
    }

    public static Map<String, List<ItemStack>> getPossibleLootItemStackMap(Dungeon dungeon, MinecraftServer server) {
        HashMap<String, List<ItemStack>> possibleLootItemStackMap = new HashMap<String, List<ItemStack>>();
        Iterator<Entry<String, String>> lootTableIterator = dungeon.getDifficultyBossLootTableMap().entrySet().iterator();
        while (lootTableIterator.hasNext()) {
            Entry<String, String> entry = lootTableIterator.next();

            LootTable lootTable = server.getLootManager().getLootTable(new Identifier(entry.getValue()));

            LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(server.getOverworld()).add(LootContextParameters.ORIGIN,
                    server.getOverworld().getPlayers().get(server.getOverworld().getRandom().nextInt(server.getOverworld().getPlayers().size())).getPos());
            Inventory inventory = new SimpleInventory(27);
            lootTable.supplyInventory(inventory, builder.build(LootContextTypes.CHEST), server.getOverworld().getRandom().nextLong());

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

    public static void teleportDungeon(ServerPlayerEntity player, BlockPos dungeonPortalPos, @Nullable UUID requiredMinGroupUuid) {
        if (player.getWorld().getBlockEntity(dungeonPortalPos) != null && player.getWorld().getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity) {
            DungeonPortalEntity dungeonPortalEntity = (DungeonPortalEntity) player.getWorld().getBlockEntity(dungeonPortalPos);

            if (player.getWorld().getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
                ServerWorld oldWorld = ((ServerPlayerAccess) player).getOldServerWorld();
                if (oldWorld != null) {
                    FabricDimensions.teleport(player, oldWorld, DungeonPlacementHandler.leave(player, oldWorld));
                    return;
                }
            } else {
                ServerWorld dungeonWorld = player.getWorld().getServer().getWorld(DimensionInit.DUNGEON_WORLD);
                if (dungeonWorld == null) {
                    player.sendMessage(Text.literal("Failed to find world, was it registered?"), false);
                    return;
                }
                if (dungeonPortalEntity.getDungeon() != null) {
                    if ((dungeonPortalEntity.getDungeonPlayerCount() + dungeonPortalEntity.getDeadDungeonPlayerUUIDs().size()) < dungeonPortalEntity.getMaxGroupSize()) {

                        if (dungeonPortalEntity.isOnCooldown((int) dungeonWorld.getTime())) {
                            player.sendMessage(Text.translatable("text.dungeonz.dungeon_cooldown"), false);
                            return;
                        }
                        if (dungeonPortalEntity.getDungeonPlayerCount() > 0 && dungeonPortalEntity.getPrivateGroup()) {
                            if (DungeonzMain.isPartyAddonLoaded) {
                                GroupManager groupManager = ((GroupManagerAccess) player).getGroupManager();
                                if (groupManager.getGroupPlayerIdList().isEmpty() || !groupManager.getGroupPlayerIdList().contains(dungeonPortalEntity.getDungeonPlayerUuids().get(0))) {
                                    player.sendMessage(Text.translatable("text.dungeonz.dungeon_private"), false);
                                    return;
                                }
                            } else {
                                player.sendMessage(Text.translatable("text.dungeonz.dungeon_private"), false);
                                return;
                            }
                        }
                        if (!player.isCreative()) {
                            if (InventoryHelper.hasRequiredItemStacks(player.getInventory(), DungeonHelper.getRequiredItemStackList(dungeonPortalEntity.getDungeon()))) {
                                InventoryHelper.decrementRequiredItemStacks(player.getInventory(), DungeonHelper.getRequiredItemStackList(dungeonPortalEntity.getDungeon()));
                            } else {
                                player.sendMessage(Text.translatable("text.dungeonz.missing"), false);
                                return;
                            }
                        }
                        if (dungeonPortalEntity.getDungeonPlayerCount() <= 0 && requiredMinGroupUuid != null && dungeonPortalEntity.getMinGroupSize() > 1) {
                            dungeonPortalEntity.addWaitingUuid(requiredMinGroupUuid);
                            if (dungeonPortalEntity.getMinGroupSize() > dungeonPortalEntity.getWaitingUuids().size()) {
                                player.sendMessage(Text.translatable("text.dungeonz.dungeon_min_group_size", (dungeonPortalEntity.getMinGroupSize() - dungeonPortalEntity.getWaitingUuids().size())),
                                        false);
                                return;
                            } else {
                                for (int i = 0; i < dungeonPortalEntity.getWaitingUuids().size(); i++) {
                                    if (player.getServerWorld().getPlayerByUuid(dungeonPortalEntity.getWaitingUuids().get(i)) != null) {
                                        FabricDimensions.teleport(player.getServerWorld().getPlayerByUuid(dungeonPortalEntity.getWaitingUuids().get(i)), dungeonWorld,
                                                DungeonPlacementHandler.enter((ServerPlayerEntity) player.getServerWorld().getPlayerByUuid(dungeonPortalEntity.getWaitingUuids().get(i)), dungeonWorld,
                                                        player.getServerWorld(), dungeonPortalEntity, dungeonPortalPos, dungeonPortalEntity.getDifficulty(), dungeonPortalEntity.getDisableEffects()));
                                    }
                                }
                                dungeonPortalEntity.getWaitingUuids().clear();
                            }
                        }
                        FabricDimensions.teleport(player, dungeonWorld, DungeonPlacementHandler.enter(player, dungeonWorld, player.getServerWorld(), dungeonPortalEntity, dungeonPortalPos,
                                dungeonPortalEntity.getDifficulty(), dungeonPortalEntity.getDisableEffects()));
                    } else {
                        player.sendMessage(Text.translatable("text.dungeonz.dungeon_full"), false);
                    }
                } else {
                    player.sendMessage(Text.translatable("text.dungeonz.dungeon_missing"), false);
                }
            }
        }
    }

    public static void teleportOutOfDungeon(ServerPlayerEntity player) {
        ServerWorld oldWorld = ((ServerPlayerAccess) player).getOldServerWorld();
        if (oldWorld != null) {
            FabricDimensions.teleport(player, oldWorld, DungeonPlacementHandler.leave(player, oldWorld));
        } else {
            Vec3d spawnPos = null;
            if (player.getSpawnPointPosition() != null) {
                spawnPos = new Vec3d(player.getSpawnPointPosition().getX(), player.getSpawnPointPosition().getY(), player.getSpawnPointPosition().getZ());
            } else {
                spawnPos = PlayerEntity.findRespawnPosition(player.server.getWorld(player.getSpawnPointDimension()), ((ServerPlayerAccess) player).getDungeonSpawnBlockPos(), 0.0f, true, true).get();
            }
            FabricDimensions.teleport(player, player.server.getWorld(player.getSpawnPointDimension()), new TeleportTarget(spawnPos, new Vec3d(0.0D, 0.0D, 0.0D), 0.0f, 0.0f));
        }
    }

}
