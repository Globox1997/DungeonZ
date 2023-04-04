package net.dungeonz.block.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.init.BlockInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class DungeonPortalEntity extends BlockEntity {

    private String dungeonType = "";
    private boolean dungeonStructureGenerated = false;
    private List<UUID> dungeonPlayerUUIDs = new ArrayList<UUID>();
    private int maxGroupSize = 0;
    private int cooldown = 0;
    private HashMap<Integer, ArrayList<BlockPos>> blockBlockPosMap = new HashMap<Integer, ArrayList<BlockPos>>();
    private List<BlockPos> chestPosList = new ArrayList<BlockPos>();
    private BlockPos bossBlockPos = new BlockPos(0, 0, 0);

    public DungeonPortalEntity(BlockPos pos, BlockState state) {
        super(BlockInit.DUNGEON_PORTAL_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.dungeonType = nbt.getString("DungeonType");
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
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putString("DungeonType", this.dungeonType);
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
    }

    public void setDungeonType(String dungeonType) {
        this.dungeonType = dungeonType;
    }

    public Dungeon getDungeon() {
        return Dungeon.getDungeon(this.dungeonType);
    }

    public boolean isDungeonStructureGenerated() {
        return this.dungeonStructureGenerated;
    }

    public void setDungeonStructureGenerated() {
        this.dungeonStructureGenerated = true;
    }

    public int getDungeonPlayerCount() {
        return this.dungeonPlayerUUIDs.size();
    }

    public void joinDungeon(UUID playerUUID) {
        if (!this.dungeonPlayerUUIDs.contains(playerUUID)) {
            this.dungeonPlayerUUIDs.add(playerUUID);
        }
    }

    public void leaveDungeon(UUID playerUUID) {
        this.dungeonPlayerUUIDs.remove(playerUUID);
    }

    // Might lead to issues if using "="
    public void setBlockMap(HashMap<Integer, ArrayList<BlockPos>> map) {
        this.blockBlockPosMap = map;
    }

    public HashMap<Integer, ArrayList<BlockPos>> getBlockMap() {
        return this.blockBlockPosMap;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getMaxGroupSize() {
        return this.maxGroupSize;
    }

    public void setMaxGroupSize(int maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
    }

    public BlockPos getBossBlockPos() {
        return this.bossBlockPos;
    }

    public void setBossBlockPos(BlockPos pos) {
        this.bossBlockPos = pos;
    }

    public List<BlockPos> getChestPosList() {
        return this.chestPosList;
    }

    public void setChestPosList(List<BlockPos> chestPosList) {
        this.chestPosList = chestPosList;
    }

}
