package net.dungeonz.item;

import java.util.List;

import net.dungeonz.init.ItemInit;
import net.dungeonz.network.DungeonServerPacket;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Vanishable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DungeonCompassItem extends Item implements Vanishable {

    public static final String DUNGEON_TYPE_KEY = "DungeonType";
    public static final String DUNGEON_POS_KEY = "DungeonPos";

    public DungeonCompassItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient) {
            return;
        }
        if (hasDungeon(stack) && world.getTime() % 100 == 0 && !hasDungeonStructure(stack)) {
            setCompassDungeonStructure((ServerWorld) world, entity.getBlockPos(), stack, stack.getNbt().getString(DUNGEON_TYPE_KEY));
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();

        if (world.getBlockState(blockPos).isOf(Blocks.CARTOGRAPHY_TABLE)) {
            if (!world.isClient) {
                DungeonServerPacket.writeS2COpenCompassScreenPacket((ServerPlayerEntity) context.getPlayer(),
                        context.getStack().getNbt() != null ? context.getStack().getNbt().getString(DUNGEON_TYPE_KEY) : "");
            }
            return ActionResult.success(world.isClient);
        }
        return super.useOnBlock(context);
    }

    public static boolean hasDungeon(ItemStack stack) {
        NbtCompound nbtCompound = stack.getNbt();
        return nbtCompound != null && nbtCompound.contains(DUNGEON_TYPE_KEY);
    }

    public static boolean hasDungeonStructure(ItemStack itemStack) {
        if (itemStack.getNbt() != null && itemStack.getNbt().contains(DUNGEON_POS_KEY + "X")) {
            return true;
        }
        return false;
    }

    @Nullable
    public static BlockPos getDungeonStructurePos(ItemStack itemStack) {
        if (itemStack.hasNbt() && itemStack.getNbt().contains(DUNGEON_POS_KEY + "X")) {
            NbtCompound nbt = itemStack.getNbt();
            return new BlockPos(nbt.getInt(DUNGEON_POS_KEY + "X"), nbt.getInt(DUNGEON_POS_KEY + "Y"), nbt.getInt(DUNGEON_POS_KEY + "Z"));
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
            NbtCompound nbt = itemStack.getOrCreateNbt();

            nbt.putString(DUNGEON_TYPE_KEY, dungeonType);
            BlockPos structurePos = getDungeonStructurePos(world, dungeonType, playerPos);
            if (structurePos != null) {
                nbt.putInt(DUNGEON_POS_KEY + "X", structurePos.getX());
                nbt.putInt(DUNGEON_POS_KEY + "Y", structurePos.getY());
                nbt.putInt(DUNGEON_POS_KEY + "Z", structurePos.getZ());
            } else {
                nbt.remove(DUNGEON_POS_KEY + "X");
                nbt.remove(DUNGEON_POS_KEY + "Y");
                nbt.remove(DUNGEON_POS_KEY + "Z");
            }
            itemStack.setNbt(nbt);
        }
    }

    @Nullable
    private static BlockPos getDungeonStructurePos(ServerWorld world, String dungeonType, BlockPos playerPos) {
        return world.locateStructure(TagKey.of(RegistryKeys.STRUCTURE, new Identifier("dungeonz", dungeonType)), playerPos, 100, false);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (stack.hasNbt() && stack.getNbt().contains(DUNGEON_TYPE_KEY)) {
            tooltip.add(Text.translatable("dungeon." + stack.getNbt().getString(DUNGEON_TYPE_KEY)));
        } else {
            tooltip.add(Text.translatable("compass.compass_item.cartography"));
        }
    }

}
