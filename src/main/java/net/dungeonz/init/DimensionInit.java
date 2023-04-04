package net.dungeonz.init;

import net.dungeonz.dimension.DungeonChunkGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class DimensionInit {

    public static final RegistryKey<World> DUNGEON_WORLD = RegistryKey.of(Registry.WORLD_KEY, new Identifier("dungeonz", "dungeon"));
    public static final RegistryKey<DimensionType> DUNGEON_DIMENSION_TYPE_KEY = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, new Identifier("dungeonz", "dungeon"));

    public static void init() {
        Registry.register(Registry.CHUNK_GENERATOR, new Identifier("dungeonz", "dungeon"), DungeonChunkGenerator.CODEC);
    }

}