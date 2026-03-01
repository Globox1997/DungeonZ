package net.dungeonz.network.packet;

import net.dungeonz.DungeonzMain;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record DungeonTeleportPacket(BlockPos dungeonPortalPos, boolean isMinGroupRequired, @Nullable UUID uuid) implements CustomPayload {

    public static final CustomPayload.Id<DungeonTeleportPacket> PACKET_ID = new CustomPayload.Id<>(DungeonzMain.identifierOf("dungeon_teleport_packet"));

    public static final PacketCodec<RegistryByteBuf, DungeonTeleportPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeBlockPos(value.dungeonPortalPos);
        buf.writeBoolean(value.isMinGroupRequired);
        if (value.isMinGroupRequired) {
            buf.writeUuid(value.uuid);
        }
    }, buf -> {
        BlockPos dungeonPortalPos = buf.readBlockPos();
        boolean isMinGroupRequired = buf.readBoolean();
        UUID uuid = null;
        if (isMinGroupRequired) {
            uuid = buf.readUuid();
        }
        return new DungeonTeleportPacket(dungeonPortalPos, isMinGroupRequired, uuid);
    });

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
