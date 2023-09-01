package net.dungeonz.init;

import net.dungeonz.access.ClientPlayerAccess;
import net.dungeonz.util.DungeonHelper;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class EventInit {

    public static void init() {
        EntityElytraEvents.ALLOW.register((entity) -> {
            PlayerEntity playerEntity = (PlayerEntity) entity;
            if (playerEntity != null && !playerEntity.isCreative() && playerEntity.getWorld().getRegistryKey() == DimensionInit.DUNGEON_WORLD) {
                if (!playerEntity.getWorld().isClient()) {
                    if (DungeonHelper.getCurrentDungeon((ServerPlayerEntity) playerEntity) != null && !DungeonHelper.getCurrentDungeon((ServerPlayerEntity) playerEntity).isElytraAllowed()) {
                        return true;
                    }
                } else {
                    if (!((ClientPlayerAccess) playerEntity).isElytraAllowed()) {
                        return true;
                    }
                }
            }
            return true;
        });
    }

}
