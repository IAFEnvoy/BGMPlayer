package com.iafenvoy.bgm.player.mixin;

import com.iafenvoy.bgm.player.music.MusicManager;
import net.minecraft.client.sound.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @Inject(method = "init", at = @At("RETURN"))
    private void openPlayer(String deviceSpecifier, boolean directionalAudio, CallbackInfo ci) {
        MusicManager.createPlayer();
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void closePlayer(CallbackInfo ci) {
        MusicManager.destroyPlayer();
    }
}
