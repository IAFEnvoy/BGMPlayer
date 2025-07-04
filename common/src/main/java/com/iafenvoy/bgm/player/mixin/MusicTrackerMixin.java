package com.iafenvoy.bgm.player.mixin;

import com.iafenvoy.bgm.player.music.MusicManager;
import net.minecraft.client.sound.MusicTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicTracker.class)
public class MusicTrackerMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void cancelVanillaBgm(CallbackInfo ci) {
        ci.cancel();
        MusicManager.startPlaying();
    }
}
