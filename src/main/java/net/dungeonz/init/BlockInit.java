package net.dungeonz.init;

import net.dungeonz.DungeonzMain;
import net.dungeonz.block.DungeonGateBlock;
import net.dungeonz.block.DungeonPortalBlock;
import net.dungeonz.block.DungeonSpawnerBlock;
import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.block.entity.DungeonSpawnerEntity;
import net.dungeonz.block.screen.DungeonPortalScreenHandler;
import net.dungeonz.network.packet.DungeonPortalPacket;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class BlockInit {

    public static final Block DUNGEON_PORTAL = register("dungeon_portal", new DungeonPortalBlock(AbstractBlock.Settings.copy(Blocks.END_PORTAL)));
    public static final Block DUNGEON_SPAWNER = register("dungeon_spawner", new DungeonSpawnerBlock(AbstractBlock.Settings.copy(Blocks.SPAWNER)));
    public static final Block DUNGEON_GATE = register("dungeon_gate", new DungeonGateBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque()));

    public static BlockEntityType<DungeonPortalEntity> DUNGEON_PORTAL_ENTITY;
    public static BlockEntityType<DungeonSpawnerEntity> DUNGEON_SPAWNER_ENTITY;
    public static BlockEntityType<DungeonGateEntity> DUNGEON_GATE_ENTITY;

    //  public static final ScreenHandlerType<DungeonPortalScreenHandler> PORTAL = new ExtendedScreenHandlerType<>(DungeonPortalScreenHandler::new);

    public static final ScreenHandlerType<DungeonPortalScreenHandler> PORTAL = new ExtendedScreenHandlerType<DungeonPortalScreenHandler, DungeonPortalPacket>(
            (syncId, playerInventory, buf) -> new DungeonPortalScreenHandler(syncId, playerInventory, buf), DungeonPortalPacket.PACKET_CODEC);

    private static Block register(String id, Block block) {
        return register(DungeonzMain.identifierOf(id), block);
    }

    private static Block register(Identifier id, Block block) {
        Item item = Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings().rarity(Rarity.EPIC)));
        ItemGroupEvents.modifyEntriesEvent(ItemInit.DUNGEONZ_ITEM_GROUP).register(entries -> entries.add(item));

        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void init() {
        DUNGEON_PORTAL_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, DungeonzMain.identifierOf("dungeon_portal_entity"),
                BlockEntityType.Builder.create(DungeonPortalEntity::new, DUNGEON_PORTAL).build(null));
        DUNGEON_SPAWNER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, DungeonzMain.identifierOf("dungeon_spawner_entity"),
                BlockEntityType.Builder.create(DungeonSpawnerEntity::new, DUNGEON_SPAWNER).build(null));
        DUNGEON_GATE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, DungeonzMain.identifierOf("dungeon_gate_entity"), BlockEntityType.Builder.create(DungeonGateEntity::new, DUNGEON_GATE).build(null));

        Registry.register(Registries.SCREEN_HANDLER, "dungeonz:portal", PORTAL);
    }
}
