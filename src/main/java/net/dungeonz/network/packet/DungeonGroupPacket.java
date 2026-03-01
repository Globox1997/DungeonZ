package net.dungeonz.network.packet;

import net.dungeonz.DungeonzMain;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record DungeonGroupPacket(BlockPos portalBlockPos, boolean privateGroup) implements CustomPayload {

    public static final CustomPayload.Id<DungeonGroupPacket> PACKET_ID = new CustomPayload.Id<>(DungeonzMain.identifierOf("dungeon_group_packet"));

    public static final PacketCodec<RegistryByteBuf, DungeonGroupPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeBlockPos(value.portalBlockPos);
        buf.writeBoolean(value.privateGroup);
    }, buf -> new DungeonGroupPacket(buf.readBlockPos(), buf.readBoolean()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
