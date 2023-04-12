package net.dungeonz.block;

import net.dungeonz.access.ServerPlayerAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dimension.DungeonPlacementHandler;
import net.dungeonz.init.DimensionInit;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class DungeonPortalBlock extends Block implements BlockEntityProvider {

    public DungeonPortalBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DungeonPortalEntity(pos, state);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!entity.hasVehicle() && !entity.hasPassengers() && entity.canUsePortals() && entity instanceof PlayerEntity) {

            if (!world.isClient) {
                ServerWorld serverWorld = (ServerWorld) entity.getEntityWorld();
                if (serverWorld.getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
                    ServerWorld oldWorld = ((ServerPlayerAccess) entity).getOldServerWorld();
                    if (oldWorld != null) {
                        FabricDimensions.teleport(entity, oldWorld, DungeonPlacementHandler.leave((ServerPlayerEntity) entity, oldWorld));
                        return;
                    }
                } else {
                    ServerWorld dungeonWorld = serverWorld.getServer().getWorld(DimensionInit.DUNGEON_WORLD);
                    if (dungeonWorld == null) {
                        ((PlayerEntity) entity).sendMessage(Text.literal("Failed to find world, was it registered?"), false);
                        return;
                    }
                    if (((DungeonPortalEntity) world.getBlockEntity(pos)).getMaxGroupSize() >= ((DungeonPortalEntity) world.getBlockEntity(pos)).getDungeonPlayerCount()) {
                        FabricDimensions.teleport(entity, dungeonWorld,
                                DungeonPlacementHandler.enter((ServerPlayerEntity) entity, dungeonWorld, (ServerWorld) entity.getEntityWorld(), (DungeonPortalEntity) world.getBlockEntity(pos), pos));
                    } else {
                        ((PlayerEntity) entity).sendMessage(Text.literal("Dungeon is full"), false);
                    }
                }
            }
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(100) == 0) {
            // world.playSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5f, random.nextFloat() * 0.4f +
            // 0.8f, false);
        }
        // for (int i = 0; i < 4; ++i) {
        // double d = (double) pos.getX() + random.nextDouble();
        // double e = (double) pos.getY() + random.nextDouble();
        // double f = (double) pos.getZ() + random.nextDouble();
        // double g = ((double) random.nextFloat() - 0.5) * 0.5;
        // double h = ((double) random.nextFloat() - 0.5) * 0.5;
        // double j = ((double) random.nextFloat() - 0.5) * 0.5;
        // int k = random.nextInt(2) * 2 - 1;
        // if (world.getBlockState(pos.west()).isOf(this) || world.getBlockState(pos.east()).isOf(this)) {
        // f = (double) pos.getZ() + 0.5 + 0.25 * (double) k;
        // j = random.nextFloat() * 2.0f * (float) k;
        // } else {
        // d = (double) pos.getX() + 0.5 + 0.25 * (double) k;
        // g = random.nextFloat() * 2.0f * (float) k;
        // }
        // world.addParticle(ParticleTypes.FLAME, d, e, f, g * 0.01D, h * 0.01D, j * 0.01D);
        // }
    }

}
