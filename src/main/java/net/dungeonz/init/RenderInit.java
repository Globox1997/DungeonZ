package net.dungeonz.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class RenderInit {

    public static void init() {
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.DUNGEON_PORTAL, RenderLayer.getTranslucent());
    }

}
