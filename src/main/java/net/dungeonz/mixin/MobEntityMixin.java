package net.dungeonz.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.access.BossEntityAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.init.BlockInit;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements BossEntityAccess {

    @Unique
    private boolean isDungeonBossEntity = false;
    @Unique
    private BlockPos portalPos = new BlockPos(0, 0, 0);
    @Unique
    private String worldRegistryKey = "";

    public MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo info) {
        if (this.isDungeonBossEntity) {
            nbt.putBoolean("IsDungeonBossEntity", this.isDungeonBossEntity);
            nbt.putString("WorldRegistryKey", this.worldRegistryKey);
            nbt.putInt("PortalPosX", this.portalPos.getX());
            nbt.putInt("PortalPosY", this.portalPos.getY());
            nbt.putInt("PortalPosZ", this.portalPos.getZ());
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo info) {
        if (nbt.contains("IsDungeonBossEntity")) {
            this.isDungeonBossEntity = nbt.getBoolean("IsDungeonBossEntity");
            this.worldRegistryKey = nbt.getString("WorldRegistryKey");
            this.portalPos = new BlockPos(nbt.getInt("PortalPosX"), nbt.getInt("PortalPosY"), nbt.getInt("PortalPosZ"));
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (!this.world.isClient && this.isDungeonBossEntity) {
            ServerWorld nonDungeonWorld = world.getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, new Identifier(this.worldRegistryKey)));

            if (nonDungeonWorld != null && nonDungeonWorld.getBlockEntity(this.portalPos) != null && nonDungeonWorld.getBlockEntity(this.portalPos) instanceof DungeonPortalEntity) {
                ((DungeonPortalEntity) nonDungeonWorld.getBlockEntity(this.portalPos)).finishDungeon((ServerWorld) this.world, this.getBlockPos());
            } else {
                this.world.setBlockState(this.getBlockPos(), BlockInit.DUNGEON_PORTAL.getDefaultState());
            }

        }
        super.onDeath(damageSource);

    }

    @Override
    public void setBoss(BlockPos portalPos, String worldRegistryKey) {
        this.isDungeonBossEntity = true;
        this.portalPos = portalPos;
        this.worldRegistryKey = worldRegistryKey;
    }

}
