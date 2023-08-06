package io.github.gaming32.lifesteal.config;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum GameOverMode implements StringRepresentable {
    NONE(false, false),
    SPECTATOR(true, false),
    BAN(true, true);

    @SuppressWarnings("deprecation")
    public static final EnumCodec<GameOverMode> CODEC = StringRepresentable.fromEnum(GameOverMode::values);

    private final String lowercase = name().toLowerCase(Locale.ROOT);

    public final boolean forceSpectator;
    public final boolean kickNonOps;

    GameOverMode(boolean forceSpectator, boolean kickNonOps) {
        this.forceSpectator = forceSpectator;
        this.kickNonOps = kickNonOps;
    }

    @NotNull
    @Override
    public String getSerializedName() {
        return lowercase;
    }

    @Override
    public String toString() {
        return getSerializedName();
    }
}
