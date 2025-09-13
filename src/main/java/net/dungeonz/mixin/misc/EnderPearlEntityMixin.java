package net.dungeonz.mixin.misc;

import net.dungeonz.util.DungeonHelper;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.init.DimensionInit;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMixin extends ThrownItemEntity {

    public EnderPearlEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasVehicle()Z"), cancellable = true)
    protected void onCollisionMixin(HitResult hitResult, CallbackInfo info) {
        if (this.getWorld().getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
            Entity entity = this.getOwner();
            if (entity instanceof ServerPlayerEntity serverPlayerEntity && DungeonHelper.getCurrentDungeon(serverPlayerEntity) != null && !DungeonHelper.getCurrentDungeon(serverPlayerEntity).isEnderPearlAllowed()) {
                this.discard();
                info.cancel();
            }
        }
    }
}
