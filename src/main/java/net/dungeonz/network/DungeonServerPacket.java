package net.dungeonz.network;

import java.util.List;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class DungeonServerPacket {

    public static final Identifier DUNGEON_INFO_PACKET = new Identifier("dungeonz", "dungeon_info");

    public static void init() {
    }

    public static void writeS2CDungeonInfoPacket(ServerPlayerEntity serverPlayerEntity, List<Integer> breakableBlockIdList, List<Integer> placeableBlockIdList, boolean allowElytra) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeIntList(new IntArrayList(breakableBlockIdList));
        buf.writeIntList(new IntArrayList(placeableBlockIdList));
        buf.writeBoolean(allowElytra);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(DUNGEON_INFO_PACKET, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

}
