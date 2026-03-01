package net.dungeonz.network.packet;

import net.dungeonz.DungeonzMain;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record DungeonCompassPacket(String dungeonType) implements CustomPayload {

    public static final CustomPayload.Id<DungeonCompassPacket> PACKET_ID = new CustomPayload.Id<>(DungeonzMain.identifierOf("dungeon_compass_packet"));

    public static final PacketCodec<RegistryByteBuf, DungeonCompassPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeString(value.dungeonType);
    }, buf -> new DungeonCompassPacket(buf.readString()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
