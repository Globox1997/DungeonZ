package net.dungeonz.block;

import net.dungeonz.access.ServerPlayerAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dimension.DungeonPlacementHandler;
import net.dungeonz.init.DimensionInit;
import net.dungeonz.util.DungeonHelper;
import net.dungeonz.util.InventoryHelper;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class DungeonPortalBlock extends BlockWithEntity {

    public DungeonPortalBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DungeonPortalEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) != null) {
            if (!world.isClient) {
                player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            } else {
                return ActionResult.success(world.isClient);
            }
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && !entity.hasVehicle() && !entity.hasPassengers() && entity.canUsePortals() && entity instanceof ServerPlayerEntity) {
            if (!entity.hasPortalCooldown()) {
                teleportDungeon((ServerPlayerEntity) entity, pos);
            } else {
                entity.resetPortalCooldown();
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

    public static void teleportDungeon(ServerPlayerEntity player, BlockPos dungeonPortalPos) {
        if (player.world.getBlockEntity(dungeonPortalPos) != null && player.world.getBlockEntity(dungeonPortalPos) instanceof DungeonPortalEntity) {
            DungeonPortalEntity dungeonPortalEntity = (DungeonPortalEntity) player.world.getBlockEntity(dungeonPortalPos);

            if (player.world.getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
                ServerWorld oldWorld = ((ServerPlayerAccess) player).getOldServerWorld();
                if (oldWorld != null) {
                    FabricDimensions.teleport(player, oldWorld, DungeonPlacementHandler.leave(player, oldWorld));
                    return;
                }
            } else {
                ServerWorld dungeonWorld = player.world.getServer().getWorld(DimensionInit.DUNGEON_WORLD);
                if (dungeonWorld == null) {
                    player.sendMessage(Text.literal("Failed to find world, was it registered?"), false);
                    return;
                }
                if (dungeonPortalEntity.getDungeonPlayerCount() < dungeonPortalEntity.getMaxGroupSize()) {
                    if (dungeonPortalEntity.getDungeon() != null) {
                        if (dungeonPortalEntity.getCooldown() > 0) {
                            player.sendMessage(Text.translatable("text.dungeonz.dungeon_cooldown"), false);
                            return;
                        }
                        if (InventoryHelper.hasRequiredItemStacks(player.getInventory(), DungeonHelper.getRequiredItemStackList(dungeonPortalEntity.getDungeon()))) {
                            InventoryHelper.decrementRequiredItemStacks(player.getInventory(), DungeonHelper.getRequiredItemStackList(dungeonPortalEntity.getDungeon()));
                        } else {
                            player.sendMessage(Text.translatable("text.dungeonz.missing"), false);
                            return;
                        }
                        FabricDimensions.teleport(player, dungeonWorld,
                                DungeonPlacementHandler.enter(player, dungeonWorld, (ServerWorld) player.world, dungeonPortalEntity, dungeonPortalPos, dungeonPortalEntity.getDifficulty()));
                    } else {
                        player.sendMessage(Text.translatable("text.dungeonz.dungeon_portal_missing"), false);
                    }
                } else {
                    player.sendMessage(Text.translatable("text.dungeonz.dungeon_full"), false);
                }
            }
        }
    }

}
