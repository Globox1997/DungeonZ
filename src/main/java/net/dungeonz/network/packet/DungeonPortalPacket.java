package net.dungeonz.network.packet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record DungeonPortalPacket(BlockPos blockPos, List<UUID> playerUuids, List<UUID> deadPlayerUuids, List<String> difficulties, Map<String, List<ItemStack>> possibleLoot,
                                  Map<String, List<ItemStack>> requiredItemStacks, int maxGroupSize, int minGroupSize, int waitingPlayerCount, int requiredLevel, int cooldownTime, String difficulty,
                                  boolean disableEffects, boolean privateGroup, Optional<Identifier> backgroundId)
        implements CustomPayload {

    public static final CustomPayload.Id<DungeonPortalPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("dungeonz", "dungeon_portal_packet"));

    public static final PacketCodec<RegistryByteBuf, DungeonPortalPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeBlockPos(value.blockPos);
        buf.writeCollection(value.playerUuids, (buffer, uuid) -> buffer.writeUuid(uuid));
        buf.writeCollection(value.deadPlayerUuids, (buffer, uuid) -> buffer.writeUuid(uuid));
        buf.writeCollection(value.difficulties, PacketByteBuf::writeString);
        buf.writeMap(value.possibleLoot, PacketByteBuf::writeString, (buffer, stacks) -> ItemStack.LIST_PACKET_CODEC.encode(buf, stacks));
        buf.writeMap(value.requiredItemStacks, PacketByteBuf::writeString, (buffer, stacks) -> ItemStack.LIST_PACKET_CODEC.encode(buf, stacks));
        buf.writeInt(value.maxGroupSize);
        buf.writeInt(value.minGroupSize);
        buf.writeInt(value.waitingPlayerCount);
        buf.writeInt(value.requiredLevel);
        buf.writeInt(value.cooldownTime);
        buf.writeString(value.difficulty);
        buf.writeBoolean(value.disableEffects);
        buf.writeBoolean(value.privateGroup);
        buf.writeOptional(value.backgroundId, PacketByteBuf::writeIdentifier);

    }, buf -> new DungeonPortalPacket(buf.readBlockPos(), buf.readList((buffer) -> PacketByteBuf.readUuid(buffer)), buf.readList((buffer) -> PacketByteBuf.readUuid(buffer)),
            buf.readList(PacketByteBuf::readString), buf.readMap(PacketByteBuf::readString, (bufx) -> ItemStack.LIST_PACKET_CODEC.decode(buf)),
            buf.readMap(PacketByteBuf::readString, (bufx) -> ItemStack.LIST_PACKET_CODEC.decode(buf)), buf.readInt(),
            buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readString(), buf.readBoolean(), buf.readBoolean(), buf.readOptional(PacketByteBuf::readIdentifier)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
