package net.dungeonz.network;

import java.util.List;

import io.netty.buffer.Unpooled;
import net.dungeonz.access.ClientPlayerAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.block.screen.DungeonPortalScreen;
import net.dungeonz.block.screen.DungeonPortalScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
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
                    if (client.player.currentScreenHandler instanceof DungeonPortalScreenHandler) {
                        ((DungeonPortalScreenHandler) client.player.currentScreenHandler).setDifficulty(difficulty);
                    }
                }
            });
        });
    }

    public static void writeC2SChangeDifficultyPacket(MinecraftClient client, BlockPos portalBlockPos) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(portalBlockPos);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(DungeonServerPacket.CHANGE_DUNGEON_DIFFICULTY_PACKET, buf);
        client.getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SChangeEffectsPacket(MinecraftClient client, BlockPos portalBlockPos, boolean disableEffects) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(portalBlockPos);
        buf.writeBoolean(disableEffects);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(DungeonServerPacket.CHANGE_DUNGEON_EFFECTS_PACKET, buf);
        client.getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SDungeonTeleportPacket(MinecraftClient client, BlockPos portalBlockPos) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(portalBlockPos);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(DungeonServerPacket.DUNGEON_TELEPORT_PACKET, buf);
        client.getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SSetDungeonTypePacket(MinecraftClient client, String dungeonType, String defaultDifficulty, BlockPos portalBlockPos) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(portalBlockPos);
        buf.writeString(dungeonType);
        buf.writeString(defaultDifficulty);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(DungeonServerPacket.SET_DUNGEON_TYPE_PACKET, buf);
        client.getNetworkHandler().sendPacket(packet);
    }
}
