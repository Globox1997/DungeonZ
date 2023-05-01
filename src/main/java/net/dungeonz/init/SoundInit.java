package net.dungeonz.init;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SoundInit {

    public static SoundEvent DUNGEON_COMPLETION_EVENT = register("dungeonz:dungeon_completion");
    public static SoundEvent DUNGEON_GATE_UNLOCK_EVENT = register("dungeonz:dungeon_gate_unlock");

    private static SoundEvent register(String id) {
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(new Identifier(id)));
    }

    public static void init() {
    }

}
