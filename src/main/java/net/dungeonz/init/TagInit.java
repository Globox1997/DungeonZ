package net.dungeonz.init;

import net.dungeonz.DungeonzMain;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class TagInit {

    public static final TagKey<EntityType<?>> IMMUNE_TO_ZOMBIFICATION = TagKey.of(RegistryKeys.ENTITY_TYPE, DungeonzMain.identifierOf("immune_to_zombification"));

    public static void init() {
    }

}
