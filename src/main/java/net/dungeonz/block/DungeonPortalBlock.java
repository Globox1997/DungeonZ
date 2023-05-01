package net.dungeonz.block;

import org.jetbrains.annotations.Nullable;

import net.dungeonz.access.ServerPlayerAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dimension.DungeonPlacementHandler;
import net.dungeonz.init.BlockInit;
import net.dungeonz.init.DimensionInit;
import net.dungeonz.network.DungeonServerPacket;
import net.dungeonz.util.DungeonHelper;
import net.dungeonz.util.InventoryHelper;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
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
        if (player.world.getBlockEntity(pos) != null && player.world.getBlockEntity(pos) instanceof DungeonPortalEntity) {
            DungeonPortalEntity dungeonPortalEntity = (DungeonPortalEntity) player.world.getBlockEntity(pos);

            if (player.isCreativeLevelTwoOp() && (dungeonPortalEntity.getDungeon() == null || player.isSneaking())) {
                if (!world.isClient) {
                    DungeonServerPacket.writeS2COpenOpScreenPacket((ServerPlayerEntity) player, dungeonPortalEntity, null);
                }
                return ActionResult.success(world.isClient);
            } else if (dungeonPortalEntity.getDungeon() != null) {
                if (!world.isClient) {
                    player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
                }
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
                entity.resetPortalCooldown();
            }
        }
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return DungeonGateBlock.checkType(type, BlockInit.DUNGEON_PORTAL_ENTITY, world.isClient ? DungeonPortalEntity::clientTick : DungeonPortalEntity::serverTick);
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
                if (dungeonPortalEntity.getDungeon() != null) {
                    if ((dungeonPortalEntity.getDungeonPlayerCount() + dungeonPortalEntity.getDeadDungeonPlayerUUIDs().size()) < dungeonPortalEntity.getMaxGroupSize()) {

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
                        FabricDimensions.teleport(player, dungeonWorld, DungeonPlacementHandler.enter(player, dungeonWorld, (ServerWorld) player.world, dungeonPortalEntity, dungeonPortalPos,
                                dungeonPortalEntity.getDifficulty(), dungeonPortalEntity.getDisableEffects()));
                    } else {
                        player.sendMessage(Text.translatable("text.dungeonz.dungeon_full"), false);
                    }
                } else {
                    player.sendMessage(Text.translatable("text.dungeonz.dungeon_missing"), false);
                }
            }
        }
    }

}
