package net.dungeonz.init;

import java.util.List;

import net.dungeonz.item.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemInit {

    public static final List<ItemStack> REQUIRED_DUNGEON_COMPASS_CALIBRATION_ITEMS = List.of(new ItemStack(Items.AMETHYST_SHARD, 3));

    public static final Item DUNGEON_COMPASS = new DungeonCompassItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1));

    public static void init() {
        Registry.register(Registry.ITEM, new Identifier("dungeonz", "dungeon_compass"), DUNGEON_COMPASS);
    }

}
