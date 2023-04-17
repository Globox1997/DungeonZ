package net.dungeonz.init;

import net.dungeonz.block.render.DungeonPortalRenderer;
import net.dungeonz.block.render.DungeonSpawnerRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

@Environment(EnvType.CLIENT)
public class RenderInit {

    public static void init() {
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.DUNGEON_SPAWNER, RenderLayer.getCutout());
        BlockEntityRendererFactories.register(BlockInit.DUNGEON_PORTAL_ENTITY, DungeonPortalRenderer::new);
        BlockEntityRendererFactories.register(BlockInit.DUNGEON_SPAWNER_ENTITY, DungeonSpawnerRenderer::new);

    }

}
