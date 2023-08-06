package io.github.gaming32.lifesteal;

import com.mojang.logging.LogUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.platform.Platform;
import io.github.gaming32.lifesteal.config.LifestealConfig;
import io.github.gaming32.lifesteal.event.PlayerEvents;
import io.github.gaming32.lifesteal.ext.ServerPlayerExt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.quiltmc.qup.json.JsonReader;
import org.quiltmc.qup.json.JsonWriter;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.UUID;

public class Lifesteal {
    public static final String MOD_ID = "lifesteal";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final UUID HEALTH_MODIFIER_ID = UUID.fromString("2f6e4954-01e7-4841-bb19-c03d0ebb207b");
    public static final Path CONFIG_PATH = Platform.getConfigFolder().resolve("lifesteal.json5");

    public static final LifestealConfig CONFIG = new LifestealConfig();

    public static void init() {
        PlayerEvent.PLAYER_CLONE.register((oldPlayer, newPlayer, wonGame) ->
            ((ServerPlayerExt)newPlayer).ls$setLivesGain(((ServerPlayerExt)oldPlayer).ls$getLivesGain())
        );

        PlayerEvents.LOAD_FROM_FILE.register(context -> {
            final CompoundTag tag;
            try {
                tag = NbtIo.read(context.getPlayerFile("lifesteal"));
            } catch (IOException e) {
                LOGGER.error("Failed to read {}'s lifesteal data", context.player().getName().getString(), e);
                return;
            }
            if (tag == null) return;
            ((ServerPlayerExt)context.player()).ls$setLivesGain(tag.getInt("LivesGain"));
        });

        PlayerEvents.SAVE_TO_FILE.register(context -> {
            final CompoundTag tag = new CompoundTag();
            tag.putInt("LivesGain", ((ServerPlayerExt)context.player()).ls$getLivesGain());
            try {
                NbtIo.write(tag, context.getPlayerFile("lifesteal"));
            } catch (IOException e) {
                LOGGER.error("Failed to write {}'s lifesteal data", context.player().getName().getString(), e);
            }
        });

        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (!(entity instanceof ServerPlayerExt player)) {
                return EventResult.pass();
            }
            player.ls$setLivesGain(player.ls$getLivesGain() - 1);
            if (source.getEntity() instanceof ServerPlayerExt killer) {
                killer.ls$setLivesGain(killer.ls$getLivesGain() + 1);
            }
            return EventResult.pass();
        });

        readConfig();
        LOGGER.info("Some quippy comment");
    }

    public static void readConfig() {
        try (JsonReader reader = JsonReader.json5(CONFIG_PATH)) {
            CONFIG.read(reader);
        } catch (IOException e) {
            if (!(e instanceof NoSuchFileException)) {
                LOGGER.error("Failed to read lifesteal.json5", e);
            }
        }
        writeConfig();
    }

    public static void writeConfig() {
        try (JsonWriter writer = JsonWriter.json5(CONFIG_PATH)) {
            CONFIG.write(writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write lifesteal.json5", e);
        }
    }
}
