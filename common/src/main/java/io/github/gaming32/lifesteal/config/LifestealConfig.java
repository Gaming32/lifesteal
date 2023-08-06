package io.github.gaming32.lifesteal.config;

import io.github.gaming32.lifesteal.Lifesteal;
import io.github.gaming32.lifesteal.LifestealUtil;
import net.minecraft.advancements.critereon.MinMaxBounds;
import org.quiltmc.qup.json.JsonReader;
import org.quiltmc.qup.json.JsonWriter;

import java.io.IOException;

public class LifestealConfig {
    private GameOverMode gameOverMode = GameOverMode.BAN;
    private double healthPerLife = 2.0;
    private int gameOverLife = -10;
    private MinMaxBounds.Ints lives = MinMaxBounds.Ints.atLeast(-10);
    private boolean respawnAtMaxHealth = true;

    public void read(JsonReader reader) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            final String key;
            switch (key = reader.nextName()) {
                case "gameOverMode" -> gameOverMode = GameOverMode.CODEC.byName(reader.nextString(), GameOverMode.BAN);
                case "healthPerLife" -> healthPerLife = reader.nextDouble();
                case "gameOverLife" -> gameOverLife = reader.nextInt();
                case "lives" -> lives = MinMaxBounds.Ints.fromJson(LifestealUtil.parseQup(reader));
                case "respawnAtMaxHealth" -> respawnAtMaxHealth = reader.nextBoolean();
                default -> {
                    Lifesteal.LOGGER.warn("Unknown key in lifesteal.json5: {}", key);
                    reader.skipValue();
                }
            }
        }
        reader.endObject();
    }

    public void write(JsonWriter writer) throws IOException {
        writer.beginObject();

        writer.comment("NOTE: Lives are added on top of the default max hearts.");
        writer.comment("NOTE: So -10 means to add -10 * healthPerLife HP to the player's max HP.");
        writer.comment("");

        writer.comment("What to do when a player runs out of lives. Default is \"ban\".");
        writer.comment("  - none");
        writer.comment("    Don't do anything. The player will just be stuck with the minimum number of lives unless they gain more.");
        writer.comment("  - spectator");
        writer.comment("    Set the player into spectator. If you use this option, it is recommended to also disable spectatorsGenerateChunks.");
        writer.comment("  - ban");
        writer.comment("    Ban the player from the server. Server operators and the singleplayer owner will be treated as if the mode is \"spectator\".");
        writer.name("gameOverMode").value(gameOverMode.getSerializedName());

        writer.comment("The number of HP one life is worth. Default is 2, meaning one heart.");
        writer.name("healthPerLife").value(healthPerLife);

        writer.comment("Once a player reaches this number of lives, they will be considered to have game overed.");
        writer.comment("Set gameOverMode to \"none\" to disable. Default is -10.");
        writer.name("gameOverLife").value(gameOverLife);

        writer.comment("The range of lives that are valid. The life counter of a player is clamped to within these values.");
        writer.name("lives");
        LifestealUtil.writeQup(writer, lives.serializeToJson());

        writer.comment("Whether players should respawn with all their hearts full, or only with the default 10.");
        writer.name("respawnAtMaxHealth").value(respawnAtMaxHealth);

        writer.endObject();
    }

    public GameOverMode getGameOverMode() {
        return gameOverMode;
    }

    public double getHealthPerLife() {
        return healthPerLife;
    }

    public int getGameOverLife() {
        return gameOverLife;
    }

    public MinMaxBounds.Ints getLives() {
        return lives;
    }

    public boolean isRespawnAtMaxHealth() {
        return respawnAtMaxHealth;
    }
}
