package net.dungeonz.network;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.dungeonz.access.ClientPlayerAccess;
import net.dungeonz.access.InGameHudAccess;
import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.block.screen.DungeonGateOpScreen;
import net.dungeonz.block.screen.DungeonPortalOpScreen;
import net.dungeonz.block.screen.DungeonPortalScreen;
import net.dungeonz.block.screen.DungeonPortalScreenHandler;
import net.dungeonz.init.SoundInit;
import net.dungeonz.item.screen.DungeonCompassScreen;
import net.dungeonz.network.packet.DungeonCompassPacket;
import net.dungeonz.network.packet.DungeonCompassScreenPacket;
import net.dungeonz.network.packet.DungeonDifficultyPacket;
import net.dungeonz.network.packet.DungeonEffectPacket;
import net.dungeonz.network.packet.DungeonGatePacket;
import net.dungeonz.network.packet.DungeonInfoPacket;
import net.dungeonz.network.packet.DungeonOpScreenPacket;
import net.dungeonz.network.packet.DungeonSyncGatePacket;
import net.dungeonz.network.packet.DungeonSyncScreenPacket;
import net.dungeonz.network.packet.DungeonTeleportCountdownPacket;
import net.dungeonz.network.packet.DungeonTeleportPacket;
import net.dungeonz.network.packet.DungeonTypePacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class DungeonClientPacket {

    @SuppressWarnings("resource")
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(DungeonInfoPacket.PACKET_ID, (payload, context) -> {
            List<Integer> breakableBlockIdList = payload.breakableBlockIdList();
            List<Integer> placeableBlockIdList = payload.placeableBlockIdList();
            boolean allowElytra = payload.allowElytra();
            context.client().execute(() -> {
                ((ClientPlayerAccess) context.player()).setClientDungeonInfo(breakableBlockIdList, placeableBlockIdList, allowElytra);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonSyncScreenPacket.PACKET_ID, (payload, context) -> {
            BlockPos dungeonPortalPos = payload.blockPos();
            String difficulty = payload.difficulty();

            context.client().execute(() -> {
                if (context.client().world.getBlockEntity(dungeonPortalPos) != null && context.client().world.getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity dungeonPortalEntity) {
                    dungeonPortalEntity.setDifficulty(difficulty);

                    if (context.client().currentScreen instanceof DungeonPortalScreen dungeonPortalScreen) {
                        dungeonPortalScreen.difficultyButton.setText(Text.translatable("dungeonz.difficulty." + difficulty));
                    }
                    if (context.client().player.currentScreenHandler instanceof DungeonPortalScreenHandler dungeonPortalScreenHandler) {
                        dungeonPortalScreenHandler.getDungeonPortalEntity().setDifficulty(difficulty);
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonOpScreenPacket.PACKET_ID, (payload, context) -> {
            BlockPos portalOrGatePos = payload.blockPos();
            String dungeonTypeOrBlockId = payload.blockIdOrDungeonType();
            String difficultyOrParticleId = payload.particleEffectOrDifficulty();
            String unlockItemId = payload.unlockItem();

            context.client().execute(() -> {
                if (context.client().world.getBlockEntity(portalOrGatePos) != null) {
                    if (context.client().world.getBlockEntity(portalOrGatePos) instanceof DungeonPortalEntity dungeonPortalEntity) {
                        dungeonPortalEntity.setDungeonType(dungeonTypeOrBlockId);
                        dungeonPortalEntity.setDifficulty(difficultyOrParticleId);
                        context.client().setScreen(new DungeonPortalOpScreen(portalOrGatePos));
                    } else if (context.client().world.getBlockEntity(portalOrGatePos) instanceof DungeonGateEntity dungeonGateEntity) {
                        dungeonGateEntity.setBlockId(Identifier.of(dungeonTypeOrBlockId));
                        dungeonGateEntity.setParticleEffectId(difficultyOrParticleId);
                        dungeonGateEntity.setUnlockItemId(unlockItemId);
                        context.client().setScreen(new DungeonGateOpScreen(portalOrGatePos));
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonCompassScreenPacket.PACKET_ID, (payload, context) -> {
            String dungeonType = payload.dungeonType();
            List<String> dungeonIds = payload.dungeonIdList();
            context.client().execute(() -> {
                context.client().setScreen(new DungeonCompassScreen(dungeonType, dungeonIds));
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonSyncGatePacket.PACKET_ID, (payload, context) -> {
            Set<BlockPos> dungeonGatesPosList = payload.dungeonGatesPosList();

            String blockId = payload.blockId();
            String particleId = payload.particleEffect();
            String unlockItemId = payload.unlockItem();

            context.client().execute(() -> {
                Iterator<BlockPos> iterator = dungeonGatesPosList.iterator();
                while (iterator.hasNext()) {
                    BlockPos pos = iterator.next();
                    if (context.client().world.getBlockEntity(pos) != null && context.client().world.getBlockEntity(pos) instanceof DungeonGateEntity dungeonGateEntity) {
                        dungeonGateEntity.setBlockId(Identifier.of(blockId));
                        dungeonGateEntity.setParticleEffectId(particleId);
                        dungeonGateEntity.setUnlockItemId(unlockItemId);
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DungeonTeleportCountdownPacket.PACKET_ID, (payload, context) -> {
            int dungeonTeleportCountdown = payload.countdownTicks();
            context.client().execute(() -> {
                ((InGameHudAccess) context.client().inGameHud).setDungeonCountdownTicks(dungeonTeleportCountdown);
                context.player().playSound(SoundInit.DUNGEON_COUNTDOWN_EVENT, 1.0f, 1.0f);
            });
        });
    }

    public static void writeC2SChangeDifficultyPacket(MinecraftClient client, BlockPos portalBlockPos) {
        ClientPlayNetworking.send(new DungeonDifficultyPacket(portalBlockPos));
    }

    public static void writeC2SChangeEffectsPacket(MinecraftClient client, BlockPos portalBlockPos, boolean disableEffects) {
        ClientPlayNetworking.send(new DungeonEffectPacket(portalBlockPos, disableEffects));
    }

    public static void writeC2SChangePrivateGroupPacket(MinecraftClient client, BlockPos portalBlockPos, boolean privateGroup) {
        ClientPlayNetworking.send(new DungeonEffectPacket(portalBlockPos, privateGroup));
    }

    public static void writeC2SDungeonTeleportPacket(MinecraftClient client, BlockPos portalBlockPos, @Nullable UUID requiredMinGroupUuid) {
        ClientPlayNetworking.send(new DungeonTeleportPacket(portalBlockPos, requiredMinGroupUuid != null, requiredMinGroupUuid));
    }

    public static void writeC2SSetDungeonTypePacket(MinecraftClient client, String dungeonType, String defaultDifficulty, BlockPos portalBlockPos) {
        ClientPlayNetworking.send(new DungeonTypePacket(portalBlockPos, dungeonType, defaultDifficulty));
    }

    public static void writeC2SSetGateBlockPacket(MinecraftClient client, String blockId, String particleId, String unlockItemId, BlockPos portalBlockPos) {
        ClientPlayNetworking.send(new DungeonGatePacket(portalBlockPos, blockId, particleId, unlockItemId));
    }

    public static void writeC2SSetDungeonCompassPacket(MinecraftClient client, String dungeonType) {
        ClientPlayNetworking.send(new DungeonCompassPacket(dungeonType));
    }
}
