package net.dungeonz.item;

import net.dungeonz.DungeonzMain;
import net.dungeonz.init.ItemInit;
import net.dungeonz.item.component.DungeonCompassComponent;
import net.dungeonz.network.DungeonServerPacket;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class DungeonCompassItem extends Item {

    public DungeonCompassItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient()) {
            return;
        }
        if (hasDungeon(stack) && world.getTime() % 100 == 0 && !hasDungeonStructure(stack)) {
            setCompassDungeonStructure((ServerWorld) world, entity.getBlockPos(), stack, stack.get(ItemInit.DUNGEON_COMPASS_DATA).dungeonType());
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();

        if (world.getBlockState(blockPos).isOf(Blocks.CARTOGRAPHY_TABLE)) {
            if (!world.isClient()) {
                DungeonServerPacket.writeS2COpenCompassScreenPacket((ServerPlayerEntity) context.getPlayer(),
                        context.getStack().get(ItemInit.DUNGEON_COMPASS_DATA) != null ? context.getStack().get(ItemInit.DUNGEON_COMPASS_DATA).dungeonType() : "");
            }
            return ActionResult.success(world.isClient());
        }
        return super.useOnBlock(context);
    }

    public static boolean hasDungeon(ItemStack stack) {
        return stack.get(ItemInit.DUNGEON_COMPASS_DATA) != null;
    }

    public static boolean hasDungeonStructure(ItemStack itemStack) {
        if (itemStack.get(ItemInit.DUNGEON_COMPASS_DATA) != null && itemStack.get(ItemInit.DUNGEON_COMPASS_DATA).hasDungeon()) {
            return true;
        }
        return false;
    }

    @Nullable
    public static BlockPos getDungeonStructurePos(ItemStack itemStack) {
        if (itemStack.get(ItemInit.DUNGEON_COMPASS_DATA) != null && itemStack.get(ItemInit.DUNGEON_COMPASS_DATA).hasDungeon()) {
            return itemStack.get(ItemInit.DUNGEON_COMPASS_DATA).dungeonPos().get();
        }
        return null;
    }

    @Nullable
    public static GlobalPos createGlobalDungeonStructurePos(World world, ItemStack itemStack) {
        BlockPos pos = getDungeonStructurePos(itemStack);
        return pos != null ? GlobalPos.create(world.getRegistryKey(), pos) : null;
    }

    public static void setCompassDungeonStructure(ServerWorld world, BlockPos playerPos, ItemStack itemStack, String dungeonType) {
        if (itemStack.isOf(ItemInit.DUNGEON_COMPASS)) {
            BlockPos structurePos = getDungeonStructurePos(world, dungeonType, playerPos);
            if (structurePos == null) {
                structurePos = BlockPos.ofFloored(0, 0, 0);
            }
            itemStack.set(ItemInit.DUNGEON_COMPASS_DATA, new DungeonCompassComponent(dungeonType, structurePos != null, Optional.of(structurePos)));
        }
    }

    @Nullable
    private static BlockPos getDungeonStructurePos(ServerWorld world, String dungeonType, BlockPos playerPos) {
        return world.locateStructure(TagKey.of(RegistryKeys.STRUCTURE, DungeonzMain.identifierOf(dungeonType)), playerPos, 100, false);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        if (stack.get(ItemInit.DUNGEON_COMPASS_DATA) != null) {
            tooltip.add(Text.translatable("dungeon." + stack.get(ItemInit.DUNGEON_COMPASS_DATA).dungeonType()));
            if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.isCreativeLevelTwoOp() && stack.get(ItemInit.DUNGEON_COMPASS_DATA).dungeonPos().isPresent()) {
                tooltip.add(Text.of(stack.get(ItemInit.DUNGEON_COMPASS_DATA).dungeonPos().get().toShortString()));
            }
        } else {
            tooltip.add(Text.translatable("compass.compass_item.cartography"));
        }
    }

}
