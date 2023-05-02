package net.dungeonz.init;

import net.dungeonz.item.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemInit {

    public static final Item DUNGEON_COMPASS = new DungeonCompassItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1));

    public static void init() {
        Registry.register(Registry.ITEM, new Identifier("dungeonz", "dungeon_compass"), DUNGEON_COMPASS);
    }

}
