package com.iafenvoy.bgm.player.forge;

import net.minecraftforge.fml.common.Mod;

import com.iafenvoy.bgm.player.BGMPlayer;

@Mod(BGMPlayer.MOD_ID)
public final class BGMPlayerForge {
    public BGMPlayerForge() {
        // Run our common setup.
        BGMPlayer.init();
    }
}
