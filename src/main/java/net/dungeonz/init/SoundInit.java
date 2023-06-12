package net.dungeonz.init;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundInit {

    public static SoundEvent DUNGEON_COMPLETION_EVENT = register("dungeonz:dungeon_completion");
    public static SoundEvent DUNGEON_GATE_UNLOCK_EVENT = register("dungeonz:dungeon_gate_unlock");

    private static SoundEvent register(String id) {
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(new Identifier(id)));
    }

    public static void init() {
    }

}
