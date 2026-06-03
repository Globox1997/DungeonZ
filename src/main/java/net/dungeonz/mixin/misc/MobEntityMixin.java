package net.dungeonz.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.access.BossEntityAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.init.BlockInit;
import net.dungeonz.init.CriteriaInit;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements BossEntityAccess {

    @Unique
    private boolean isDungeonBossEntity = false;
    @Unique
    private BlockPos portalPos = new BlockPos(0, 0, 0);
    @Unique
    private String worldRegistryKey = "";
    @Unique
    private String dungeonType = "";
    @Unique
    private String difficulty = "";

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
            nbt.putString("DungeonType", this.dungeonType);
            nbt.putString("Difficulty", this.difficulty);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo info) {
        if (nbt.contains("IsDungeonBossEntity")) {
            this.isDungeonBossEntity = nbt.getBoolean("IsDungeonBossEntity");
            this.worldRegistryKey = nbt.getString("WorldRegistryKey");
            this.portalPos = new BlockPos(nbt.getInt("PortalPosX"), nbt.getInt("PortalPosY"), nbt.getInt("PortalPosZ"));
            if (nbt.contains("DungeonType")) {
                this.dungeonType = nbt.getString("DungeonType");
            }
            if (nbt.contains("Difficulty")) {
                this.difficulty = nbt.getString("Difficulty");
            }
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (!this.getWorld().isClient() && this.isDungeonBossEntity) {
            ServerWorld nonDungeonWorld = getWorld().getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(this.worldRegistryKey)));

            if (nonDungeonWorld != null && nonDungeonWorld.getBlockEntity(this.portalPos) != null && nonDungeonWorld.getBlockEntity(this.portalPos) instanceof DungeonPortalEntity) {
                ((DungeonPortalEntity) nonDungeonWorld.getBlockEntity(this.portalPos)).finishDungeon((ServerWorld) this.getWorld(), this.getBlockPos());
            } else {
                this.getWorld().setBlockState(this.getBlockPos(), BlockInit.DUNGEON_PORTAL.getDefaultState());
            }

            // Trigger advancement criterion
            if (damageSource.getAttacker() instanceof ServerPlayerEntity player) {
                CriteriaInit.DUNGEON_COMPLETION.trigger(player, this.dungeonType, this.difficulty);
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

    @Override
    public void setDungeonData(String dungeonType, String difficulty) {
        this.dungeonType = dungeonType;
        this.difficulty = difficulty;
    }
}