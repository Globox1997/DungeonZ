package net.dungeonz.init;

import net.dungeonz.util.DungeonHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandInit {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            dispatcher.register((CommandManager.literal("dungeon").requires((serverCommandSource) -> {
                return serverCommandSource.hasPermissionLevel(0);
            })).then(CommandManager.literal("leave").executes((commandContext) -> {
                return executeDungeonCommand(commandContext.getSource());
            })));
        });
    }

    private static int executeDungeonCommand(ServerCommandSource source) {
        if (source.getPlayer() != null) {
            if (DungeonHelper.getCurrentDungeon(source.getPlayer()) != null) {
                DungeonHelper.teleportOutOfDungeon(source.getPlayer());
            } else {
                source.sendFeedback(() -> Text.translatable("text.dungeonz.dungeon_missing"), false);
            }
        }

        return 1;
    }

}
