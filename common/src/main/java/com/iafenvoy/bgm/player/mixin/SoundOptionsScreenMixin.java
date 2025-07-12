package com.iafenvoy.bgm.player.mixin;

import com.iafenvoy.bgm.player.screen.MusicListScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.SoundOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin extends GameOptionsScreen {
    public SoundOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }

    @Inject(method = "addOptions", at = @At("RETURN"))
    private void addBgmPlayerButton(CallbackInfo ci) {
        assert this.body != null;
        this.body.addSingleOptionEntry(SimpleOption.ofBoolean("screen.bgm_player.title", SimpleOption.emptyTooltip(), (t, v) -> Text.empty(), false, v -> MinecraftClient.getInstance().setScreen(new MusicListScreen(this))));
    }
}
