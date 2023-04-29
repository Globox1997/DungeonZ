package net.dungeonz.block.render;

import net.dungeonz.block.DungeonGateBlock;
import net.dungeonz.block.entity.DungeonGateEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class DungeonGateRenderer<T extends DungeonGateEntity> implements BlockEntityRenderer<T> {
    private final BlockRenderManager manager;

    public DungeonGateRenderer(BlockEntityRendererFactory.Context ctx) {
        this.manager = ctx.getRenderManager();
    }

    @Override
    public void render(T gateBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        World world = gateBlockEntity.getWorld();
        if (world == null) {
            return;
        }
        if (gateBlockEntity.getCachedState().get(DungeonGateBlock.ENABLED)) {
            BlockState state = gateBlockEntity.getBlockState();
            RenderLayer renderLayer = RenderLayers.getBlockLayer(state);
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);

            this.manager.getModelRenderer().render(world, this.manager.getModel(state), state, gateBlockEntity.getPos(), matrixStack, vertexConsumer, false, Random.create(),
                    state.getRenderingSeed(gateBlockEntity.getPos()), OverlayTexture.DEFAULT_UV);
        }
    }

}
