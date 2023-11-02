package net.dungeonz.init;

import net.dungeonz.dungeon.DungeonChunkGenerator;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class DimensionInit {

    public static final RegistryKey<World> DUNGEON_WORLD = RegistryKey.of(RegistryKeys.WORLD, new Identifier("dungeonz", "dungeon"));
    public static final RegistryKey<DimensionType> DUNGEON_DIMENSION_TYPE_KEY = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, new Identifier("dungeonz", "dungeon"));

    public static void init() {
        Registry.register(Registries.CHUNK_GENERATOR, new Identifier("dungeonz", "dungeon"), DungeonChunkGenerator.CODEC);
    }

}