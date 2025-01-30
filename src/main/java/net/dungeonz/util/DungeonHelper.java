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
import net.dungeonz.network.DungeonServerPacket;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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
        if (playerEntity != null && playerEntity.getWorld().getRegistryKey() == DimensionInit.DUNGEON_WORLD && ((ServerPlayerAccess) playerEntity).getOldServerWorld() != null) {
            BlockEntity blockEntity = ((ServerPlayerAccess) playerEntity).getOldServerWorld().getBlockEntity(((ServerPlayerAccess) playerEntity).getDungeonPortalBlockPos());
            if (blockEntity == null) {
                return null;
            }
            if (blockEntity instanceof DungeonPortalEntity dungeonPortalEntity) {
                return dungeonPortalEntity.getDungeon();
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
            if (blockEntity instanceof DungeonPortalEntity dungeonPortalEntity) {
                return dungeonPortalEntity;
            }
        }
        return null;
    }

    public static Map<String, List<ItemStack>> getRequiredItemStackList(Dungeon dungeon) {
        Map<String, List<ItemStack>> requiredItemStackList = new HashMap<>();
        for (Entry<String, HashMap<Integer, Integer>> entry : dungeon.getDifficultyRequiredItemCountMap().entrySet()) {
            List<ItemStack> stacks = new ArrayList<>();
            for (Entry<Integer, Integer> itemIdEntry : entry.getValue().entrySet()) {
                stacks.add(new ItemStack(Registries.ITEM.get(itemIdEntry.getKey()), itemIdEntry.getValue()));
            }
            requiredItemStackList.put(entry.getKey(), stacks);
        }
        return requiredItemStackList;
    }

    public static Map<String, List<ItemStack>> getPossibleLootItemStackMap(Dungeon dungeon, MinecraftServer server) {
        HashMap<String, List<ItemStack>> possibleLootItemStackMap = new HashMap<String, List<ItemStack>>();
        for (Entry<String, String> entry : dungeon.getDifficultyBossLootTableMap().entrySet()) {
            LootTable lootTable = server.getReloadableRegistries().getLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(entry.getValue())));
            LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(server.getOverworld()).add(LootContextParameters.ORIGIN,
                    server.getOverworld().getPlayers().get(server.getOverworld().getRandom().nextInt(server.getOverworld().getPlayers().size())).getPos());
            Inventory inventory = new SimpleInventory(27);
            lootTable.supplyInventory(inventory, builder.build(LootContextTypes.CHEST), server.getOverworld().getRandom().nextLong());

            List<ItemStack> itemStacks = new ArrayList<ItemStack>();
            for (int i = 0; i < inventory.size(); i++) {
                if (!inventory.getStack(i).isEmpty()) {
                    if (inventory.getStack(i).isDamaged()) {
                        inventory.getStack(i).setDamage(0);
                    }
                    boolean contains = false;
                    for (ItemStack itemStack : itemStacks) {
                        if (ItemStack.areItemsEqual(itemStack, inventory.getStack(i))) {
                            itemStack.increment(inventory.getStack(i).getCount());
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
        if (player.getWorld().getBlockEntity(dungeonPortalPos) != null && player.getWorld().getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity dungeonPortalEntity) {

            if (player.getWorld().getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
                ServerWorld oldWorld = ((ServerPlayerAccess) player).getOldServerWorld();
                if (oldWorld != null) {
                    player.teleportTo(DungeonPlacementHandler.leave(player, oldWorld));
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
                            if (DungeonHelper.getRequiredItemStackList(dungeonPortalEntity.getDungeon()).containsKey(dungeonPortalEntity.getDifficulty())) {
                                if (InventoryHelper.hasRequiredItemStacks(player.getInventory(), DungeonHelper.getRequiredItemStackList(dungeonPortalEntity.getDungeon()).get(dungeonPortalEntity.getDifficulty()))) {
                                    InventoryHelper.decrementRequiredItemStacks(player.getInventory(), DungeonHelper.getRequiredItemStackList(dungeonPortalEntity.getDungeon()).get(dungeonPortalEntity.getDifficulty()));
                                } else {
                                    player.sendMessage(Text.translatable("text.dungeonz.missing"), false);
                                    return;
                                }
                            }
                        }
                        if (!dungeonPortalEntity.getWaitingUuids().isEmpty() && dungeonPortalEntity.getWaitingUuids().contains(player.getUuid())) {
                            player.closeHandledScreen();
                            return;
                        }
                        if (dungeonPortalEntity.getDungeonPlayerCount() <= 0 && requiredMinGroupUuid != null && dungeonPortalEntity.getMinGroupSize() > 1) {
                            dungeonPortalEntity.addWaitingUuid(requiredMinGroupUuid);
                            if (dungeonPortalEntity.getMinGroupSize() > dungeonPortalEntity.getWaitingUuids().size()) {
                                player.sendMessage(Text.translatable("text.dungeonz.dungeon_min_group_size", (dungeonPortalEntity.getMinGroupSize() - dungeonPortalEntity.getWaitingUuids().size())),
                                        false);
                                return;
                            } else if (dungeonPortalEntity.getdungeonTeleportCountdown() <= 0) {
                                dungeonPortalEntity.startDungeonTeleportCountdown(dungeonWorld);
                                player.closeHandledScreen();
                            }
                        } else if (dungeonPortalEntity.getDungeonPlayerCount() <= 0 && dungeonPortalEntity.getdungeonTeleportCountdown() <= 0) {
                            dungeonPortalEntity.addWaitingUuid(requiredMinGroupUuid);
                            dungeonPortalEntity.startDungeonTeleportCountdown(dungeonWorld);
                            player.closeHandledScreen();
                        } else if (dungeonPortalEntity.getdungeonTeleportCountdown() > 0) {
                            dungeonPortalEntity.addWaitingUuid(requiredMinGroupUuid);
                            player.closeHandledScreen();
                        } else if (!dungeonPortalEntity.getDeadDungeonPlayerUUIDs().contains(player.getUuid()) || dungeonPortalEntity.getDungeon().isRespawnAllowed()) {
                            teleportPlayer(player, dungeonWorld, dungeonPortalEntity, dungeonPortalPos);
                        } else {
                            player.sendMessage(Text.translatable("text.dungeonz.dead_player"), false);
                            player.closeHandledScreen();
                        }
                    } else {
                        player.sendMessage(Text.translatable("text.dungeonz.dungeon_full"), false);
                    }
                } else {
                    player.sendMessage(Text.translatable("text.dungeonz.dungeon_missing"), false);
                }
            }
        }
    }

    public static void teleportPlayer(ServerPlayerEntity serverPlayerEntity, ServerWorld dungeonWorld, DungeonPortalEntity dungeonPortalEntity, BlockPos dungeonPortalPos) {
        ServerPlayerEntity playerEntity = (ServerPlayerEntity) serverPlayerEntity.teleportTo(DungeonPlacementHandler.enter(serverPlayerEntity, dungeonWorld, serverPlayerEntity.getServerWorld(),
                dungeonPortalEntity, dungeonPortalPos, dungeonPortalEntity.getDifficulty(), dungeonPortalEntity.getDisableEffects()));

        DungeonServerPacket.writeS2CDungeonInfoPacket(playerEntity, dungeonPortalEntity.getDungeon().getBreakableBlockIdList(), dungeonPortalEntity.getDungeon().getplaceableBlockIdList(),
                dungeonPortalEntity.getDungeon().isElytraAllowed());
    }

    public static void teleportOutOfDungeon(ServerPlayerEntity player) {
        ServerWorld oldWorld = ((ServerPlayerAccess) player).getOldServerWorld();
        if (oldWorld != null) {
            player.teleportTo(DungeonPlacementHandler.leave(player, oldWorld));
        } else {
            Vec3d spawnPos = null;
            if (player.getSpawnPointPosition() != null) {
                spawnPos = new Vec3d(player.getSpawnPointPosition().getX(), player.getSpawnPointPosition().getY(), player.getSpawnPointPosition().getZ());
            } else {
                // spawnPos = ServerPlayerEntity.findRespawnPosition(player.server.getWorld(player.getSpawnPointDimension()), ((ServerPlayerAccess) player).getDungeonSpawnBlockPos(), 0.0f, true,
                // true).get();
                player.teleportTo(player.getRespawnTarget(true, TeleportTarget.NO_OP));
            }
            player.teleportTo(new TeleportTarget(player.server.getWorld(player.getSpawnPointDimension()), spawnPos, new Vec3d(0.0D, 0.0D, 0.0D), 0.0f, 0.0f, TeleportTarget.NO_OP));
        }
    }

}
