package net.dungeonz.init;

import net.dungeonz.data.DungeonLoader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class JsonReaderInit {

    public static void init() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DungeonLoader());
    }
}
