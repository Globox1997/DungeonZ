package net.dungeonz.network.packet;

import net.dungeonz.DungeonzMain;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record DungeonTypePacket(BlockPos portalBlockPos, String dungeonType, String defaultDifficulty) implements CustomPayload {

    public static final CustomPayload.Id<DungeonTypePacket> PACKET_ID = new CustomPayload.Id<>(DungeonzMain.identifierOf("dungeon_type_packet"));

    public static final PacketCodec<RegistryByteBuf, DungeonTypePacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeBlockPos(value.portalBlockPos);
        buf.writeString(value.dungeonType);
        buf.writeString(value.defaultDifficulty);
    }, buf -> new DungeonTypePacket(buf.readBlockPos(), buf.readString(), buf.readString()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
