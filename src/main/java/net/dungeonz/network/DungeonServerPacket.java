package net.dungeonz.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import net.dungeonz.block.DungeonPortalBlock;
import org.jetbrains.annotations.Nullable;

import net.dungeonz.DungeonzMain;
import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.init.ItemInit;
import net.dungeonz.item.DungeonCompassItem;
import net.dungeonz.network.packet.*;
import net.dungeonz.util.DungeonHelper;
import net.dungeonz.util.InventoryHelper;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class DungeonServerPacket {

    public static void init() {
        PayloadTypeRegistry.playS2C().register(DungeonInfoPacket.PACKET_ID, DungeonInfoPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(DungeonTeleportCountdownPacket.PACKET_ID, DungeonTeleportCountdownPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(DungeonSyncGatePacket.PACKET_ID, DungeonSyncGatePacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(DungeonSyncScreenPacket.PACKET_ID, DungeonSyncScreenPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(DungeonOpScreenPacket.PACKET_ID, DungeonOpScreenPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(DungeonCompassScreenPacket.PACKET_ID, DungeonCompassScreenPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(DungeonPortalPacket.PACKET_ID, DungeonPortalPacket.PACKET_CODEC);

        PayloadTypeRegistry.playC2S().register(DungeonDifficultyPacket.PACKET_ID, DungeonDifficultyPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(DungeonGroupPacket.PACKET_ID, DungeonGroupPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(DungeonTeleportPacket.PACKET_ID, DungeonTeleportPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(DungeonTypePacket.PACKET_ID, DungeonTypePacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(DungeonCompassPacket.PACKET_ID, DungeonCompassPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(DungeonGatePacket.PACKET_ID, DungeonGatePacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DungeonDifficultyPacket.PACKET_ID, (payload, context) -> {
            BlockPos dungeonPortalPos = payload.portalBlockPos();
            context.server().execute(() -> {
                if (context.player().getWorld().getBlockEntity(dungeonPortalPos) != null && context.player().getWorld().getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity dungeonPortalEntity) {

                    if (dungeonPortalEntity.getDungeonPlayerCount() == 0) {
                        List<String> difficulties = dungeonPortalEntity.getDungeon().getDifficultyList();
                        if (dungeonPortalEntity.getDifficulty().equals("")) {
                            dungeonPortalEntity.setDifficulty(difficulties.get(0));
                        } else {
                            int index = difficulties.indexOf(dungeonPortalEntity.getDifficulty()) + 1;
                            if (index >= difficulties.size()) {
                                index = 0;
                            }
                            dungeonPortalEntity.setDifficulty(difficulties.get(index));
                        }
                        dungeonPortalEntity.markDirty();
                        writeS2CSyncScreenPacket(context.player(), dungeonPortalEntity);
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DungeonTeleportPacket.PACKET_ID, (payload, context) -> {
            BlockPos dungeonPortalPos = payload.dungeonPortalPos();
            // Boolean isMinGroupRequired = payload.isMinGroupRequired();
            UUID uuid = payload.uuid();
            context.server().execute(() -> {
                DungeonHelper.teleportDungeon(context.player(), dungeonPortalPos, uuid);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DungeonGroupPacket.PACKET_ID, (payload, context) -> {
            BlockPos dungeonPortalPos = payload.portalBlockPos();
            boolean privateGroup = payload.privateGroup();
            context.server().execute(() -> {
                if (context.player().getWorld().getBlockEntity(dungeonPortalPos) != null
                        && context.player().getWorld().getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity dungeonPortalEntity) {

                    if (dungeonPortalEntity.getDungeonPlayerCount() == 0) {
                        dungeonPortalEntity.setPrivateGroup(privateGroup);
                        dungeonPortalEntity.markDirty();
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DungeonTypePacket.PACKET_ID, (payload, context) -> {
            BlockPos dungeonPortalPos = payload.portalBlockPos();
            String dungeonType = payload.dungeonType();
            String defaultDifficulty = payload.defaultDifficulty();
            context.server().execute(() -> {
                if (context.player().isCreativeLevelTwoOp()) {
                    if (Dungeon.getDungeon(dungeonType) != null) {
                        Dungeon dungeon = Dungeon.getDungeon(dungeonType);
                        if (dungeon.getDifficultyList().contains(defaultDifficulty)) {
                            BlockPos pos = dungeonPortalPos;
                            if (DungeonPortalBlock.isOtherDungeonPortalBlockNearby(context.player().getWorld(), dungeonPortalPos)) {
                                pos = DungeonPortalBlock.getMainDungeonPortalBlockPos(context.player().getWorld(), pos);
                            }
                            if (context.player().getWorld().getBlockEntity(pos) != null
                                    && context.player().getWorld().getBlockEntity(pos) instanceof DungeonPortalEntity dungeonPortalEntity) {
                                dungeonPortalEntity.setDungeonType(dungeonType);
                                dungeonPortalEntity.setDifficulty(defaultDifficulty);
                                dungeonPortalEntity.setMaxGroupSize(dungeon.getMaxGroupSize());
                                dungeonPortalEntity.setMinGroupSize(dungeon.getMinGroupSize());
                                dungeonPortalEntity.markDirty();
                                context.player().sendMessage(Text.of("Set dungeon type successfully!"), false);
                                return;
                            }
                        } else {
                            context.player().sendMessage(Text.of("Failed to set dungeon type cause difficulty " + defaultDifficulty + " does not exist in type " + dungeonType + "!"), false);
                        }
                    } else {
                        context.player().sendMessage(Text.of("Failed to set dungeon type cause " + dungeonType + " does not exist!"), false);
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DungeonGatePacket.PACKET_ID, (payload, context) -> {
            BlockPos gatePos = payload.portalBlockPos();
            String blockId = payload.blockId();
            String particleId = payload.particleId();
            String unlockItemId = payload.unlockItemId();
            context.server().execute(() -> {
                if (context.player().isCreativeLevelTwoOp()) {
                    if (context.player().getWorld().getBlockEntity(gatePos) != null && context.player().getWorld().getBlockEntity(gatePos) instanceof DungeonGateEntity) {
                        List<BlockPos> otherDungeonGatesPosList = DungeonGateEntity.getConnectedDungeonGatePosList(context.player().getWorld(), gatePos);
                        for (int i = 0; i < otherDungeonGatesPosList.size(); i++) {
                            if (context.player().getWorld().getBlockEntity(otherDungeonGatesPosList.get(i)) != null
                                    && context.player().getWorld().getBlockEntity(otherDungeonGatesPosList.get(i)) instanceof DungeonGateEntity otherDungeonGateEntity) {
                                otherDungeonGateEntity.setBlockId(Identifier.of(blockId));
                                otherDungeonGateEntity.setParticleEffectId(particleId);
                                otherDungeonGateEntity.setUnlockItemId(unlockItemId);
                                otherDungeonGateEntity.markDirty();
                            }
                        }
                        writeS2CSyncGatePacket(context.player(), (DungeonGateEntity) context.player().getWorld().getBlockEntity(gatePos), otherDungeonGatesPosList);
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DungeonCompassPacket.PACKET_ID, (payload, context) -> {
            String dungeonType = payload.dungeonType();
            context.server().execute(() -> {
                if (context.player().getMainHandStack().isOf(ItemInit.DUNGEON_COMPASS)
                        && InventoryHelper.hasRequiredItemStacks(context.player().getInventory(), ItemInit.REQUIRED_DUNGEON_COMPASS_CALIBRATION_ITEMS)) {
                    InventoryHelper.decrementRequiredItemStacks(context.player().getInventory(), ItemInit.REQUIRED_DUNGEON_COMPASS_CALIBRATION_ITEMS);
                    DungeonCompassItem.setCompassDungeonStructure((ServerWorld) context.player().getWorld(), context.player().getBlockPos(), context.player().getMainHandStack(), dungeonType);
                }
            });
        });
    }

    public static void writeS2CDungeonInfoPacket(ServerPlayerEntity serverPlayerEntity, List<Integer> breakableBlockIdList, List<Integer> placeableBlockIdList, boolean allowElytra) {
        ServerPlayNetworking.send(serverPlayerEntity, new DungeonInfoPacket(breakableBlockIdList, placeableBlockIdList, allowElytra));
    }

    public static void writeS2CSyncScreenPacket(ServerPlayerEntity serverPlayerEntity, DungeonPortalEntity dungeonPortalEntity) {
        ServerPlayNetworking.send(serverPlayerEntity, new DungeonSyncScreenPacket(dungeonPortalEntity.getPos(), dungeonPortalEntity.getDifficulty()));
    }

    public static void writeS2COpenOpScreenPacket(ServerPlayerEntity serverPlayerEntity, @Nullable DungeonPortalEntity dungeonPortalEntity, @Nullable DungeonGateEntity dungeonGateEntity) {
        BlockPos blockPos = null;
        String blockIdOrDungeonType = "";
        String particleEffectOrDifficulty = "";
        String unlockItem = "";
        if (dungeonPortalEntity != null) {
            blockPos = dungeonPortalEntity.getPos();
            blockIdOrDungeonType = dungeonPortalEntity.getDungeonType();
            particleEffectOrDifficulty = dungeonPortalEntity.getDifficulty();
        }
        if (dungeonGateEntity != null) {
            blockPos = dungeonGateEntity.getPos();
            blockIdOrDungeonType = Registries.BLOCK.getId(dungeonGateEntity.getBlockState().getBlock()).toString();
            particleEffectOrDifficulty = dungeonGateEntity.getParticleEffect() != null ? Registries.PARTICLE_TYPE.getId(dungeonGateEntity.getParticleEffect().getType()).toString() : "";
            unlockItem = dungeonGateEntity.getUnlockItem() != null ? Registries.ITEM.getId(dungeonGateEntity.getUnlockItem()).toString() : "";
        }

        ServerPlayNetworking.send(serverPlayerEntity, new DungeonOpScreenPacket(blockPos, blockIdOrDungeonType, particleEffectOrDifficulty, unlockItem));
    }

    public static void writeS2COpenCompassScreenPacket(ServerPlayerEntity serverPlayerEntity, String dungeonType) {
        List<String> dungeonIdList = new ArrayList<String>();
        for (int i = 0; i < DungeonzMain.DUNGEONS.size(); i++) {
            dungeonIdList.add(DungeonzMain.DUNGEONS.get(i).getDungeonTypeId());
        }
        ServerPlayNetworking.send(serverPlayerEntity, new DungeonCompassScreenPacket(dungeonType, dungeonIdList));
    }

    public static void writeS2CSyncGatePacket(ServerPlayerEntity serverPlayerEntity, DungeonGateEntity dungeonGateEntity, List<BlockPos> dungeonGatesPosList) {
        ServerPlayNetworking.send(serverPlayerEntity,
                new DungeonSyncGatePacket(new HashSet<BlockPos>(dungeonGatesPosList), Registries.BLOCK.getId(dungeonGateEntity.getBlockState().getBlock()).toString(),
                        dungeonGateEntity.getParticleEffect() != null ? Registries.PARTICLE_TYPE.getId(dungeonGateEntity.getParticleEffect().getType()).toString() : "",
                        dungeonGateEntity.getUnlockItem() != null ? Registries.ITEM.getId(dungeonGateEntity.getUnlockItem()).toString() : ""));
    }

    public static void writeS2CDungeonTeleportCountdown(ServerPlayerEntity serverPlayerEntity, int countdownTicks) {
        ServerPlayNetworking.send(serverPlayerEntity, new DungeonTeleportCountdownPacket(countdownTicks));
    }

}
