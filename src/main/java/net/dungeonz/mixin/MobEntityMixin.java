package net.dungeonz.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.access.BossEntityAccess;
import net.dungeonz.dimension.DungeonPlacementHandler;
import net.dungeonz.init.BlockInit;
import net.dungeonz.init.CriteriaInit;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements BossEntityAccess {

    @Unique
    private boolean isDungeonBossEntity = false;
    @Unique
    private String dungeonBossLootTableId = "";
    @Unique
    private String dungeonDifficulty = "";
    @Unique
    private String dungeonTypeId = "";

    public MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo info) {
        if (this.isDungeonBossEntity) {
            nbt.putBoolean("IsDungeonBossEntity", this.isDungeonBossEntity);
            nbt.putString("DungeonBossLootTableId", this.dungeonBossLootTableId);
            nbt.putString("DungeonTypeId", this.dungeonTypeId);
            nbt.putString("DungeonDifficulty", this.dungeonDifficulty);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo info) {
        if (nbt.contains("IsDungeonBossEntity")) {
            this.isDungeonBossEntity = nbt.getBoolean("IsDungeonBossEntity");
            this.dungeonBossLootTableId = nbt.getString("DungeonBossLootTableId");
            this.dungeonTypeId = nbt.getString("DungeonTypeId");
            this.dungeonDifficulty = nbt.getString("DungeonDifficulty");
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (!this.world.isClient && this.isDungeonBossEntity) {
            this.world.setBlockState(this.getBlockPos(), BlockInit.DUNGEON_PORTAL.getDefaultState());
            this.world.setBlockState(this.getBlockPos().up(2), Blocks.CHEST.getDefaultState());
            DungeonPlacementHandler.fillChestWithLoot(this.getServer(), (ServerWorld) world, this.getBlockPos().up(2), this.dungeonBossLootTableId);

            List<PlayerEntity> players = this.world.getPlayers(TargetPredicate.createAttackable().setBaseMaxDistance(64.0), this, this.getBoundingBox().expand(64.0, 64.0, 64.0));
            for (int i = 0; i < players.size(); i++) {
                CriteriaInit.DUNGEON_COMPLETION.trigger((ServerPlayerEntity) players.get(i), this.dungeonTypeId, this.dungeonDifficulty);
            }
        }
        super.onDeath(damageSource);
    }

    @Override
    public void setBoss(String dungeonType, String difficulty, String lootTableId) {
        this.isDungeonBossEntity = true;
        this.dungeonBossLootTableId = lootTableId;
        this.dungeonTypeId = dungeonType;
        this.dungeonDifficulty = difficulty;
    }

}
