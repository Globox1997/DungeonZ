package net.dungeonz.network;

import java.util.List;

import net.dungeonz.access.ClientPlayerAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

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
    }

}
