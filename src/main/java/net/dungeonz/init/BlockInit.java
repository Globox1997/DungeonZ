package net.dungeonz.init;

import net.dungeonz.block.*;
import net.dungeonz.block.entity.*;
import net.dungeonz.block.screen.DungeonPortalScreenHandler;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class BlockInit {

    public static final Block DUNGEON_PORTAL = new DungeonPortalBlock(FabricBlockSettings.copy(Blocks.END_PORTAL));
    public static final Block DUNGEON_SPAWNER = new DungeonSpawnerBlock(FabricBlockSettings.copy(Blocks.SPAWNER));
    public static final Block DUNGEON_GATE = new DungeonGateBlock(FabricBlockSettings.copy(Blocks.BEDROCK).nonOpaque());

    public static BlockEntityType<DungeonPortalEntity> DUNGEON_PORTAL_ENTITY;
    public static BlockEntityType<DungeonSpawnerEntity> DUNGEON_SPAWNER_ENTITY;
    public static BlockEntityType<DungeonGateEntity> DUNGEON_GATE_ENTITY;

    public static final ScreenHandlerType<DungeonPortalScreenHandler> PORTAL = new ExtendedScreenHandlerType<>(DungeonPortalScreenHandler::new);

    public static void init() {
        Registry.register(Registry.ITEM, new Identifier("dungeonz", "dungeon_portal"), new BlockItem(DUNGEON_PORTAL, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS).rarity(Rarity.EPIC)));
        Registry.register(Registry.BLOCK, new Identifier("dungeonz", "dungeon_portal"), DUNGEON_PORTAL);
        Registry.register(Registry.ITEM, new Identifier("dungeonz", "dungeon_spawner"), new BlockItem(DUNGEON_SPAWNER, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS).rarity(Rarity.EPIC)));
        Registry.register(Registry.BLOCK, new Identifier("dungeonz", "dungeon_spawner"), DUNGEON_SPAWNER);
        Registry.register(Registry.ITEM, new Identifier("dungeonz", "dungeon_gate"), new BlockItem(DUNGEON_GATE, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS).rarity(Rarity.EPIC)));
        Registry.register(Registry.BLOCK, new Identifier("dungeonz", "dungeon_gate"), DUNGEON_GATE);

        DUNGEON_PORTAL_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "dungeonz:dungeon_portal_entity",
                FabricBlockEntityTypeBuilder.create(DungeonPortalEntity::new, DUNGEON_PORTAL).build(null));
        DUNGEON_SPAWNER_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "dungeonz:dungeon_spawner_entity",
                FabricBlockEntityTypeBuilder.create(DungeonSpawnerEntity::new, DUNGEON_SPAWNER).build(null));
        DUNGEON_GATE_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "dungeonz:dungeon_gate_entity", FabricBlockEntityTypeBuilder.create(DungeonGateEntity::new, DUNGEON_GATE).build(null));

        Registry.register(Registry.SCREEN_HANDLER, "dungeonz:portal", PORTAL);
    }
}
