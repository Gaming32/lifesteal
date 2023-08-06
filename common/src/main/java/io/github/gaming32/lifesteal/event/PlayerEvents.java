package io.github.gaming32.lifesteal.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.util.UUID;

public class PlayerEvents {
    public static final Event<SaveLoadEvent> LOAD_FROM_FILE = EventFactory.createLoop();
    public static final Event<SaveLoadEvent> SAVE_TO_FILE = EventFactory.createLoop();

    @FunctionalInterface
    public interface SaveLoadEvent {
        void handle(SaveContext context);
    }

    public record SaveContext(ServerPlayer player, File playerDirectory, UUID playerUuid) {
        public File getPlayerFile(String suffix) {
            if ("dat".equals(suffix)) {
                throw new IllegalArgumentException("The suffix 'dat' is reserved");
            }
            return new File(playerDirectory, playerUuid + "." + suffix);
        }
    }
}
