package net.dungeonz.network.packet;

import net.dungeonz.DungeonzMain;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record DungeonOpScreenPacket(BlockPos blockPos, String blockIdOrDungeonType, String particleEffectOrDifficulty, String unlockItem) implements CustomPayload {

    public static final CustomPayload.Id<DungeonOpScreenPacket> PACKET_ID = new CustomPayload.Id<>(DungeonzMain.identifierOf("dungeon_op_screen_packet"));

    public static final PacketCodec<RegistryByteBuf, DungeonOpScreenPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {

        buf.writeBlockPos(value.blockPos);
        buf.writeString(value.blockIdOrDungeonType);
        buf.writeString(value.particleEffectOrDifficulty);
        buf.writeString(value.unlockItem);
    }, buf -> new DungeonOpScreenPacket(buf.readBlockPos(), buf.readString(), buf.readString(), buf.readString()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
