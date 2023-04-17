package net.dungeonz.block.render;

import net.dungeonz.block.entity.DungeonSpawnerEntity;
import net.dungeonz.block.logic.DungeonSpawnerLogic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class DungeonSpawnerRenderer implements BlockEntityRenderer<DungeonSpawnerEntity> {
    private final EntityRenderDispatcher entityRenderDispatcher;

    public DungeonSpawnerRenderer(BlockEntityRendererFactory.Context ctx) {
        this.entityRenderDispatcher = ctx.getEntityRenderDispatcher();
    }

    @Override
    public void render(DungeonSpawnerEntity dungeonSpawnerEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        matrixStack.push();
        matrixStack.translate(0.5, 0.0, 0.5);
        DungeonSpawnerLogic dungeonSpawnerLogic = dungeonSpawnerEntity.getLogic();
        Entity entity = dungeonSpawnerLogic.getRenderedEntity(dungeonSpawnerEntity.getWorld());
        if (entity != null) {
            float g = 0.53125f;
            float h = Math.max(entity.getWidth(), entity.getHeight());
            if ((double) h > 1.0) {
                g /= h;
            }
            matrixStack.translate(0.0, 0.4f, 0.0);
            matrixStack.multiply(
                    Vec3f.POSITIVE_Y.getDegreesQuaternion((float) MathHelper.lerp((double) f, dungeonSpawnerLogic.randomParticleValueTwo(), dungeonSpawnerLogic.randomParticleValueOne()) * 10.0f));
            matrixStack.translate(0.0, -0.2f, 0.0);
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-30.0f));
            matrixStack.scale(g, g, g);
            this.entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, f, matrixStack, vertexConsumerProvider, i);
        }
        matrixStack.pop();
    }
}
