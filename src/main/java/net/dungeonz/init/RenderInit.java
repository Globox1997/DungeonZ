package net.dungeonz.init;

import net.dungeonz.block.render.DungeonGateRenderer;
import net.dungeonz.block.render.DungeonPortalRenderer;
import net.dungeonz.block.render.DungeonSpawnerRenderer;
import net.dungeonz.block.screen.DungeonPortalScreen;
import net.dungeonz.item.DungeonCompassItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.CompassAnglePredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RenderInit {

    public static void init() {
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.DUNGEON_SPAWNER, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.DUNGEON_GATE, RenderLayer.getCutout());

        BlockEntityRendererFactories.register(BlockInit.DUNGEON_PORTAL_ENTITY, DungeonPortalRenderer::new);
        BlockEntityRendererFactories.register(BlockInit.DUNGEON_SPAWNER_ENTITY, DungeonSpawnerRenderer::new);
        BlockEntityRendererFactories.register(BlockInit.DUNGEON_GATE_ENTITY, DungeonGateRenderer::new);

        HandledScreens.register(BlockInit.PORTAL, DungeonPortalScreen::new);

        ModelPredicateProviderRegistry.register(ItemInit.DUNGEON_COMPASS, new Identifier("angle"), new CompassAnglePredicateProvider((world, stack, entity) -> {
            return DungeonCompassItem.createGlobalDungeonStructurePos(world, stack);
        }));
    }

}
