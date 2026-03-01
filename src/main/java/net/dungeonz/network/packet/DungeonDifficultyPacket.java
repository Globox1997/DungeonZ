package net.dungeonz.network.packet;

import net.dungeonz.DungeonzMain;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record DungeonDifficultyPacket(BlockPos portalBlockPos) implements CustomPayload {

    public static final CustomPayload.Id<DungeonDifficultyPacket> PACKET_ID = new CustomPayload.Id<>(DungeonzMain.identifierOf("dungeon_difficulty_packet"));

    public static final PacketCodec<RegistryByteBuf, DungeonDifficultyPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeBlockPos(value.portalBlockPos);
    }, buf -> new DungeonDifficultyPacket(buf.readBlockPos()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
