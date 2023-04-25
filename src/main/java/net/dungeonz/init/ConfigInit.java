package net.dungeonz.init;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.dungeonz.config.DungeonzConfig;

public class ConfigInit {

    public static DungeonzConfig CONFIG = new DungeonzConfig();

    public static void init() {
        AutoConfig.register(DungeonzConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(DungeonzConfig.class).getConfig();

    }
}
