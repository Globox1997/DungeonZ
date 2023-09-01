package net.dungeonz;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.init.BlockInit;
import net.dungeonz.init.ConfigInit;
import net.dungeonz.init.CriteriaInit;
import net.dungeonz.init.DimensionInit;
import net.dungeonz.init.EventInit;
import net.dungeonz.init.ItemInit;
import net.dungeonz.init.LoaderInit;
import net.dungeonz.init.SoundInit;
import net.dungeonz.init.WorldInit;
import net.dungeonz.network.DungeonServerPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class DungeonzMain implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("DungeonZ");

    public static final boolean isPartyAddonLoaded = FabricLoader.getInstance().isModLoaded("partyaddon");
    public static final boolean isRpgDifficultyLoaded = FabricLoader.getInstance().isModLoaded("rpgdifficulty");

    public static final List<Dungeon> dungeons = new ArrayList<Dungeon>();

    @Override
    public void onInitialize() {
        BlockInit.init();
        DimensionInit.init();
        DungeonServerPacket.init();
        WorldInit.init();
        LoaderInit.init();
        CriteriaInit.init();
        ConfigInit.init();
        SoundInit.init();
        ItemInit.init();
        EventInit.init();
    }

}
