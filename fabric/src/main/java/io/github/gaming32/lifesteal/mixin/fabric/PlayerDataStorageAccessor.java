package io.github.gaming32.lifesteal.mixin.fabric;

import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(PlayerDataStorage.class)
public interface PlayerDataStorageAccessor {
    @Accessor
    File getPlayerDir();
}
