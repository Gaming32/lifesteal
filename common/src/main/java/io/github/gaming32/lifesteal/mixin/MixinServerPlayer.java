package io.github.gaming32.lifesteal.mixin;

import com.mojang.authlib.GameProfile;
import io.github.gaming32.lifesteal.Lifesteal;
import io.github.gaming32.lifesteal.LifestealUtil;
import io.github.gaming32.lifesteal.ext.ServerPlayerExt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements ServerPlayerExt {
    @Shadow public abstract boolean setGameMode(GameType gameMode);

    @Shadow @Final public MinecraftServer server;
    @Shadow public ServerGamePacketListenerImpl connection;

    @Unique
    private int ls$livesGain;

    public MixinServerPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Override
    public int ls$getLivesGain() {
        return ls$livesGain;
    }

    @Override
    public void ls$setLivesGain(int gain, boolean useOld) {
        final int old = useOld ? ls$livesGain : gain;
        ls$livesGain = gain;
        ls$refreshLivesGain(old);
    }

    @Override
    public void ls$refreshLivesGain(int oldValue) {
        ls$livesGain = LifestealUtil.clamp(ls$livesGain, Lifesteal.CONFIG.getLives());
        final AttributeInstance attribute = getAttribute(Attributes.MAX_HEALTH);
        if (attribute != null) {
            attribute.removeModifier(Lifesteal.HEALTH_MODIFIER_ID);
            if (ls$livesGain != 0) {
                attribute.addPermanentModifier(new AttributeModifier(
                    Lifesteal.HEALTH_MODIFIER_ID, "Lifesteal health",
                    Lifesteal.CONFIG.getHealthPerLife() * ls$livesGain,
                    AttributeModifier.Operation.ADDITION
                ));
            }
        }

        if (ls$livesGain > Lifesteal.CONFIG.getGameOverLife()) return;
        if (Lifesteal.CONFIG.getGameOverMode().forceSpectator) {
            if (oldValue > Lifesteal.CONFIG.getGameOverLife()) {
                sendSystemMessage(
                    Component.literal("You have run out of lives! You are now a spectator.")
                        .withStyle(ChatFormatting.RED)
                );
            }
            if (connection != null) {
                setGameMode(GameType.SPECTATOR);
            }
        }
        if (
            Lifesteal.CONFIG.getGameOverMode().kickNonOps &&
                !server.isSingleplayerOwner(getGameProfile()) &&
                !server.getPlayerList().isOp(getGameProfile())
        ) {
            connection.disconnect(Component.literal("You have run out of lives!"));
        }
    }
}
