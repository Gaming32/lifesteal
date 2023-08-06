package io.github.gaming32.lifesteal.mixin.fabric;

import io.github.gaming32.lifesteal.event.PlayerEvents;
import io.github.gaming32.lifesteal.mixin.PlayerDataStorageAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class MixinPlayerList {
    @Shadow @Final private PlayerDataStorage playerIo;

    @Inject(
        method = "load",
        at = @At(
            value = "INVOKE",
            target = "Lorg/slf4j/Logger;debug(Ljava/lang/String;)V",
            shift = At.Shift.AFTER,
            remap = false
        )
    )
    private void loadFromFileEvent(ServerPlayer serverPlayer, CallbackInfoReturnable<CompoundTag> cir) {
        PlayerEvents.LOAD_FROM_FILE.invoker().handle(new PlayerEvents.SaveContext(
            serverPlayer, ((PlayerDataStorageAccessor)playerIo).getPlayerDir(), serverPlayer.getUUID()
        ));
    }
}
