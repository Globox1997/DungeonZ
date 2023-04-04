package net.dungeonz.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep.Carver;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DungeonChunkGenerator extends ChunkGenerator {

    public static final Codec<DungeonChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> createStructureSetRegistryGetter(instance)
            .and(RegistryOps.createRegistryCodec(Registry.BIOME_KEY).forGetter((generator) -> generator.biomeRegistry)).apply(instance, instance.stable(DungeonChunkGenerator::new)));

    private final Registry<Biome> biomeRegistry;

    public DungeonChunkGenerator(Registry<StructureSet> registry, Registry<Biome> biomeRegistry) {
        super(registry, Optional.of(RegistryEntryList.of(Collections.emptyList())), new FixedBiomeSource(biomeRegistry.getOrCreateEntry(BiomeKeys.PLAINS)));
        this.biomeRegistry = biomeRegistry;
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public void populateEntities(ChunkRegion region) {
    }

    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
    }

    @Override
    public void setStructureStarts(DynamicRegistryManager registryManager, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk,
            StructureTemplateManager structureTemplateManager, long seed) {
    }

    @Override
    public void addStructureReferences(StructureWorldAccess world, StructureAccessor accessor, Chunk chunk) {
    }

    @Override
    public Pool<SpawnSettings.SpawnEntry> getEntitySpawnList(RegistryEntry<Biome> biome, StructureAccessor accessor, SpawnGroup group, BlockPos pos) {
        return Pool.of(new ArrayList<SpawnSettings.SpawnEntry>());
    }

    @Override
    public int getWorldHeight() {
        return 256;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinimumY() {
        return 0;
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long l, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, Carver carver) {
    }

    @Override
    public void buildSurface(ChunkRegion chunkRegion, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk) {
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getHeight(int x, int z, Type type, HeightLimitView heightLimitView, NoiseConfig noiseConfig) {
        return 0;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView var3, NoiseConfig var4) {
        return new VerticalBlockSample(z, new BlockState[0]);
    }

    @Override
    public void getDebugHudText(List<String> list, NoiseConfig noiseConfig, BlockPos blockPos) {
    }

}