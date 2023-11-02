package net.dungeonz.compat;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DungeonReiPlugin implements REIClientPlugin {

    // Check levelz for how to do it if needed in the future
    @Override
    public void registerScreens(ScreenRegistry registry) {
    }
}
