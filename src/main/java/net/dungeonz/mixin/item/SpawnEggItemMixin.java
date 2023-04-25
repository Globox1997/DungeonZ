package net.dungeonz.mixin.item;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.dungeonz.block.entity.DungeonSpawnerEntity;
import net.dungeonz.block.logic.DungeonSpawnerLogic;
import net.dungeonz.init.BlockInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

@Mixin(SpawnEggItem.class)
public class SpawnEggItemMixin {

    @Inject(method = "useOnBlock", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void useOnBlockMixin(ItemUsageContext context, CallbackInfoReturnable<ActionResult> info, World world, ItemStack itemStack, BlockPos blockPos, Direction direction,
            BlockState blockState) {
        BlockEntity blockEntity;
        if (blockState.isOf(BlockInit.DUNGEON_SPAWNER) && (blockEntity = world.getBlockEntity(blockPos)) instanceof DungeonSpawnerEntity) {
            DungeonSpawnerLogic dungeonSpawnerLogic = ((DungeonSpawnerEntity) blockEntity).getLogic();
            EntityType<?> entityType = this.getEntityType(itemStack.getNbt());
            dungeonSpawnerLogic.setEntityId(entityType);
            blockEntity.markDirty();
            world.updateListeners(blockPos, blockState, blockState, Block.NOTIFY_ALL);
            world.emitGameEvent(context.getPlayer(), GameEvent.BLOCK_CHANGE, blockPos);
            itemStack.decrement(1);
            info.setReturnValue(ActionResult.CONSUME);
        }
    }

    @Shadow
    public EntityType<?> getEntityType(@Nullable NbtCompound nbt) {
        return null;
    }
}
