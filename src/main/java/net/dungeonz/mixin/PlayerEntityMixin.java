package net.dungeonz.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.dungeonz.init.DimensionInit;
import net.dungeonz.util.DungeonHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    public PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "canPlaceOn", at = @At(value = "HEAD"), cancellable = true)
    public void canPlaceOnMixin(BlockPos pos, Direction facing, ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        PlayerEntity playerEntity = (PlayerEntity) (Object) this;
        if (playerEntity != null && !playerEntity.isCreative() && this.getWorld().getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
            info.setReturnValue(false);
        }
    }

    @Override
    public boolean teleport(double x, double y, double z, boolean particleEffects) {
        if (this.getWorld().getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
            return false;
        }
        return super.teleport(x, y, z, particleEffects);
    }

    @Override
    public boolean addStatusEffect(StatusEffectInstance effect, Entity source) {
        if (!this.getWorld().isClient() && effect.getEffectType().isBeneficial() && this.getWorld().getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
            if (DungeonHelper.getDungeonPortalEntity((ServerPlayerEntity) (Object) this).getDisableEffects()) {
                return false;
            }
        }
        return super.addStatusEffect(effect, source);
    }

}
