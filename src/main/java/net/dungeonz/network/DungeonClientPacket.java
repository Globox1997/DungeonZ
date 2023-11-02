package net.dungeonz.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.Unpooled;
import net.dungeonz.access.ClientPlayerAccess;
import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.block.screen.DungeonGateOpScreen;
import net.dungeonz.block.screen.DungeonPortalOpScreen;
import net.dungeonz.block.screen.DungeonPortalScreen;
import net.dungeonz.block.screen.DungeonPortalScreenHandler;
import net.dungeonz.item.screen.DungeonCompassScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
                        ((DungeonPortalScreenHandler) client.player.currentScreenHandler).getDungeonPortalEntity().setDifficulty(difficulty);
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonServerPacket.OP_SCREEN_PACKET, (client, handler, buf, sender) -> {
            String portalOrGate = buf.readString();
            BlockPos portalOrGatePos = buf.readBlockPos();
            String dungeonTypeOrBlockId = buf.readString();
            String difficultyOrParticleId = buf.readString();
            String unlockItemId = portalOrGate.equals("gate") ? buf.readString() : "";

            client.execute(() -> {
                if (client.world.getBlockEntity(portalOrGatePos) != null) {
                    if (client.world.getBlockEntity(portalOrGatePos) instanceof DungeonPortalEntity) {
                        DungeonPortalEntity dungeonPortalEntity = (DungeonPortalEntity) client.world.getBlockEntity(portalOrGatePos);
                        dungeonPortalEntity.setDungeonType(dungeonTypeOrBlockId);
                        dungeonPortalEntity.setDifficulty(difficultyOrParticleId);
                        client.setScreen(new DungeonPortalOpScreen(portalOrGatePos));
                    } else if (client.world.getBlockEntity(portalOrGatePos) instanceof DungeonGateEntity) {
                        DungeonGateEntity dungeonGateEntity = (DungeonGateEntity) client.world.getBlockEntity(portalOrGatePos);
                        dungeonGateEntity.setBlockId(new Identifier(dungeonTypeOrBlockId));
                        dungeonGateEntity.setParticleEffectId(difficultyOrParticleId);
                        dungeonGateEntity.setUnlockItemId(unlockItemId);
                        client.setScreen(new DungeonGateOpScreen(portalOrGatePos));
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonServerPacket.COMPASS_SCREEN_PACKET, (client, handler, buf, sender) -> {
            String dungeonType = buf.readString();
            int dungeonCount = buf.readInt();
            List<String> dungeonIds = new ArrayList<String>();
            for (int i = 0; i < dungeonCount; i++) {
                dungeonIds.add(buf.readString());
            }
            client.execute(() -> {
                client.setScreen(new DungeonCompassScreen(dungeonType, dungeonIds));
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonServerPacket.SYNC_GATE_BLOCK_PACKET, (client, handler, buf, sender) -> {
            int posListSize = buf.readInt();
            List<BlockPos> dungeonGatesPosList = new ArrayList<BlockPos>();
            for (int i = 0; i < posListSize; i++) {
                dungeonGatesPosList.add(buf.readBlockPos());
            }
            String blockId = buf.readString();
            String particleId = buf.readString();
            String unlockItemId = buf.readString();

            client.execute(() -> {
                for (int i = 0; i < dungeonGatesPosList.size(); i++) {
                    if (client.world.getBlockEntity(dungeonGatesPosList.get(i)) != null && client.world.getBlockEntity(dungeonGatesPosList.get(i)) instanceof DungeonGateEntity) {
                        DungeonGateEntity dungeonGateEntity = (DungeonGateEntity) client.world.getBlockEntity(dungeonGatesPosList.get(i));
                        dungeonGateEntity.setBlockId(new Identifier(blockId));
                        dungeonGateEntity.setParticleEffectId(particleId);
                        dungeonGateEntity.setUnlockItemId(unlockItemId);
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

    public static void writeC2SChangePrivateGroupPacket(MinecraftClient client, BlockPos portalBlockPos, boolean privateGroup) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(portalBlockPos);
        buf.writeBoolean(privateGroup);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(DungeonServerPacket.CHANGE_DUNGEON_PRIVATE_GROUP_PACKET, buf);
        client.getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SDungeonTeleportPacket(MinecraftClient client, BlockPos portalBlockPos, @Nullable UUID requiredMinGroupUuid) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(portalBlockPos);
        buf.writeBoolean(requiredMinGroupUuid != null);
        if (requiredMinGroupUuid != null) {
            buf.writeUuid(requiredMinGroupUuid);
        }
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

    public static void writeC2SSetGateBlockPacket(MinecraftClient client, String blockId, String particleId, String unlockItemId, BlockPos portalBlockPos) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(portalBlockPos);
        buf.writeString(blockId);
        buf.writeString(particleId);
        buf.writeString(unlockItemId);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(DungeonServerPacket.SET_GATE_BLOCK_PACKET, buf);
        client.getNetworkHandler().sendPacket(packet);
    }

    public static void writeC2SSetDungeonCompassPacket(MinecraftClient client, String dungeonType) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(dungeonType);
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(DungeonServerPacket.SET_DUNGEON_COMPASS_PACKET, buf);
        client.getNetworkHandler().sendPacket(packet);
    }
}
