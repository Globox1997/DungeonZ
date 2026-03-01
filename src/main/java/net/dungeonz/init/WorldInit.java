package net.dungeonz.init;

import net.dungeonz.DungeonzMain;
import net.dungeonz.structure.DimensionStructure;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.structure.StructureType;

public class WorldInit {

    public static StructureType<DimensionStructure> DIMENSION_STRUCTURES;

    public static void init() {
        DIMENSION_STRUCTURES = Registry.register(Registries.STRUCTURE_TYPE, DungeonzMain.identifierOf("dimension_structures"), () -> DimensionStructure.CODEC);
    }

}
