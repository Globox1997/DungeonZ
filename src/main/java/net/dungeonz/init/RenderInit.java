package net.dungeonz.init;

import net.dungeonz.block.render.DungeonPortalRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

@Environment(EnvType.CLIENT)
public class RenderInit {

    public static void init() {
        // BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.DUNGEON_PORTAL, RenderLayer.getTranslucent());
        BlockEntityRendererFactories.register(BlockInit.DUNGEON_PORTAL_ENTITY, DungeonPortalRenderer::new);
    }

}
