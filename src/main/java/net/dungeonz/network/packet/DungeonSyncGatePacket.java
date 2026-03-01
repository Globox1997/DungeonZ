package net.dungeonz.network.packet;

import net.dungeonz.DungeonzMain;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public record DungeonSyncGatePacket(Set<BlockPos> dungeonGatesPosList, String blockId, String particleEffect, String unlockItem) implements CustomPayload {

    public static final CustomPayload.Id<DungeonSyncGatePacket> PACKET_ID = new CustomPayload.Id<>(DungeonzMain.identifierOf("dungeon_sync_gate_packet"));

    public static final PacketCodec<RegistryByteBuf, DungeonSyncGatePacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeCollection(value.dungeonGatesPosList, BlockPos.PACKET_CODEC);
        buf.writeString(value.blockId);
        buf.writeString(value.particleEffect);
        buf.writeString(value.unlockItem);
    }, buf -> new DungeonSyncGatePacket(buf.readCollection(HashSet::new, BlockPos.PACKET_CODEC), buf.readString(), buf.readString(), buf.readString()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
