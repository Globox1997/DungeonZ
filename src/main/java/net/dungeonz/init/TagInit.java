package net.dungeonz.init;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class TagInit {

    public static final TagKey<EntityType<?>> IMMUNE_TO_ZOMBIFICATION = TagKey.of(RegistryKeys.ENTITY_TYPE, new Identifier("dungeonz", "immune_to_zombification"));

    public static void init() {
    }

}
