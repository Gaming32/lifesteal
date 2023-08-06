package io.github.gaming32.lifesteal.mixin.fabric;

import io.github.gaming32.lifesteal.event.PlayerEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(PlayerDataStorage.class)
public class MixinPlayerDataStorage {
    @Shadow @Final private File playerDir;

    @Inject(method = "load", at = @At("RETURN"))
    private void loadFromFileEvent(Player player, CallbackInfoReturnable<CompoundTag> cir) {
        PlayerEvents.LOAD_FROM_FILE.invoker().handle(new PlayerEvents.SaveContext((ServerPlayer)player, playerDir, player.getUUID()));
    }

    @Inject(
        method = "save",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/Util;safeReplaceFile(Ljava/io/File;Ljava/io/File;Ljava/io/File;)V",
            shift = At.Shift.AFTER
        )
    )
    private void saveToFileEvent(Player player, CallbackInfo ci) {
        PlayerEvents.SAVE_TO_FILE.invoker().handle(new PlayerEvents.SaveContext((ServerPlayer)player, playerDir, player.getUUID()));
    }
}
