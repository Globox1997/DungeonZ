package net.dungeonz.network.packet;

import net.dungeonz.DungeonzMain;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record DungeonSyncScreenPacket(BlockPos blockPos, String difficulty) implements CustomPayload {

    public static final CustomPayload.Id<DungeonSyncScreenPacket> PACKET_ID = new CustomPayload.Id<>(DungeonzMain.identifierOf("dungeon_sync_screen_packet"));

    public static final PacketCodec<RegistryByteBuf, DungeonSyncScreenPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeBlockPos(value.blockPos);
        buf.writeString(value.difficulty);
    }, buf -> new DungeonSyncScreenPacket(buf.readBlockPos(), buf.readString()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
