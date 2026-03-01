package net.dungeonz.network.packet;

import net.dungeonz.DungeonzMain;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.List;

public record DungeonInfoPacket(List<Integer> breakableBlockIdList, List<Integer> placeableBlockIdList, boolean allowElytra) implements CustomPayload {

    public static final CustomPayload.Id<DungeonInfoPacket> PACKET_ID = new CustomPayload.Id<>(DungeonzMain.identifierOf("dungeon_info_packet"));

    public static final PacketCodec<RegistryByteBuf, DungeonInfoPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeCollection(value.breakableBlockIdList, PacketByteBuf::writeInt);
        buf.writeCollection(value.placeableBlockIdList, PacketByteBuf::writeInt);
        buf.writeBoolean(value.allowElytra);
    }, buf -> new DungeonInfoPacket(buf.readList(PacketByteBuf::readInt), buf.readList(PacketByteBuf::readInt), buf.readBoolean()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
