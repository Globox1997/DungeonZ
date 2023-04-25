package net.dungeonz.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "dungeonz")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class DungeonzConfig implements ConfigData {

    public int test1 = 30;
    public int test2 = 30;
    public int test3 = 30;
    public int test4 = 30;
    public int test5 = 82;
    public int test6 = 82;
    public int test7 = 82;
    public int test8 = 82;
    public int test9 = 82;
    public int test10 = 82;
    public int test11 = 82;
    public int test12 = 82;
    public int test13 = 82;
    @Comment("Crops won't grow")
    public boolean devMode = false;

}
