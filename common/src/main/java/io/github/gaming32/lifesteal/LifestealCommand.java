package io.github.gaming32.lifesteal;

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
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
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
                    .executes(ctx -> getLives(ctx, Collections.singleton(ctx.getSource().getPlayerOrException())))
                    .then(argument("players", EntityArgument.players())
                        .executes(ctx -> getLives(ctx, EntityArgument.getPlayers(ctx, "players")))
                    )
                )
                .then(literal("set")
                    .then(argument("lives", IntegerArgumentType.integer())
                        .executes(ctx -> setLives(ctx, Collections.singleton(ctx.getSource().getPlayerOrException())))
                        .then(argument("players", EntityArgument.players())
                            .executes(ctx -> setLives(ctx, EntityArgument.getPlayers(ctx, "players")))
                        )
                    )
                )
                .then(literal("add")
                    .then(argument("lives", IntegerArgumentType.integer())
                        .executes(ctx -> addLives(ctx, Collections.singleton(ctx.getSource().getPlayerOrException())))
                        .then(argument("players", EntityArgument.players())
                            .executes(ctx -> addLives(ctx, EntityArgument.getPlayers(ctx, "players")))
                        )
                    )
                )
                .then(literal("reset")
                    .executes(ctx -> resetLives(ctx, Collections.singleton(ctx.getSource().getPlayerOrException())))
                    .then(argument("players", EntityArgument.players())
                        .executes(ctx -> resetLives(ctx, EntityArgument.getPlayers(ctx, "players")))
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

    private static int getLives(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players) {
        ctx.getSource().sendSuccess(
            () -> players.stream()
                .map(p -> p.getDisplayName()
                    .copy()
                    .append(" has " + ((ServerPlayerExt)p).ls$getLivesGain() + " lives gain")
                )
                .collect(LifestealUtil.componentJoin(Component.literal("\n"))),
            false
        );
        return players.stream()
            .map(p -> (ServerPlayerExt)p)
            .mapToInt(ServerPlayerExt::ls$getLivesGain)
            .sum();
    }

    private static int setLives(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players) throws CommandSyntaxException {
        final int lives = IntegerArgumentType.getInteger(ctx, "lives");
        final MinMaxBounds.Ints allowed = Lifesteal.CONFIG.getLives();
        if (!allowed.matches(lives)) {
            throw ERROR_OUT_OF_RANGE.create("Lives count", allowed);
        }
        for (final ServerPlayer player : players) {
            ((ServerPlayerExt)player).ls$setLivesGain(lives);
        }
        ctx.getSource().sendSuccess(
            () -> Component.literal("Set " + players.size() + " player(s)' lives gain to " + lives),
            true
        );
        return players.size();
    }

    private static int addLives(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players) {
        final int lives = IntegerArgumentType.getInteger(ctx, "lives");
        for (final ServerPlayer player : players) {
            ((ServerPlayerExt)player).ls$setLivesGain(((ServerPlayerExt)player).ls$getLivesGain() + lives);
        }
        ctx.getSource().sendSuccess(
            () -> players.stream()
                .map(p -> Component.literal("Set ")
                    .append(p.getDisplayName())
                    .append("'s lives gain to " + ((ServerPlayerExt)p).ls$getLivesGain())
                )
                .collect(LifestealUtil.componentJoin(Component.literal("\n"))),
            true
        );
        return players.size();
    }

    private static int resetLives(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players) {
        for (final ServerPlayer player : players) {
            ((ServerPlayerExt)player).ls$setLivesGain(0);
        }
        ctx.getSource().sendSuccess(
            () -> Component.literal("Reset " + players.size() + " player(s)' lives gain to 0"),
            true
        );
        return players.size();
    }
}
