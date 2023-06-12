package net.dungeonz.init;

import net.dungeonz.structure.DimensionStructure;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.StructureType;

public class WorldInit {

    public static StructureType<DimensionStructure> DIMENSION_STRUCTURES;

    public static void init() {
        DIMENSION_STRUCTURES = Registry.register(Registries.STRUCTURE_TYPE, new Identifier("dungeonz", "dimension_structures"), () -> DimensionStructure.CODEC);
    }

}
