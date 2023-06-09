package net.dungeonz;

import net.dungeonz.init.RenderInit;
import net.dungeonz.network.DungeonClientPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DungeonzClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        DungeonClientPacket.init();
        RenderInit.init();
    }

}
