package io.github.gaming32.lifesteal.mixin;

import com.mojang.authlib.GameProfile;
import io.github.gaming32.lifesteal.Lifesteal;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(PlayerList.class)
public class MixinPlayerList {
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "canPlayerLogin", at = @At("HEAD"), cancellable = true)
    private void gameOverNoJoin(SocketAddress socketAddress, GameProfile gameProfile, CallbackInfoReturnable<Component> cir) {
        if (Lifesteal.getLivesGain(server, gameProfile) <= Lifesteal.CONFIG.getGameOverLife()) {
            cir.setReturnValue(Component.literal("You have run out of lives!"));
        }
    }
}
