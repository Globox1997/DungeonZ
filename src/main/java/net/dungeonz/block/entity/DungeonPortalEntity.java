package net.dungeonz.block.entity;

import net.dungeonz.block.screen.DungeonPortalScreenHandler;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.dungeon.DungeonPlacementHandler;
import net.dungeonz.init.*;
import net.dungeonz.network.DungeonServerPacket;
import net.dungeonz.network.packet.DungeonPortalPacket;
import net.dungeonz.util.DungeonHelper;
import net.dungeonz.util.InventoryHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

public class DungeonPortalEntity extends EndPortalBlockEntity implements ExtendedScreenHandlerFactory<DungeonPortalPacket> {

    private Text title = Text.translatable("container.dungeon_portal");
    private String dungeonType = "";
    private String difficulty = "";
    private boolean dungeonStructureGenerated = false;
    private List<UUID> dungeonPlayerUuids = new ArrayList<UUID>();
    private List<UUID> deadDungeonPlayerUuids = new ArrayList<UUID>();
    private int maxGroupSize = 0;
    private int minGroupSize = 0;
    private List<UUID> waitingUuids = new ArrayList<UUID>();
    private int requiredLevel = 0;
    private int cooldownTime = 0;
    private int autoKickTime = 0;
    private boolean disableEffects = false;
    private boolean privateGroup = false;
    private HashMap<Integer, ArrayList<BlockPos>> blockBlockPosMap = new HashMap<Integer, ArrayList<BlockPos>>();
    private List<BlockPos> chestPosList = new ArrayList<BlockPos>();
    private List<BlockPos> exitPosList = new ArrayList<BlockPos>();
    private List<BlockPos> gatePosList = new ArrayList<BlockPos>();
    private Map<BlockPos, Integer> movingBlockMap = new HashMap<>();
    private Map<BlockPos, Powered> poweredBlockMap = new HashMap<>();
    private BlockPos bossBlockPos = new BlockPos(0, 0, 0);
    private BlockPos bossLootBlockPos = new BlockPos(0, 0, 0);
    private HashMap<BlockPos, Integer> spawnerPosEntityIdMap = new HashMap<BlockPos, Integer>();
    private HashMap<BlockPos, Integer> replacePosBlockIdMap = new HashMap<BlockPos, Integer>();
    private List<Integer> dungeonEdgeList = new ArrayList<Integer>();
    private int dungeonTeleportCountdown = 0;

