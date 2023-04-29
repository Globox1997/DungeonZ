package net.dungeonz.block;

import org.jetbrains.annotations.Nullable;

import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.init.BlockInit;
import net.dungeonz.init.ConfigInit;
import net.dungeonz.network.DungeonServerPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.util.ParticleUtil;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class DungeonGateBlock extends BlockWithEntity {

    public static BooleanProperty ENABLED = Properties.ENABLED;

    public DungeonGateBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(ENABLED, true));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DungeonGateEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return DungeonGateBlock.checkType(type, BlockInit.DUNGEON_GATE_ENTITY, world.isClient ? null : DungeonGateEntity::serverTick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.world.getBlockEntity(pos) != null && player.world.getBlockEntity(pos) instanceof DungeonGateEntity) {
            DungeonGateEntity dungeonGateEntity = (DungeonGateEntity) player.world.getBlockEntity(pos);
            if (player.isCreativeLevelTwoOp()) {
                if (!player.getStackInHand(hand).isEmpty() && player.getStackInHand(hand).getItem() instanceof BlockItem) {
                    dungeonGateEntity.setBlockId(Registry.BLOCK.getId(((BlockItem) player.getStackInHand(hand).getItem()).getBlock()));
                    dungeonGateEntity.markDirty();
                } else if (player.isSneaking()) {
                    if (!world.isClient) {
                        DungeonServerPacket.writeS2COpenOpScreenPacket((ServerPlayerEntity) player, null, dungeonGateEntity);
                    }
                }
                return ActionResult.success(world.isClient);
            } else if (dungeonGateEntity.getUnlockItem() != null && player.getStackInHand(hand).isOf(dungeonGateEntity.getUnlockItem())) {
                if (!world.isClient) {
                    if (!player.isCreative()) {
                        player.getStackInHand(hand).decrement(1);
                    }
                    dungeonGateEntity.unlockGate(pos);
                }
                return ActionResult.success(world.isClient);
            }

        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ENABLED);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(ENABLED) && world.getBlockEntity(pos) != null && world.getBlockEntity(pos) instanceof DungeonGateEntity
                && ((DungeonGateEntity) world.getBlockEntity(pos)).getParticleEffect() != null) {
            ParticleUtil.spawnParticle(world, pos, ((DungeonGateEntity) world.getBlockEntity(pos)).getParticleEffect(), UniformIntProvider.create(0, 1));
        }
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        if (!state.get(ENABLED)) {
            return true;
        }
        return super.isTranslucent(state, world, pos);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        if (state.get(ENABLED)) {
            return false;
        }
        return true;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (!state.get(ENABLED) && !ConfigInit.CONFIG.devMode) {
            return VoxelShapes.empty();
        }
        return super.getOutlineShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (!state.get(ENABLED)) {
            return VoxelShapes.empty();
        }
        return super.getCollisionShape(state, world, pos, context);
    }

}
