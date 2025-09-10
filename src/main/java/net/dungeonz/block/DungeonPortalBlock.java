package net.dungeonz.block;

import java.util.Iterator;

import net.minecraft.block.FluidFillable;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.dungeonz.DungeonzMain;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.init.BlockInit;
import net.dungeonz.network.DungeonServerPacket;
import net.dungeonz.util.DungeonHelper;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.partyaddon.access.GroupManagerAccess;
import net.partyaddon.network.PartyAddonServerPacket;

public class DungeonPortalBlock extends BlockWithEntity implements FluidFillable {

    public static final MapCodec<DungeonPortalBlock> CODEC = DungeonPortalBlock.createCodec(DungeonPortalBlock::new);

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
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player.getWorld().getBlockEntity(pos) != null && player.getWorld().getBlockEntity(pos) instanceof DungeonPortalEntity dungeonPortalEntity) {
            if (isOtherDungeonPortalBlockNearby(world, pos)) {
                dungeonPortalEntity = getMainDungeonPortalEntity(world, pos);
                pos = getMainDungeonPortalBlockPos(world, pos);
            }
            if (player.isCreativeLevelTwoOp() && (dungeonPortalEntity.getDungeon() == null || player.isSneaking())) {
                if (!world.isClient()) {
                    DungeonServerPacket.writeS2COpenOpScreenPacket((ServerPlayerEntity) player, dungeonPortalEntity, null);
                }
                return ActionResult.success(world.isClient());
            } else if (dungeonPortalEntity.getDungeon() != null) {
                if (!world.isClient()) {
                    if (DungeonzMain.isPartyAddonLoaded) {
                        PartyAddonServerPacket.writeS2CSyncGroupManagerPacket((ServerPlayerEntity) player, ((GroupManagerAccess) player).getGroupManager());
                    }
                    player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
                }
                return ActionResult.success(world.isClient());
            }
        }
        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient() && !entity.hasVehicle() && !entity.hasPassengers() && entity.canUsePortals(false) && entity instanceof ServerPlayerEntity) {
            if (!entity.hasPortalCooldown()) {
                if (isOtherDungeonPortalBlockNearby(world, pos)) {
                    pos = getMainDungeonPortalBlockPos(world, pos);
                }
                DungeonHelper.teleportDungeon((ServerPlayerEntity) entity, pos, entity.getUuid());
                entity.resetPortalCooldown();
            }
        }
    }

    @Override
    protected boolean canBucketPlace(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return DungeonGateBlock.validateTicker(type, BlockInit.DUNGEON_PORTAL_ENTITY, world.isClient() ? DungeonPortalEntity::clientTick : DungeonPortalEntity::serverTick);
    }

    public static boolean isOtherDungeonPortalBlockNearby(World world, BlockPos pos) {
        for (BlockPos checkPos : BlockPos.iterateOutwards(pos, 1, 1, 1)) {
            if (checkPos.equals(pos)) {
                continue;
            }
            if (world.getBlockState(checkPos).isOf(BlockInit.DUNGEON_PORTAL)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static BlockPos getMainDungeonPortalBlockPos(World world, BlockPos pos) {
        BlockPos checkPos = new BlockPos(pos);
        for (int i = 1; i < 30; i++) {
            if (world.getBlockState(checkPos.east(1)).isOf(BlockInit.DUNGEON_PORTAL)) {
                checkPos = checkPos.east(1);
            } else {
                break;
            }
        }
        for (int i = 1; i < 30; i++) {
            if (world.getBlockState(checkPos.south(1)).isOf(BlockInit.DUNGEON_PORTAL)) {
                checkPos = checkPos.south(1);
            } else {
                break;
            }
        }
        for (int i = 1; i < 30; i++) {
            if (world.getBlockState(checkPos.down(1)).isOf(BlockInit.DUNGEON_PORTAL)) {
                checkPos = checkPos.down(1);
            } else {
                break;
            }
        }
        return world.getBlockEntity(checkPos) instanceof DungeonPortalEntity dungeonPortalEntity ? dungeonPortalEntity.getPos() : null;
    }

    @Nullable
    public static DungeonPortalEntity getMainDungeonPortalEntity(World world, BlockPos pos) {
        if (getMainDungeonPortalBlockPos(world, pos) != null) {
            return (DungeonPortalEntity) world.getBlockEntity(getMainDungeonPortalBlockPos(world, pos));
        }
        return null;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    // Used for not getting removed by water
    @Override
    public boolean canFillWithFluid(@Nullable PlayerEntity player, BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }
}
