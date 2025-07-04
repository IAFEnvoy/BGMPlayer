package com.iafenvoy.bgm.player.fabric;

import net.fabricmc.api.ModInitializer;

import com.iafenvoy.bgm.player.BGMPlayer;

public final class BGMPlayerFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        BGMPlayer.init();
    }
}
