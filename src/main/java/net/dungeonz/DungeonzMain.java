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
import net.dungeonz.init.JsonReaderInit;
import net.dungeonz.init.SoundInit;
import net.dungeonz.init.WorldInit;
import net.dungeonz.network.DungeonServerPacket;
import net.fabricmc.api.ModInitializer;

public class DungeonzMain implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("DungeonZ");

    public static final List<Dungeon> dungeons = new ArrayList<Dungeon>();

    @Override
    public void onInitialize() {
        BlockInit.init();
        DimensionInit.init();
        DungeonServerPacket.init();
        WorldInit.init();
        JsonReaderInit.init();
        CriteriaInit.init();
        ConfigInit.init();
        SoundInit.init();
    }

}
