package net.dungeonz.init;

import net.dungeonz.criteria.DungeonBossCriterion;
import net.fabricmc.fabric.mixin.object.builder.CriteriaAccessor;

public class CriteriaInit {

    public static final DungeonBossCriterion DUNGEON_COMPLETION = CriteriaAccessor.callRegister(new DungeonBossCriterion());

    public static void init() {
    }
}
