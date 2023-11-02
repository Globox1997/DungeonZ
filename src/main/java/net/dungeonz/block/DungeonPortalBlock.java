package net.dungeonz.block;

import org.jetbrains.annotations.Nullable;

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
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.partyaddon.access.GroupManagerAccess;
import net.partyaddon.network.PartyAddonServerPacket;

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
        if (player.getWorld().getBlockEntity(pos) != null && player.getWorld().getBlockEntity(pos) instanceof DungeonPortalEntity) {
            DungeonPortalEntity dungeonPortalEntity = (DungeonPortalEntity) player.getWorld().getBlockEntity(pos);

            if (player.isCreativeLevelTwoOp() && (dungeonPortalEntity.getDungeon() == null || player.isSneaking())) {
                if (!world.isClient) {
                    DungeonServerPacket.writeS2COpenOpScreenPacket((ServerPlayerEntity) player, dungeonPortalEntity, null);
                }
                return ActionResult.success(world.isClient);
            } else if (dungeonPortalEntity.getDungeon() != null) {
                if (!world.isClient) {
                    if (DungeonzMain.isPartyAddonLoaded) {
                        PartyAddonServerPacket.writeS2CSyncGroupManagerPacket((ServerPlayerEntity) player, ((GroupManagerAccess) player).getGroupManager());
                    }
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
                DungeonHelper.teleportDungeon((ServerPlayerEntity) entity, pos, entity.getUuid());
                entity.resetPortalCooldown();
            }
        }
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return DungeonGateBlock.checkType(type, BlockInit.DUNGEON_PORTAL_ENTITY, world.isClient ? DungeonPortalEntity::clientTick : DungeonPortalEntity::serverTick);
    }

}
