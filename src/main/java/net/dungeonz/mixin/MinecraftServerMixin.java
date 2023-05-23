package net.dungeonz.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.init.DimensionInit;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Shadow
    @Mutable
    @Final
    private Map<RegistryKey<World>, ServerWorld> worlds;

    @Inject(method = "updateMobSpawnOptions", at = @At("TAIL"))
    private void updateMobSpawnOptionsMixin(CallbackInfo info) {
        if (worlds.get(DimensionInit.DUNGEON_WORLD) != null) {
            worlds.get(DimensionInit.DUNGEON_WORLD).setMobSpawnOptions(false, false);
        }
    }
}
