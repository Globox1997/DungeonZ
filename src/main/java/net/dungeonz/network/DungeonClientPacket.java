package net.dungeonz.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import net.dungeonz.access.ClientPlayerAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.block.screen.DungeonPortalScreen;
import net.dungeonz.block.screen.DungeonPortalScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class DungeonClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(DungeonServerPacket.DUNGEON_INFO_PACKET, (client, handler, buf, sender) -> {
            List<Integer> breakableBlockIdList = buf.readIntList();
            List<Integer> placeableBlockIdList = buf.readIntList();
            boolean allowElytra = buf.readBoolean();
            client.execute(() -> {
                ((ClientPlayerAccess) client.player).setClientDungeonInfo(breakableBlockIdList, placeableBlockIdList, allowElytra);
            });
        });
        // ClientPlayNetworking.registerGlobalReceiver(DungeonServerPacket.DUNGEON_SCREEN_PACKET, (client, handler, buf, sender) -> {
        // int dungeonPlayerCount = buf.readInt();
        // List<UUID> dungeonPlayerUUIDs = new ArrayList<UUID>();
        // for (int i = 0; i < dungeonPlayerCount; i++) {
        // dungeonPlayerUUIDs.add(buf.readUuid());
        // }
        // int difficultyCount = buf.readInt();
        // List<String> difficulties = new ArrayList<String>();
        // if (difficultyCount != 0) {
        // difficulties.add(buf.readString());
        // }
        // int possibleLootCount = buf.readInt();
        // Map<String, List<ItemStack>> possibleLootDifficultyItemStackMap = new HashMap<String, List<ItemStack>>();
        // if (possibleLootCount != 0) {
        // // possibleLootDifficultyItemStackMap = buf.readMap(PacketByteBuf::readString, PacketByteBuf::readItemStack);
        // }
        // final Map<String, List<ItemStack>> possibleLootDifficultyItemStacks = possibleLootDifficultyItemStackMap;
        // int requiredItemCount = buf.readInt();
        // List<ItemStack> requiredItemStacks = new ArrayList<ItemStack>();
        // if (requiredItemCount != 0) {
        // // requiredItemStacks.add(buf.readItemStack());
        // }
        // int maxGroupSize = buf.readInt();
        // int cooldown = buf.readInt();
        // String difficulty = buf.readString();

        // client.execute(() -> {
        // ScreenHandler screenHandler = client.player.currentScreenHandler;
        // System.out.println("??? " + screenHandler + " : " + (screenHandler instanceof DungeonPortalScreenHandler) + " : " + client.currentScreen);
        // if (screenHandler instanceof DungeonPortalScreenHandler) {
        // DungeonPortalScreenHandler dungeonPortalScreenHandler = (DungeonPortalScreenHandler) screenHandler;

        // dungeonPortalScreenHandler.setDungeonPlayerUUIDs(dungeonPlayerUUIDs);
        // dungeonPortalScreenHandler.setDifficulties(difficulties);
        // dungeonPortalScreenHandler.setPossibleLootItemStacks(possibleLootDifficultyItemStacks);
        // dungeonPortalScreenHandler.setRequiredItemStacks(requiredItemStacks);
        // dungeonPortalScreenHandler.setMaxPlayerCount(maxGroupSize);
        // dungeonPortalScreenHandler.setCooldown(cooldown);
        // dungeonPortalScreenHandler.setDifficulty(difficulty);
        // System.out.println("SET: " + difficulty);
        // }
        // });
        // });
        ClientPlayNetworking.registerGlobalReceiver(DungeonServerPacket.SYNC_SCREEN_PACKET, (client, handler, buf, sender) -> {
            BlockPos dungeonPortalPos = buf.readBlockPos();
            String difficulty = buf.readString();
            client.execute(() -> {
                if (client.world.getBlockEntity(dungeonPortalPos) != null && client.world.getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity) {
                    DungeonPortalEntity dungeonPortalEntity = (DungeonPortalEntity) client.world.getBlockEntity(dungeonPortalPos);
                    dungeonPortalEntity.setDifficulty(difficulty);
                    if (client.currentScreen instanceof DungeonPortalScreen) {
                        ((DungeonPortalScreen) client.currentScreen).difficultyButton.setText(Text.translatable("dungeonz.difficulty." + difficulty));
                    }
                }
            });
        });
    }

    public static void writeC2SChanceDifficultyPacket(MinecraftClient client, BlockPos portalBlockPos) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(portalBlockPos);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(DungeonServerPacket.CHANGE_DUNGEON_DIFFICULTY_PACKET, buf);
        client.getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SDungeonTeleportPacket(MinecraftClient client, BlockPos portalBlockPos) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(portalBlockPos);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(DungeonServerPacket.DUNGEON_TELEPORT_PACKET, buf);
        client.getNetworkHandler().sendPacket(packet);
    }

}
