package io.github.gaming32.lifesteal.forge;

import dev.architectury.platform.forge.EventBuses;
import io.github.gaming32.lifesteal.Lifesteal;
import io.github.gaming32.lifesteal.event.PlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.UUID;

@Mod(Lifesteal.MOD_ID)
public class LifestealForge {
    public LifestealForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(Lifesteal.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
        Lifesteal.init();
    }

    @SubscribeEvent
    public void onLoadFromFile(PlayerEvent.LoadFromFile event) {
        PlayerEvents.LOAD_FROM_FILE.invoker().handle(new PlayerEvents.SaveContext(
            (ServerPlayer)event.getEntity(), event.getPlayerDirectory(), UUID.fromString(event.getPlayerUUID())
        ));
    }

    @SubscribeEvent
    public void onSaveToFile(PlayerEvent.SaveToFile event) {
        PlayerEvents.SAVE_TO_FILE.invoker().handle(new PlayerEvents.SaveContext(
            (ServerPlayer)event.getEntity(), event.getPlayerDirectory(), UUID.fromString(event.getPlayerUUID())
        ));
    }
}