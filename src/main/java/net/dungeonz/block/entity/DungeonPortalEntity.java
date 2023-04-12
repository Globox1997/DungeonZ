package net.dungeonz.block.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import net.dungeonz.dimension.DungeonPlacementHandler;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.init.BlockInit;
import net.dungeonz.init.CriteriaInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class DungeonPortalEntity extends BlockEntity {

    private String dungeonType = "";
    private String difficulty = "";
    private boolean dungeonStructureGenerated = false;
    private List<UUID> dungeonPlayerUUIDs = new ArrayList<UUID>();
    private int maxGroupSize = 0;
    private int cooldown = 0;
    private HashMap<Integer, ArrayList<BlockPos>> blockBlockPosMap = new HashMap<Integer, ArrayList<BlockPos>>();
    private List<BlockPos> chestPosList = new ArrayList<BlockPos>();
    private List<BlockPos> exitPosList = new ArrayList<BlockPos>();
    private BlockPos bossBlockPos = new BlockPos(0, 0, 0);
    private BlockPos bossLootBlockPos = new BlockPos(0, 0, 0);

    public DungeonPortalEntity(BlockPos pos, BlockState state) {
        super(BlockInit.DUNGEON_PORTAL_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.dungeonType = nbt.getString("DungeonType");
        this.difficulty = nbt.getString("Difficulty");
        this.dungeonStructureGenerated = nbt.getBoolean("DungeonStructureGenerated");
        this.dungeonPlayerUUIDs.clear();
        for (int i = 0; i < nbt.getInt("DungeonPlayerCount"); i++) {
            this.dungeonPlayerUUIDs.add(nbt.getUuid("PlayerUUID" + i));
        }
        this.maxGroupSize = nbt.getInt("MaxGroupSize");
        this.cooldown = nbt.getInt("Cooldown");

        this.blockBlockPosMap.clear();

        if (nbt.getInt("BlockMapSize") > 0) {
            for (int i = 0; i < nbt.getInt("BlockMapSize"); i++) {
                ArrayList<BlockPos> posList = new ArrayList<>();
                for (int u = 0; u < nbt.getInt("BlockListSize" + i); u++) {
                    posList.add(new BlockPos(nbt.getInt("BlockPosX" + u), nbt.getInt("BlockPosY" + u), nbt.getInt("BlockPosZ" + u)));
                }
                this.blockBlockPosMap.put(nbt.getInt("BlockId" + i), posList);
            }
        }

        this.bossBlockPos = new BlockPos(nbt.getInt("BossPosX"), nbt.getInt("BossPosY"), nbt.getInt("BossPosZ"));
        if (nbt.getInt("ChestListSize") > 0) {
            this.chestPosList.clear();
            for (int i = 0; i < nbt.getInt("ChestListSize"); i++) {
                this.chestPosList.add(new BlockPos(nbt.getInt("ChestPosX" + i), nbt.getInt("ChestPosY" + i), nbt.getInt("ChestPosZ" + i)));
            }
        }
        if (nbt.getInt("ExitListSize") > 0) {
            this.exitPosList.clear();
            for (int i = 0; i < nbt.getInt("ExitListSize"); i++) {
                this.exitPosList.add(new BlockPos(nbt.getInt("ExitPosX" + i), nbt.getInt("ExitPosY" + i), nbt.getInt("ExitPosZ" + i)));
            }
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putString("DungeonType", this.dungeonType);
        nbt.putString("Difficulty", this.difficulty);
        nbt.putBoolean("DungeonStructureGenerated", this.dungeonStructureGenerated);
        nbt.putInt("DungeonPlayerCount", this.dungeonPlayerUUIDs.size());
        for (int i = 0; i < this.dungeonPlayerUUIDs.size(); i++) {
            nbt.putUuid("PlayerUUID" + i, this.dungeonPlayerUUIDs.get(i));
        }
        nbt.putInt("MaxGroupSize", this.maxGroupSize);
        nbt.putInt("Cooldown", this.cooldown);

        nbt.putInt("BlockMapSize", this.blockBlockPosMap.size());
        if (this.blockBlockPosMap.size() > 0) {
            int blockCount = 0;
            Iterator<Entry<Integer, ArrayList<BlockPos>>> iterator = this.blockBlockPosMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<Integer, ArrayList<BlockPos>> entry = iterator.next();
                nbt.putInt("BlockId" + blockCount, entry.getKey());
                nbt.putInt("BlockListSize" + blockCount, entry.getValue().size());
                for (int i = 0; i < entry.getValue().size(); i++) {
                    nbt.putInt("BlockPosX" + blockCount, entry.getValue().get(i).getX());
                    nbt.putInt("BlockPosY" + blockCount, entry.getValue().get(i).getY());
                    nbt.putInt("BlockPosZ" + blockCount, entry.getValue().get(i).getZ());
                }
                blockCount++;
            }
        }

        nbt.putInt("BossPosX", this.bossBlockPos.getX());
        nbt.putInt("BossPosY", this.bossBlockPos.getY());
        nbt.putInt("BossPosZ", this.bossBlockPos.getZ());

        nbt.putInt("ChestListSize", this.chestPosList.size());
        if (this.chestPosList.size() > 0) {
            for (int i = 0; i < this.chestPosList.size(); i++) {
                nbt.putInt("ChestPosX" + i, this.chestPosList.get(i).getX());
                nbt.putInt("ChestPosY" + i, this.chestPosList.get(i).getY());
                nbt.putInt("ChestPosZ" + i, this.chestPosList.get(i).getZ());
            }
        }

        nbt.putInt("ExitListSize", this.exitPosList.size());
        if (this.exitPosList.size() > 0) {
            for (int i = 0; i < this.exitPosList.size(); i++) {
                nbt.putInt("ExitPosX" + i, this.exitPosList.get(i).getX());
                nbt.putInt("ExitPosY" + i, this.exitPosList.get(i).getY());
                nbt.putInt("ExitPosZ" + i, this.exitPosList.get(i).getZ());
            }
        }
    }

    public void finishDungeon(ServerWorld world, BlockPos pos) {
        List<PlayerEntity> players = world.getPlayers(TargetPredicate.createAttackable().setBaseMaxDistance(64.0), null, new Box(pos).expand(64.0, 64.0, 64.0));
        for (int i = 0; i < players.size(); i++) {
            CriteriaInit.DUNGEON_COMPLETION.trigger((ServerPlayerEntity) players.get(i), this.getDungeonType(), this.getDifficulty());
            // play success sound here

        }

        for (int i = 0; i < this.getExitPosList().size(); i++) {
            // System.out.println(this.getExitPosList().get(i));

            world.setBlockState(this.getExitPosList().get(i), BlockInit.DUNGEON_PORTAL.getDefaultState(), 3);
        }

        // play dungeon sound
        world.setBlockState(this.getBossLootBlockPos(), Blocks.CHEST.getDefaultState(), 3);
        DungeonPlacementHandler.fillChestWithLoot(world.getServer(), world, this.getBossLootBlockPos(), this.getDungeon().getDifficultyBossLootTableMap().get(this.getDifficulty()));
    }

    public Dungeon getDungeon() {
        return Dungeon.getDungeon(this.dungeonType);
    }

    public void setDungeonType(String dungeonType) {
        this.dungeonType = dungeonType;
    }

    public String getDungeonType() {
        return this.dungeonType;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficulty() {
        return this.difficulty;
    }

    public void setDungeonStructureGenerated() {
        this.dungeonStructureGenerated = true;
    }

    public boolean isDungeonStructureGenerated() {
        return this.dungeonStructureGenerated;
    }

    public void joinDungeon(UUID playerUUID) {
        if (!this.dungeonPlayerUUIDs.contains(playerUUID)) {
            this.dungeonPlayerUUIDs.add(playerUUID);
        }
    }

    public void leaveDungeon(UUID playerUUID) {
        this.dungeonPlayerUUIDs.remove(playerUUID);
    }

    public int getDungeonPlayerCount() {
        return this.dungeonPlayerUUIDs.size();
    }

    // Might lead to issues if using "="
    public void setBlockMap(HashMap<Integer, ArrayList<BlockPos>> map) {
        this.blockBlockPosMap = map;
    }

    public HashMap<Integer, ArrayList<BlockPos>> getBlockMap() {
        return this.blockBlockPosMap;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void setMaxGroupSize(int maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
    }

    public int getMaxGroupSize() {
        return this.maxGroupSize;
    }

    public void setBossBlockPos(BlockPos pos) {
        this.bossBlockPos = pos;
    }

    public BlockPos getBossBlockPos() {
        return this.bossBlockPos;
    }

    public void setBossLootBlockPos(BlockPos pos) {
        this.bossLootBlockPos = pos;
    }

    public BlockPos getBossLootBlockPos() {
        return this.bossLootBlockPos;
    }

    public void setChestPosList(List<BlockPos> chestPosList) {
        this.chestPosList = chestPosList;
    }

    public List<BlockPos> getChestPosList() {
        return this.chestPosList;
    }

    public void setExitPosList(List<BlockPos> exitPosList) {
        this.exitPosList = exitPosList;
    }

    public List<BlockPos> getExitPosList() {
        return this.exitPosList;
    }

}
