package io.github.gaming32.lifesteal.fabric;

import io.github.gaming32.lifesteal.Lifesteal;
import net.fabricmc.api.ModInitializer;

public class LifestealFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Lifesteal.init();
    }
}