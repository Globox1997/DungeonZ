package net.dungeonz.network.packet;

import net.dungeonz.DungeonzMain;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record DungeonGatePacket(BlockPos portalBlockPos, String blockId, String particleId, String unlockItemId) implements CustomPayload {

    public static final CustomPayload.Id<DungeonGatePacket> PACKET_ID = new CustomPayload.Id<>(DungeonzMain.identifierOf("dungeon_gate_packet"));

    public static final PacketCodec<RegistryByteBuf, DungeonGatePacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeBlockPos(value.portalBlockPos);
        buf.writeString(value.blockId);
        buf.writeString(value.particleId);
        buf.writeString(value.unlockItemId);
    }, buf -> new DungeonGatePacket(buf.readBlockPos(), buf.readString(), buf.readString(), buf.readString()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
