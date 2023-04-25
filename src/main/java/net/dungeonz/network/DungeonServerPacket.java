package net.dungeonz.network;

import java.util.List;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.dungeonz.block.DungeonPortalBlock;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class DungeonServerPacket {

    public static final Identifier DUNGEON_INFO_PACKET = new Identifier("dungeonz", "dungeon_info");
    public static final Identifier CHANGE_DUNGEON_DIFFICULTY_PACKET = new Identifier("dungeonz", "change_dungeon_difficulty");
    public static final Identifier SYNC_SCREEN_PACKET = new Identifier("dungeonz", "sync_screen");
    public static final Identifier DUNGEON_TELEPORT_PACKET = new Identifier("dungeonz", "dungeon_teleport");

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

}