    public DungeonPortalEntity(BlockPos pos, BlockState state) {
        super(BlockInit.DUNGEON_PORTAL_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.dungeonType = nbt.getString("DungeonType");
        this.difficulty = nbt.getString("Difficulty");
        this.dungeonStructureGenerated = nbt.getBoolean("DungeonStructureGenerated");
        this.dungeonPlayerUuids.clear();
        for (int i = 0; i < nbt.getInt("DungeonPlayerCount"); i++) {
            this.dungeonPlayerUuids.add(nbt.getUuid("PlayerUUID" + i));
        }
        this.deadDungeonPlayerUuids.clear();
        for (int i = 0; i < nbt.getInt("DeadDungeonPlayerCount"); i++) {
            this.deadDungeonPlayerUuids.add(nbt.getUuid("DeadPlayerUUID" + i));
        }
        this.maxGroupSize = nbt.getInt("MaxGroupSize");
        this.minGroupSize = nbt.getInt("MinGroupSize");
        this.requiredLevel = nbt.getInt("RequiredLevel");
        this.cooldownTime = nbt.getInt("CooldownTime");
        this.autoKickTime = nbt.getInt("AutoKickTime");
        this.disableEffects = nbt.getBoolean("DisableEffects");
        this.privateGroup = nbt.getBoolean("PrivateGroup");
        this.blockBlockPosMap.clear();
        if (nbt.getInt("BlockMapSize") > 0) {
            for (int i = 0; i < nbt.getInt("BlockMapSize"); i++) {
                ArrayList<BlockPos> posList = new ArrayList<>();
                for (int u = 0; u < nbt.getInt("BlockListSize" + i); u++) {
                    int[] blockPos = nbt.getIntArray("BlockPos" + i + "" + u);
                    posList.add(new BlockPos(blockPos[0], blockPos[1], blockPos[2]));
                }
                this.blockBlockPosMap.put(nbt.getInt("BlockId" + i), posList);
            }
        }

        int[] bossPos = nbt.getIntArray("BossPos");
        if (bossPos.length > 0) {
            this.bossBlockPos = new BlockPos(bossPos[0], bossPos[1], bossPos[2]);
        }
        int[] bossLootPos = nbt.getIntArray("BossLootPos");
        if (bossLootPos.length > 0) {
            this.bossLootBlockPos = new BlockPos(bossLootPos[0], bossLootPos[1], bossLootPos[2]);
        }

        if (nbt.getInt("ChestListSize") > 0) {
            this.chestPosList.clear();
            for (int i = 0; i < nbt.getInt("ChestListSize"); i++) {
                int[] chestPos = nbt.getIntArray("ChestPos" + i);
                this.chestPosList.add(new BlockPos(chestPos[0], chestPos[1], chestPos[2]));
            }
        }

        if (nbt.getInt("ExitListSize") > 0) {
            this.exitPosList.clear();
            for (int i = 0; i < nbt.getInt("ExitListSize"); i++) {
                int[] exitPos = nbt.getIntArray("ExitPos" + i);
                this.exitPosList.add(new BlockPos(exitPos[0], exitPos[1], exitPos[2]));
            }
        }

        if (nbt.getInt("SpawnerMapSize") > 0) {
            this.spawnerPosEntityIdMap.clear();
            for (int i = 0; i < nbt.getInt("SpawnerListSize"); i++) {
                int[] spawnerPos = nbt.getIntArray("SpawnerPos" + i);
                this.spawnerPosEntityIdMap.put(new BlockPos(spawnerPos[0], spawnerPos[1], spawnerPos[2]), spawnerPos[3]);
            }
        }

        if (nbt.getInt("ReplacePosSize") > 0) {
            this.replacePosBlockIdMap.clear();
            for (int i = 0; i < nbt.getInt("ReplacePosSize"); i++) {
                int[] replacePos = nbt.getIntArray("ReplacePos" + i);
                this.replacePosBlockIdMap.put(new BlockPos(replacePos[0], replacePos[1], replacePos[2]), replacePos[3]);
            }
        }

        if (nbt.getInt("MovingPosSize") > 0) {
            this.movingBlockMap.clear();
            for (int i = 0; i < nbt.getInt("MovingPosSize"); i++) {
                int[] movingPos = nbt.getIntArray("MovingPos" + i);
                this.movingBlockMap.put(new BlockPos(movingPos[0], movingPos[1], movingPos[2]), movingPos[3]);
            }
        }

        if (nbt.getInt("PoweredPosSize") > 0) {
            this.poweredBlockMap.clear();
            for (int i = 0; i < nbt.getInt("PoweredPosSize"); i++) {
                int[] poweredPos = nbt.getIntArray("PoweredPos" + i);
                boolean isPowered = poweredPos[4] == 1;
                this.poweredBlockMap.put(new BlockPos(poweredPos[0], poweredPos[1], poweredPos[2]), new Powered(poweredPos[3], isPowered, poweredPos[5]));
            }
        }

        if (nbt.getInt("DungeonEdgeSize") > 0) {
            this.dungeonEdgeList.clear();
            for (int i = 0; i < nbt.getInt("DungeonEdgeSize") / 3; i++) {
                int[] dungeonEdgePos = nbt.getIntArray("DungeonEdge" + i);
                this.dungeonEdgeList.add(dungeonEdgePos[0]);
                this.dungeonEdgeList.add(dungeonEdgePos[1]);
                this.dungeonEdgeList.add(dungeonEdgePos[2]);
            }
        }

        if (nbt.getInt("GateListSize") > 0) {
            this.gatePosList.clear();
            for (int i = 0; i < nbt.getInt("GateListSize"); i++) {
                int[] gatePos = nbt.getIntArray("GatePos" + i);
                this.gatePosList.add(new BlockPos(gatePos[0], gatePos[1], gatePos[2]));
            }
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putString("DungeonType", this.dungeonType);
        nbt.putString("Difficulty", this.difficulty);
        nbt.putBoolean("DungeonStructureGenerated", this.dungeonStructureGenerated);
        nbt.putInt("DungeonPlayerCount", this.dungeonPlayerUuids.size());
        for (int i = 0; i < this.dungeonPlayerUuids.size(); i++) {
            nbt.putUuid("PlayerUUID" + i, this.dungeonPlayerUuids.get(i));
        }
        nbt.putInt("DeadDungeonPlayerCount", this.deadDungeonPlayerUuids.size());
        for (int i = 0; i < this.deadDungeonPlayerUuids.size(); i++) {
            nbt.putUuid("DeadPlayerUUID" + i, this.deadDungeonPlayerUuids.get(i));
        }
        nbt.putInt("MaxGroupSize", this.maxGroupSize);
        nbt.putInt("MinGroupSize", this.minGroupSize);
        nbt.putInt("RequiredLevel", this.requiredLevel);
        nbt.putInt("CooldownTime", this.cooldownTime);
        nbt.putInt("AutoKickTime", this.autoKickTime);
        nbt.putBoolean("DisableEffects", this.disableEffects);
        nbt.putBoolean("PrivateGroup", this.privateGroup);

        nbt.putInt("BlockMapSize", this.blockBlockPosMap.size());
        if (!this.blockBlockPosMap.isEmpty()) {
            int blockCount = 0;
            for (Entry<Integer, ArrayList<BlockPos>> entry : this.blockBlockPosMap.entrySet()) {
                nbt.putInt("BlockId" + blockCount, entry.getKey());
                nbt.putInt("BlockListSize" + blockCount, entry.getValue().size());
                for (int i = 0; i < entry.getValue().size(); i++) {
                    nbt.putIntArray("BlockPos" + blockCount + "" + i, List.of(entry.getValue().get(i).getX(), entry.getValue().get(i).getY(), entry.getValue().get(i).getZ()));
                }
                blockCount++;
            }
        }
        nbt.putIntArray("BossPos", List.of(this.bossBlockPos.getX(), this.bossBlockPos.getY(), this.bossBlockPos.getZ()));
        nbt.putIntArray("BossLootPos", List.of(this.bossLootBlockPos.getX(), this.bossLootBlockPos.getY(), this.bossLootBlockPos.getZ()));

        nbt.putInt("ChestListSize", this.chestPosList.size());
        if (!this.chestPosList.isEmpty()) {
            for (int i = 0; i < this.chestPosList.size(); i++) {
                nbt.putIntArray("ChestPos" + i, List.of(this.chestPosList.get(i).getX(), this.chestPosList.get(i).getY(), this.chestPosList.get(i).getZ()));
            }
        }

        nbt.putInt("ExitListSize", this.exitPosList.size());
        if (!this.exitPosList.isEmpty()) {
            for (int i = 0; i < this.exitPosList.size(); i++) {
                nbt.putIntArray("ExitPos" + i, List.of(this.exitPosList.get(i).getX(), this.exitPosList.get(i).getY(), this.exitPosList.get(i).getZ()));
            }
        }

        nbt.putInt("SpawnerMapSize", this.spawnerPosEntityIdMap.size());
        if (!this.spawnerPosEntityIdMap.isEmpty()) {
            Iterator<Entry<BlockPos, Integer>> iterator = this.spawnerPosEntityIdMap.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Entry<BlockPos, Integer> entry = iterator.next();
                nbt.putIntArray("SpawnerPos" + count, List.of(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ(), entry.getValue()));
                count++;
            }
        }

        nbt.putInt("ReplacePosSize", this.replacePosBlockIdMap.size());
        if (!this.replacePosBlockIdMap.isEmpty()) {
            Iterator<Entry<BlockPos, Integer>> iterator = this.replacePosBlockIdMap.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Entry<BlockPos, Integer> entry = iterator.next();
                nbt.putIntArray("ReplacePos" + count, List.of(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ(), entry.getValue()));
                count++;
            }
        }

        nbt.putInt("MovingPosSize", this.movingBlockMap.size());
        if (!this.movingBlockMap.isEmpty()) {
            Iterator<Entry<BlockPos, Integer>> iterator = this.movingBlockMap.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Entry<BlockPos, Integer> entry = iterator.next();
                nbt.putIntArray("MovingPos" + count, List.of(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ(), entry.getValue()));
                count++;
            }
        }

