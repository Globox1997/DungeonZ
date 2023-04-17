package net.dungeonz.block.entity;

import net.dungeonz.block.logic.DungeonSpawnerLogic;
import net.dungeonz.init.BlockInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerEntry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DungeonSpawnerEntity extends BlockEntity {
    private final DungeonSpawnerLogic logic = new DungeonSpawnerLogic() {

        @Override
        public void sendStatus(World world, BlockPos pos, int status) {
            world.addSyncedBlockEvent(pos, BlockInit.DUNGEON_SPAWNER, status, 0);
        }

        @Override
        public void setSpawnEntry(@Nullable World world, BlockPos pos, MobSpawnerEntry spawnEntry) {
            super.setSpawnEntry(world, pos, spawnEntry);
            if (world != null) {
                BlockState blockState = world.getBlockState(pos);
                world.updateListeners(pos, blockState, blockState, Block.NO_REDRAW);
            }
        }
    };

    public DungeonSpawnerEntity(BlockPos pos, BlockState state) {
        super(BlockInit.DUNGEON_SPAWNER_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.logic.readNbt(this.world, this.pos, nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        this.logic.writeNbt(nbt);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, DungeonSpawnerEntity blockEntity) {
        blockEntity.logic.clientTick(world, pos);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, DungeonSpawnerEntity blockEntity) {
        blockEntity.logic.serverTick((ServerWorld) world, pos);
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbtCompound = this.createNbt();
        nbtCompound.remove("SpawnPotentials");
        return nbtCompound;
    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (this.logic.handleStatus(this.world, type)) {
            return true;
        }
        return super.onSyncedBlockEvent(type, data);
    }

    @Override
    public boolean copyItemDataRequiresOperator() {
        return true;
    }

    public DungeonSpawnerLogic getLogic() {
        return this.logic;
    }

}
