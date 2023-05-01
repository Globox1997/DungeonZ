package net.dungeonz.network;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.dungeonz.block.DungeonPortalBlock;
import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dungeon.Dungeon;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class DungeonServerPacket {

    public static final Identifier DUNGEON_INFO_PACKET = new Identifier("dungeonz", "dungeon_info");

    public static final Identifier DUNGEON_TELEPORT_PACKET = new Identifier("dungeonz", "dungeon_teleport");

    public static final Identifier CHANGE_DUNGEON_DIFFICULTY_PACKET = new Identifier("dungeonz", "change_dungeon_difficulty");
    public static final Identifier CHANGE_DUNGEON_EFFECTS_PACKET = new Identifier("dungeonz", "change_dungeon_effects");
    public static final Identifier CHANGE_DUNGEON_PRIVATE_GROUP_PACKET = new Identifier("dungeonz", "change_dungeon_private_group");

    public static final Identifier SET_DUNGEON_TYPE_PACKET = new Identifier("dungeonz", "set_dungeon_type");
    public static final Identifier SET_GATE_BLOCK_PACKET = new Identifier("dungeonz", "set_gate_block");
    public static final Identifier SYNC_GATE_BLOCK_PACKET = new Identifier("dungeonz", "sync_gate_block");

    public static final Identifier SYNC_SCREEN_PACKET = new Identifier("dungeonz", "sync_screen");
    public static final Identifier OP_SCREEN_PACKET = new Identifier("dungeonz", "op_screen");

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(CHANGE_DUNGEON_DIFFICULTY_PACKET, (server, player, handler, buffer, sender) -> {
            BlockPos dungeonPortalPos = buffer.readBlockPos();
            server.execute(() -> {
                if (player.world.getBlockEntity(dungeonPortalPos) != null && player.world.getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity) {
                    DungeonPortalEntity dungeonPortalEntity = (DungeonPortalEntity) player.world.getBlockEntity(dungeonPortalPos);

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
                        writeS2CSyncScreenPacket(player, dungeonPortalEntity);
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DUNGEON_TELEPORT_PACKET, (server, player, handler, buffer, sender) -> {
            BlockPos dungeonPortalPos = buffer.readBlockPos();
            server.execute(() -> {
                DungeonPortalBlock.teleportDungeon(player, dungeonPortalPos);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(CHANGE_DUNGEON_EFFECTS_PACKET, (server, player, handler, buffer, sender) -> {
            BlockPos dungeonPortalPos = buffer.readBlockPos();
            boolean disableEffects = buffer.readBoolean();
            server.execute(() -> {
                if (player.world.getBlockEntity(dungeonPortalPos) != null && player.world.getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity) {
                    DungeonPortalEntity dungeonPortalEntity = (DungeonPortalEntity) player.world.getBlockEntity(dungeonPortalPos);

                    if (dungeonPortalEntity.getDungeonPlayerCount() == 0) {
                        dungeonPortalEntity.setDisableEffects(disableEffects);
                        dungeonPortalEntity.markDirty();
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(CHANGE_DUNGEON_PRIVATE_GROUP_PACKET, (server, player, handler, buffer, sender) -> {
            BlockPos dungeonPortalPos = buffer.readBlockPos();
            boolean privateGroup = buffer.readBoolean();
            server.execute(() -> {
                if (player.world.getBlockEntity(dungeonPortalPos) != null && player.world.getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity) {
                    DungeonPortalEntity dungeonPortalEntity = (DungeonPortalEntity) player.world.getBlockEntity(dungeonPortalPos);

                    if (dungeonPortalEntity.getDungeonPlayerCount() == 0) {
                        dungeonPortalEntity.setPrivateGroup(privateGroup);
                        dungeonPortalEntity.markDirty();
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(SET_DUNGEON_TYPE_PACKET, (server, player, handler, buffer, sender) -> {
            BlockPos dungeonPortalPos = buffer.readBlockPos();
            String dungeonType = buffer.readString();
            String defaultDifficulty = buffer.readString();
            server.execute(() -> {
                if (player.isCreativeLevelTwoOp()) {
                    if (Dungeon.getDungeon(dungeonType) != null) {
                        Dungeon dungeon = Dungeon.getDungeon(dungeonType);
                        if (dungeon.getDifficultyList().contains(defaultDifficulty)) {
                            if (player.world.getBlockEntity(dungeonPortalPos) != null && player.world.getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity) {
                                DungeonPortalEntity dungeonPortalEntity = (DungeonPortalEntity) player.world.getBlockEntity(dungeonPortalPos);
                                dungeonPortalEntity.setDungeonType(dungeonType);
                                dungeonPortalEntity.setDifficulty(defaultDifficulty);
                                dungeonPortalEntity.setMaxGroupSize(dungeon.getMaxGroupSize());
                                dungeonPortalEntity.markDirty();
                                player.sendMessage(Text.of("Set dungeon type successfully!"), false);
                                // player.openHandledScreen(dungeonPortalEntity.getCachedState().createScreenHandlerFactory(player.world, dungeonPortalPos));
                                return;
                            }
                        } else {
                            player.sendMessage(Text.of("Failed to set dungeon type cause difficulty " + defaultDifficulty + " does not exist in type " + dungeonType + "!"), false);
                        }
                    } else {
                        player.sendMessage(Text.of("Failed to set dungeon type cause " + dungeonType + " does not exist!"), false);
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(SET_GATE_BLOCK_PACKET, (server, player, handler, buffer, sender) -> {
            BlockPos gatePos = buffer.readBlockPos();
            String blockId = buffer.readString();
            String particleId = buffer.readString();
            String unlockItemId = buffer.readString();
            server.execute(() -> {
                if (player.isCreativeLevelTwoOp()) {
                    if (player.world.getBlockEntity(gatePos) != null && player.world.getBlockEntity(gatePos) instanceof DungeonGateEntity) {
                        List<BlockPos> otherDungeonGatesPosList = DungeonGateEntity.getConnectedDungeonGatePosList(player.world, gatePos);
                        for (int i = 0; i < otherDungeonGatesPosList.size(); i++) {
                            if (player.world.getBlockEntity(otherDungeonGatesPosList.get(i)) != null && player.world.getBlockEntity(otherDungeonGatesPosList.get(i)) instanceof DungeonGateEntity) {
                                DungeonGateEntity otherDungeonGateEntity = (DungeonGateEntity) player.world.getBlockEntity(otherDungeonGatesPosList.get(i));
                                otherDungeonGateEntity.setBlockId(new Identifier(blockId));
                                otherDungeonGateEntity.setParticleEffectId(particleId);
                                otherDungeonGateEntity.setUnlockItemId(unlockItemId);
                                otherDungeonGateEntity.markDirty();
                            }
                        }
                        writeS2CSyncGatePacket(player, (DungeonGateEntity) player.world.getBlockEntity(gatePos), otherDungeonGatesPosList);
                    }
                }
            });
        });
    }

    public static void writeS2CDungeonInfoPacket(ServerPlayerEntity serverPlayerEntity, List<Integer> breakableBlockIdList, List<Integer> placeableBlockIdList, boolean allowElytra) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeIntList(new IntArrayList(breakableBlockIdList));
        buf.writeIntList(new IntArrayList(placeableBlockIdList));
        buf.writeBoolean(allowElytra);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(DUNGEON_INFO_PACKET, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void writeS2CSyncScreenPacket(ServerPlayerEntity serverPlayerEntity, DungeonPortalEntity dungeonPortalEntity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(dungeonPortalEntity.getPos());
        buf.writeString(dungeonPortalEntity.getDifficulty());

        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(SYNC_SCREEN_PACKET, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void writeS2COpenOpScreenPacket(ServerPlayerEntity serverPlayerEntity, @Nullable DungeonPortalEntity dungeonPortalEntity, @Nullable DungeonGateEntity dungeonGateEntity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        if (dungeonPortalEntity != null) {
            buf.writeString("portal");
            buf.writeBlockPos(dungeonPortalEntity.getPos());
            buf.writeString(dungeonPortalEntity.getDungeonType());
            buf.writeString(dungeonPortalEntity.getDifficulty());
        }
        if (dungeonGateEntity != null) {
            buf.writeString("gate");
            buf.writeBlockPos(dungeonGateEntity.getPos());
            buf.writeString(Registry.BLOCK.getId(dungeonGateEntity.getBlockState().getBlock()).toString());
            buf.writeString(dungeonGateEntity.getParticleEffect() != null ? dungeonGateEntity.getParticleEffect().asString() : "");
            buf.writeString(dungeonGateEntity.getUnlockItem() != null ? Registry.ITEM.getId(dungeonGateEntity.getUnlockItem()).toString() : "");
        }
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(OP_SCREEN_PACKET, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void writeS2CSyncGatePacket(ServerPlayerEntity serverPlayerEntity, DungeonGateEntity dungeonGateEntity, List<BlockPos> dungeonGatesPosList) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(dungeonGatesPosList.size());
        for (int i = 0; i < dungeonGatesPosList.size(); i++) {
            buf.writeBlockPos(dungeonGatesPosList.get(i));
        }
        buf.writeString(Registry.BLOCK.getId(dungeonGateEntity.getBlockState().getBlock()).toString());
        buf.writeString(dungeonGateEntity.getParticleEffect() != null ? dungeonGateEntity.getParticleEffect().asString() : "");
        buf.writeString(dungeonGateEntity.getUnlockItem() != null ? Registry.ITEM.getId(dungeonGateEntity.getUnlockItem()).toString() : "");

        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(SYNC_GATE_BLOCK_PACKET, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

}