        nbt.putInt("PoweredPosSize", this.poweredBlockMap.size());
        if (!this.poweredBlockMap.isEmpty()) {
            Iterator<Entry<BlockPos, Powered>> iterator = this.poweredBlockMap.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Entry<BlockPos, Powered> entry = iterator.next();
                int isPowered = entry.getValue().getPowered() ? 1 : 0;
                nbt.putIntArray("PoweredPos" + count, List.of(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ(), entry.getValue().getBlockId(), isPowered, entry.getValue().getFacing()));
                count++;
            }
        }

        nbt.putInt("DungeonEdgeSize", this.dungeonEdgeList.size());
        if (!this.dungeonEdgeList.isEmpty()) {
            for (int i = 0; i < this.dungeonEdgeList.size() / 3; i++) {
                nbt.putIntArray("DungeonEdge" + i, List.of(this.dungeonEdgeList.get(3 * i), this.dungeonEdgeList.get(1 + 3 * i), this.dungeonEdgeList.get(2 + 3 * i)));
            }
        }

        nbt.putInt("GateListSize", this.gatePosList.size());
        if (!this.gatePosList.isEmpty()) {
            for (int i = 0; i < this.gatePosList.size(); i++) {
                nbt.putIntArray("GatePos" + i, List.of(this.gatePosList.get(i).getX(), this.gatePosList.get(i).getY(), this.gatePosList.get(i).getZ()));
            }
        }
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, DungeonPortalEntity blockEntity) {
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, DungeonPortalEntity blockEntity) {
        if (blockEntity.getDungeonPlayerCount() > 0) {
            if (blockEntity.autoKickTime == 0) {
                blockEntity.autoKickTime = (int) world.getTime() + 432000;
            } else if (blockEntity.autoKickTime < (int) world.getTime()) {
                if (blockEntity.getDungeon() != null) {
                    blockEntity.setCooldownTime(blockEntity.getDungeon().getCooldown() + (int) blockEntity.getWorld().getTime());
                    for (int i = 0; i < blockEntity.getDungeonPlayerUuids().size(); i++) {
                        ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(blockEntity.getDungeonPlayerUuids().get(i));
                        if (DungeonHelper.getCurrentDungeon(player) != null) {
                            DungeonHelper.teleportOutOfDungeon(player);
                            player.sendMessage(Text.translatable("text.dungeonz.dungeon_autokick"));
                        }
                    }
                }
                blockEntity.getDungeonPlayerUuids().clear();
                blockEntity.getDeadDungeonPlayerUUIDs().clear();
                blockEntity.autoKickTime = 0;
            }
        } else if (blockEntity.autoKickTime != 0) {
            blockEntity.autoKickTime = 0;
        }
        if (blockEntity.dungeonTeleportCountdown >= 1) {
            if (blockEntity.dungeonTeleportCountdown % 20 == 0) {
                for (int i = 0; i < blockEntity.getWaitingUuids().size(); i++) {
                    if (((ServerWorld) blockEntity.getWorld()).getEntity(blockEntity.getWaitingUuids().get(i)) != null
                            && ((ServerWorld) blockEntity.getWorld()).getEntity(blockEntity.getWaitingUuids().get(i)) instanceof ServerPlayerEntity serverPlayerEntity) {
                        DungeonServerPacket.writeS2CDungeonTeleportCountdown(serverPlayerEntity, blockEntity.dungeonTeleportCountdown);
                    }
                }

            }
            blockEntity.dungeonTeleportCountdown--;

            if (blockEntity.dungeonTeleportCountdown == (ConfigInit.CONFIG.defaultDungeonTeleportCountdown / 2)) {
//                CompletableFuture.runAsync(() -> DungeonPlacementHandler.refreshDungeon(((ServerWorld) blockEntity.getWorld()).getServer(), blockEntity.getWorld().getServer().getWorld(DimensionInit.DUNGEON_WORLD), blockEntity,
//                        blockEntity.getDungeon(), blockEntity.getDifficulty(), blockEntity.getDisableEffects()));
                DungeonPlacementHandler.refreshDungeon(((ServerWorld) blockEntity.getWorld()).getServer(), blockEntity.getWorld().getServer().getWorld(DimensionInit.DUNGEON_WORLD), blockEntity,
                        blockEntity.getDungeon(), blockEntity.getDifficulty(), blockEntity.getDisableEffects());
            }

            if (blockEntity.dungeonTeleportCountdown == 0) {
                for (int i = 0; i < blockEntity.getWaitingUuids().size(); i++) {
                    if (((ServerWorld) blockEntity.getWorld()).getEntity(blockEntity.getWaitingUuids().get(i)) != null
                            && ((ServerWorld) blockEntity.getWorld()).getEntity(blockEntity.getWaitingUuids().get(i)) instanceof ServerPlayerEntity serverPlayerEntity) {
                        DungeonHelper.teleportPlayer(serverPlayerEntity, blockEntity.getWorld().getServer().getWorld(DimensionInit.DUNGEON_WORLD), blockEntity, blockEntity.getPos());
                    }
                }
                blockEntity.getWaitingUuids().clear();
            }
        }
    }

    @Override
    public Text getDisplayName() {
        if (this.getDungeon() != null) {
            return Text.translatable("dungeon." + this.getDungeonType());
        }
        return title;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(WrapperLookup registryLookup) {
        return this.createNbt(registryLookup);
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new DungeonPortalScreenHandler(syncId, playerInventory, this, ScreenHandlerContext.create(world, pos));
    }

    @Override
    public boolean shouldDrawSide(Direction direction) {
        return true;
    }

    @Override
    public DungeonPortalPacket getScreenOpeningData(ServerPlayerEntity player) {
        List<String> difficulties = new ArrayList<String>();
        Map<String, List<ItemStack>> possibleLoot = new HashMap<>();
        Map<String, List<ItemStack>> requiredItemStacks = new HashMap<>();
        Optional<Identifier> backgroundId = Optional.empty();
        int requiredLevel = 0;
        if (this.getDungeon() != null) {
            difficulties = this.getDungeon().getDifficultyList();
            possibleLoot = DungeonHelper.getPossibleLootItemStackMap(this.getDungeon(), player.getServer());
            requiredItemStacks = DungeonHelper.getRequiredItemStackList(this.getDungeon());
            backgroundId = Optional.ofNullable(this.getDungeon().getBackgroundId());
            requiredLevel = this.getDungeon().getRequiredLevel();
        }

        return new DungeonPortalPacket(this.pos, this.getDungeonPlayerUuids(), this.getDeadDungeonPlayerUUIDs(), difficulties, possibleLoot, requiredItemStacks, this.getMaxGroupSize(),
                this.getMinGroupSize(), this.getWaitingUuids().size(), requiredLevel, this.getCooldownTime(), this.getDifficulty(), this.getDisableEffects(), this.getPrivateGroup(), backgroundId);
    }

    public void finishDungeon(ServerWorld world, BlockPos pos) {
        List<PlayerEntity> players = world.getPlayers(TargetPredicate.createAttackable().setBaseMaxDistance(64.0), null, new Box(pos).expand(64.0, 64.0, 64.0));
        for (PlayerEntity player : players) {
            CriteriaInit.DUNGEON_COMPLETION.trigger((ServerPlayerEntity) player, this.getDungeonType(), this.getDifficulty());
        }
        world.playSound(null, pos, SoundInit.DUNGEON_COMPLETION_EVENT, SoundCategory.BLOCKS, 1.0f, 0.9f + world.getRandom().nextFloat() * 0.2f);

        for (int i = 0; i < this.getExitPosList().size(); i++) {
            world.setBlockState(this.getExitPosList().get(i), BlockInit.DUNGEON_PORTAL.getDefaultState(), 3);
        }

        world.setBlockState(this.getBossLootBlockPos(), Blocks.CHEST.getDefaultState(), 3);
        InventoryHelper.fillInventoryWithLoot(world.getServer(), world, this.getBossLootBlockPos(), this.getDungeon().getDifficultyBossLootTableMap().get(this.getDifficulty()),
                this.getDisableEffects());

        this.setCooldownTime(this.getDungeon().getCooldown() + (int) this.getWorld().getTime());
        markDirty();
    }

    @Nullable
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

    public void joinDungeon(UUID playerUuid) {
        if (!this.dungeonPlayerUuids.contains(playerUuid)) {
            this.dungeonPlayerUuids.add(playerUuid);
        }
    }

    public void leaveDungeon(UUID playerUuid) {
        this.dungeonPlayerUuids.remove(playerUuid);
    }

    public int getDungeonPlayerCount() {
        return this.dungeonPlayerUuids.size();
    }

    public void setDungeonPlayerUuids(List<UUID> dungeonPlayerUuids) {
        this.dungeonPlayerUuids = dungeonPlayerUuids;
    }

    public List<UUID> getDungeonPlayerUuids() {
        return this.dungeonPlayerUuids;
    }

    public void addDeadDungeonPlayerUuids(UUID deadDungeonPlayerUuids) {
        this.deadDungeonPlayerUuids.add(deadDungeonPlayerUuids);
    }

    public void setDeadDungeonPlayerUuids(List<UUID> deadDungeonPlayerUuids) {
        this.deadDungeonPlayerUuids = deadDungeonPlayerUuids;
    }

    public List<UUID> getDeadDungeonPlayerUUIDs() {
        return this.deadDungeonPlayerUuids;
    }

    // Might lead to issues if using "="
    public void setBlockMap(HashMap<Integer, ArrayList<BlockPos>> map) {
        this.blockBlockPosMap = map;
    }

    public HashMap<Integer, ArrayList<BlockPos>> getBlockMap() {
        return this.blockBlockPosMap;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public int getRequiredLevel() {
        return this.requiredLevel;
    }

    public void setCooldownTime(int cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public int getCooldownTime() {
        return this.cooldownTime;
    }

    public boolean isOnCooldown(int currentTime) {
        if (this.cooldownTime <= currentTime) {
            return false;
        }
        return true;
    }

    public void setMaxGroupSize(int maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
    }

    public void setMinGroupSize(int minGroupSize) {
        this.minGroupSize = minGroupSize;
    }

    public List<UUID> getWaitingUuids() {
        return this.waitingUuids;
    }

    public void addWaitingUuid(UUID uuid) {
        if (!this.waitingUuids.contains(uuid)) {
            this.waitingUuids.add(uuid);
        }
    }

    public int getMaxGroupSize() {
        return this.maxGroupSize;
    }

    public int getMinGroupSize() {
        return this.minGroupSize;
    }

    public void setDisableEffects(boolean disableEffects) {
        this.disableEffects = disableEffects;
    }

    public boolean getDisableEffects() {
        return this.disableEffects;
    }

    public void setPrivateGroup(boolean privateGroup) {
        this.privateGroup = privateGroup;
    }

    public boolean getPrivateGroup() {
        return this.privateGroup;
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

    public void setGatePosList(List<BlockPos> gatePosList) {
        this.gatePosList = gatePosList;
    }

    public List<BlockPos> getGatePosList() {
        return this.gatePosList;
    }

    public void setMovingBlockMap(Map<BlockPos, Integer> movingBlockMap) {
        this.movingBlockMap = movingBlockMap;
    }

    public Map<BlockPos, Integer> getMovingBlockMap() {
        return this.movingBlockMap;
    }

    public void setPoweredBlockMap(Map<BlockPos, Powered> poweredBlockMap) {
        this.poweredBlockMap = poweredBlockMap;
    }

    public Map<BlockPos, Powered> getPoweredBlockMap() {
        return this.poweredBlockMap;
    }

    public void setExitPosList(List<BlockPos> exitPosList) {
        this.exitPosList = exitPosList;
    }

    public List<BlockPos> getExitPosList() {
        return this.exitPosList;
    }

    public void addDungeonEdge(int edgeX, int edgeY, int edgeZ) {
        this.dungeonEdgeList.add(edgeX);
        this.dungeonEdgeList.add(edgeY);
        this.dungeonEdgeList.add(edgeZ);
    }

    public List<Integer> getDungeonEdgeList() {
        return this.dungeonEdgeList;
    }

    public void setSpawnerPosEntityIdMap(HashMap<BlockPos, Integer> spawnerPosEntityIdMap) {
        this.spawnerPosEntityIdMap = spawnerPosEntityIdMap;
    }

    public HashMap<BlockPos, Integer> getSpawnerPosEntityIdMap() {
        return this.spawnerPosEntityIdMap;
    }

    public void setReplaceBlockIdMap(HashMap<BlockPos, Integer> replacePosBlockIdMap) {
        this.replacePosBlockIdMap = replacePosBlockIdMap;
    }

    public void addReplaceBlockId(BlockPos pos, Block block) {
        this.replacePosBlockIdMap.put(pos, Registries.BLOCK.getRawId(block));
    }

    public HashMap<BlockPos, Integer> getReplaceBlockIdMap() {
        return this.replacePosBlockIdMap;
    }

    public void startDungeonTeleportCountdown(ServerWorld dungeonWorld) {
        this.dungeonTeleportCountdown = ConfigInit.CONFIG.defaultDungeonTeleportCountdown;

        boolean isDungeonStructureGenerated = this.isDungeonStructureGenerated();
        if (!isDungeonStructureGenerated) {
            this.setDungeonStructureGenerated();
            DungeonPlacementHandler.generateDungeonStructure(dungeonWorld, new BlockPos(0, 0, 0).add(this.getPos().getX() * 16, 100, this.getPos().getZ() * 16), this);
        } else {
            DungeonPlacementHandler.prepareDungeon(dungeonWorld, this);
        }
        this.markDirty();
    }

    public int getdungeonTeleportCountdown() {
        return this.dungeonTeleportCountdown;
    }

    public static class Powered {
        private final int blockId;
        private final boolean powered;
        private final int facing;

        public Powered(int blockId, boolean powered, int facing) {
            this.blockId = blockId;
            this.powered = powered;
            this.facing = facing;
        }

        public int getBlockId() {
            return blockId;
        }

        public boolean getPowered() {
            return powered;
        }

        public int getFacing() {
            return facing;
        }
    }

}
