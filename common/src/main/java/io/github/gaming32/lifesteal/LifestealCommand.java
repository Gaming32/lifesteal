package io.github.gaming32.lifesteal;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import io.github.gaming32.lifesteal.ext.ServerPlayerExt;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Collections;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class LifestealCommand {
    public static final Dynamic2CommandExceptionType ERROR_OUT_OF_RANGE = new Dynamic2CommandExceptionType(
        (a, b) -> Component.literal(a + " must be in range " + LifestealUtil.toString((MinMaxBounds<?>)b))
    );

    public static void register(
        CommandDispatcher<CommandSourceStack> dispatcher,
        CommandBuildContext registry,
        Commands.CommandSelection selection
    ) {
        dispatcher.register(literal("lifesteal")
            .requires(ctx -> ctx.hasPermission(2))
            .then(literal("lives")
                .then(literal("get")
                    .executes(ctx -> getLives(ctx, getDefaultPlayer(ctx)))
                    .then(argument("players", GameProfileArgument.gameProfile())
                        .executes(ctx -> getLives(ctx, GameProfileArgument.getGameProfiles(ctx, "players")))
                    )
                )
                .then(literal("set")
                    .then(argument("lives", IntegerArgumentType.integer())
                        .executes(ctx -> setLives(ctx, getDefaultPlayer(ctx)))
                        .then(argument("players", GameProfileArgument.gameProfile())
                            .executes(ctx -> setLives(ctx, GameProfileArgument.getGameProfiles(ctx, "players")))
                        )
                    )
                )
                .then(literal("add")
                    .then(argument("lives", IntegerArgumentType.integer())
                        .executes(ctx -> addLives(ctx, getDefaultPlayer(ctx)))
                        .then(argument("players", GameProfileArgument.gameProfile())
                            .executes(ctx -> addLives(ctx, GameProfileArgument.getGameProfiles(ctx, "players")))
                        )
                    )
                )
                .then(literal("reset")
                    .executes(ctx -> resetLives(ctx, getDefaultPlayer(ctx)))
                    .then(argument("players", GameProfileArgument.gameProfile())
                        .executes(ctx -> resetLives(ctx, GameProfileArgument.getGameProfiles(ctx, "players")))
                    )
                )
            )
            .then(literal("reload")
                .executes(ctx -> {
                    if (Lifesteal.readConfig()) {
                        for (final ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                            ((ServerPlayerExt)player).ls$refreshLivesGain();
                        }
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("Successfully reloaded the Lifesteal config"),
                            true
                        );
                        return Command.SINGLE_SUCCESS;
                    } else {
                        ctx.getSource().sendFailure(Component.literal(
                            "Failed to load the Lifesteal config. Consult the logs for details."
                        ));
                        return 0;
                    }
                })
            )
        );
    }

    private static Collection<GameProfile> getDefaultPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return Collections.singleton(ctx.getSource().getPlayerOrException().getGameProfile());
    }

    private static int getLives(CommandContext<CommandSourceStack> ctx, Collection<GameProfile> players) {
        final MinecraftServer server = ctx.getSource().getServer();
        ctx.getSource().sendSuccess(
            () -> players.stream()
                .map(p -> Component.literal(p.getName() + " has " + Lifesteal.getLivesGain(server, p) + " lives gain")
                )
                .collect(LifestealUtil.componentJoin(Component.literal("\n"))),
            false
        );
        return players.stream()
            .mapToInt(g -> Lifesteal.getLivesGain(server, g))
            .sum();
    }

    private static int setLives(CommandContext<CommandSourceStack> ctx, Collection<GameProfile> players) throws CommandSyntaxException {
        final int lives = IntegerArgumentType.getInteger(ctx, "lives");
        final MinMaxBounds.Ints allowed = Lifesteal.CONFIG.getLives();
        if (!allowed.matches(lives)) {
            throw ERROR_OUT_OF_RANGE.create("Lives count", allowed);
        }
        final MinecraftServer server = ctx.getSource().getServer();
        for (final GameProfile player : players) {
            Lifesteal.setLivesGain(server, player, lives);
        }
        ctx.getSource().sendSuccess(
            () -> Component.literal("Set " + players.size() + " player(s)' lives gain to " + lives),
            true
        );
        return players.size();
    }

    private static int addLives(CommandContext<CommandSourceStack> ctx, Collection<GameProfile> players) {
        final int lives = IntegerArgumentType.getInteger(ctx, "lives");
        final MinecraftServer server = ctx.getSource().getServer();
        for (final GameProfile player : players) {
            Lifesteal.setLivesGain(server, player, Lifesteal.getLivesGain(server, player) + lives);
        }
        ctx.getSource().sendSuccess(
            () -> players.stream()
                .map(p -> Component.literal("Set " + p.getName() + "'s lives gain to " + Lifesteal.getLivesGain(server, p)))
                .collect(LifestealUtil.componentJoin(Component.literal("\n"))),
            true
        );
        return players.size();
    }

    private static int resetLives(CommandContext<CommandSourceStack> ctx, Collection<GameProfile> players) {
        final MinecraftServer server = ctx.getSource().getServer();
        for (final GameProfile player : players) {
            Lifesteal.setLivesGain(server, player, 0);
        }
        ctx.getSource().sendSuccess(
            () -> Component.literal("Reset " + players.size() + " player(s)' lives gain to 0"),
            true
        );
        return players.size();
    }
}
