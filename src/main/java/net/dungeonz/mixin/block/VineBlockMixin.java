package net.dungeonz.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.init.ConfigInit;
import net.dungeonz.init.DimensionInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.VineBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
@Mixin(VineBlock.class)
public abstract class VineBlockMixin extends Block {

    public VineBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (ConfigInit.CONFIG.devMode && world.getBlockState(pos).isOf(Blocks.VINE)) {
            if (!world.isClient) {
                if (world.isAir(pos.down())) {
                    world.setBlockState(pos.down(), (BlockState) this.getDefaultState().with(VineBlock.getFacingProperty(hit.getSide().getOpposite()), true), Block.NOTIFY_LISTENERS);
                }
            }
            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player, hand, hit);

    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo info) {
        if (world.getRegistryKey() == DimensionInit.DUNGEON_WORLD || ConfigInit.CONFIG.devMode) {
            info.cancel();
        }
    }

}
