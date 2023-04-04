package net.dungeonz.init;

import net.dungeonz.block.DungeonPortalBlock;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockInit {

    public static final Block DUNGEON_PORTAL = new DungeonPortalBlock(FabricBlockSettings.copy(Blocks.NETHER_PORTAL));

    public static BlockEntityType<DungeonPortalEntity> DUNGEON_PORTAL_ENTITY;

    public static void init() {
        Registry.register(Registry.ITEM, new Identifier("dungeonz", "dungeon_portal"), new BlockItem(DUNGEON_PORTAL, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));
        Registry.register(Registry.BLOCK, new Identifier("dungeonz", "dungeon_portal"), DUNGEON_PORTAL);

        DUNGEON_PORTAL_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "dungeonz:dungeon_portal_entity",
                FabricBlockEntityTypeBuilder.create(DungeonPortalEntity::new, DUNGEON_PORTAL).build(null));
    }
}
