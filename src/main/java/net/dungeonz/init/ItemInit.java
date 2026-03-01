package net.dungeonz.init;

import java.util.List;
import java.util.function.UnaryOperator;

import net.dungeonz.DungeonzMain;
import net.dungeonz.item.*;
import net.dungeonz.item.component.DungeonCompassComponent;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemInit {

    // Item Group
    public static final RegistryKey<ItemGroup> DUNGEONZ_ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, DungeonzMain.identifierOf("item_group"));

    // Component
    public static final ComponentType<DungeonCompassComponent> DUNGEON_COMPASS_DATA = registerComponent(DungeonzMain.identifierOf("dungeon_data"),
            builder -> builder.codec(DungeonCompassComponent.CODEC).packetCodec(DungeonCompassComponent.PACKET_CODEC));

    public static final List<ItemStack> REQUIRED_DUNGEON_COMPASS_CALIBRATION_ITEMS = List.of(new ItemStack(Items.AMETHYST_SHARD, 3));

    public static final Item DUNGEON_COMPASS = new DungeonCompassItem(new Item.Settings().maxCount(1));

    private static <T> ComponentType<T> registerComponent(Identifier id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, id, builderOperator.apply(ComponentType.builder()).build());
    }

    public static void init() {
        Registry.register(Registries.ITEM_GROUP, DUNGEONZ_ITEM_GROUP,
                FabricItemGroup.builder().icon(() -> new ItemStack(DUNGEON_COMPASS)).displayName(Text.translatable("item.dungeonz.item_group")).build());
        Registry.register(Registries.ITEM, DungeonzMain.identifierOf("dungeon_compass"), DUNGEON_COMPASS);
        ItemGroupEvents.modifyEntriesEvent(DUNGEONZ_ITEM_GROUP).register(entries -> entries.add(DUNGEON_COMPASS));
    }

}
