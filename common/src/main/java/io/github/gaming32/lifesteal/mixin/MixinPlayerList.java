package io.github.gaming32.lifesteal.mixin;

import com.mojang.authlib.GameProfile;
import io.github.gaming32.lifesteal.Lifesteal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;

@Mixin(PlayerList.class)
public class MixinPlayerList {
    @Shadow @Final private PlayerDataStorage playerIo;

    @Inject(method = "canPlayerLogin", at = @At("HEAD"), cancellable = true)
    private void gameOverNoJoin(SocketAddress socketAddress, GameProfile gameProfile, CallbackInfoReturnable<Component> cir) {
        if (!Lifesteal.CONFIG.getGameOverMode().kickNonOps) return;
        final CompoundTag tag;
        try {
            tag = NbtIo.read(new File(
                ((PlayerDataStorageAccessor)playerIo).getPlayerDir(),
                gameProfile.getId() + ".lifesteal.dat"
            ));
        } catch (IOException e) {
            Lifesteal.LOGGER.error("Failed to read {}'s lifesteal data", gameProfile.getName(), e);
            return;
        }
        if (tag == null) return;
        if (tag.getInt("LivesGain") <= Lifesteal.CONFIG.getGameOverLife()) {
            cir.setReturnValue(Component.literal("You have run out of lives!"));
        }
    }
}
