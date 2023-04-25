package net.dungeonz.network;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.dungeonz.access.ServerPlayerAccess;
import net.dungeonz.block.DungeonPortalBlock;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dimension.DungeonPlacementHandler;
import net.dungeonz.init.DimensionInit;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class DungeonServerPacket {

    public static final Identifier DUNGEON_INFO_PACKET = new Identifier("dungeonz", "dungeon_info");
    // public static final Identifier DUNGEON_SCREEN_PACKET = new Identifier("dungeonz", "dungeon_screen");
    public static final Identifier CHANGE_DUNGEON_DIFFICULTY_PACKET = new Identifier("dungeonz", "change_dungeon_difficulty");
    public static final Identifier SYNC_SCREEN_PACKET = new Identifier("dungeonz", "sync_screen");
    public static final Identifier DUNGEON_TELEPORT_PACKET = new Identifier("dungeonz", "dungeon_teleport");

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(CHANGE_DUNGEON_DIFFICULTY_PACKET, (server, player, handler, buffer, sender) -> {
            BlockPos dungeonPortalPos = buffer.readBlockPos();
            server.execute(() -> {
                if (player.world.getBlockEntity(dungeonPortalPos) != null && player.world.getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity) {
                    DungeonPortalEntity dungeonPortalEntity = (DungeonPortalEntity) player.world.getBlockEntity(dungeonPortalPos);

                    // System.out.println(dungeonPortalEntity);

                    if (dungeonPortalEntity.getDungeonPlayerCount() == 0) {
                        List<String> difficulties = dungeonPortalEntity.getDungeon().getDifficultyList();
                        if (dungeonPortalEntity.getDifficulty().equals("")) {
                            dungeonPortalEntity.setDifficulty(difficulties.get(0));
                        } else {
                            int index = difficulties.indexOf(dungeonPortalEntity.getDifficulty()) + 1;
                            if (index >= difficulties.size()) {
                                index = 0;
                            }
                            dungeonPortalEntity.setDifficulty(difficulties.get(index));
                        }

                        // System.out.println(dungeonPortalEntity.getDifficulty());

                        writeS2CSyncScreenPacket(player, dungeonPortalEntity);
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(DUNGEON_TELEPORT_PACKET, (server, player, handler, buffer, sender) -> {
            BlockPos dungeonPortalPos = buffer.readBlockPos();
            server.execute(() -> {
                DungeonPortalBlock.teleportDungeon(player, dungeonPortalPos);
            });
        });
    }

    public static void writeS2CDungeonInfoPacket(ServerPlayerEntity serverPlayerEntity, List<Integer> breakableBlockIdList, List<Integer> placeableBlockIdList, boolean allowElytra) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeIntList(new IntArrayList(breakableBlockIdList));
        buf.writeIntList(new IntArrayList(placeableBlockIdList));
        buf.writeBoolean(allowElytra);
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(DUNGEON_INFO_PACKET, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    // public static void writeS2CDungeonScreenPacket(ServerPlayerEntity serverPlayerEntity, DungeonPortalEntity dungeonPortalEntity) {
    // PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
    // buf.writeInt(dungeonPortalEntity.getDungeonPlayerCount());
    // for (int i = 0; i < dungeonPortalEntity.getDungeonPlayerCount(); i++) {
    // buf.writeUuid(dungeonPortalEntity.getDungeonPlayerUUIDs().get(i));
    // }

    // if (dungeonPortalEntity.getDungeon() != null) {
    // // Difficulty
    // buf.writeInt(dungeonPortalEntity.getDungeon().getDifficultyList().size());
    // for (int i = 0; i < dungeonPortalEntity.getDungeon().getDifficultyList().size(); i++) {
    // buf.writeString(dungeonPortalEntity.getDungeon().getDifficultyList().get(i));
    // }
    // // Possible Loot Items
    // buf.writeInt(dungeonPortalEntity.getDungeon().getDifficultyBossLootTableMap().size());
    // // Map<String,I
    // // buf.writeMap(dungeonPortalEntity.getDungeon().getDifficultyBossLootTableMap(), PacketByteBuf::writeString, PacketByteBuf::writeItemStack);
    // // Required Items
    // buf.writeInt(dungeonPortalEntity.getDungeon().getRequiredItemCountMap().size());
    // // Iterator<Entry<Integer, Integer>> requiredItemIterator = dungeonPortalEntity.getDungeon().getRequiredItemCountMap().entrySet().iterator();
    // // while (requiredItemIterator.hasNext()) {
    // // Entry<Integer, Integer> entry = requiredItemIterator.next();
    // // buf.writeItemStack(new ItemStack(Registry.ITEM.get(entry.getKey()), entry.getValue()));
    // // }
    // } else {
    // buf.writeInt(0);
    // buf.writeInt(0);
    // buf.writeInt(0);
    // }

    // buf.writeInt(dungeonPortalEntity.getMaxGroupSize());
    // buf.writeInt(dungeonPortalEntity.getCooldown());
    // buf.writeString(dungeonPortalEntity.getDifficulty());
    // // System.out.println("WRITE: " + dungeonPortalEntity.getDifficulty());

    // CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(DUNGEON_SCREEN_PACKET, buf);
    // serverPlayerEntity.networkHandler.sendPacket(packet);
    // }

    public static void writeS2CSyncScreenPacket(ServerPlayerEntity serverPlayerEntity, DungeonPortalEntity dungeonPortalEntity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(dungeonPortalEntity.getPos());
        buf.writeString(dungeonPortalEntity.getDifficulty());

        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(SYNC_SCREEN_PACKET, buf);
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

}
