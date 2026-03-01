package net.dungeonz.network.packet;

import net.dungeonz.DungeonzMain;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.List;

public record DungeonCompassScreenPacket(String dungeonType, List<String> dungeonIdList) implements CustomPayload {

    public static final CustomPayload.Id<DungeonCompassScreenPacket> PACKET_ID = new CustomPayload.Id<>(DungeonzMain.identifierOf("dungeon_compass_screen_packet"));

    public static final PacketCodec<RegistryByteBuf, DungeonCompassScreenPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeString(value.dungeonType);
        buf.writeCollection(value.dungeonIdList, PacketByteBuf::writeString);
    }, buf -> new DungeonCompassScreenPacket(buf.readString(), buf.readList(PacketByteBuf::readString)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
