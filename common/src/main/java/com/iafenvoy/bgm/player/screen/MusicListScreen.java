package com.iafenvoy.bgm.player.screen;

import com.iafenvoy.bgm.player.BGMPlayer;
import com.iafenvoy.bgm.player.music.AudioPlayer;
import com.iafenvoy.bgm.player.music.MusicData;
import com.iafenvoy.bgm.player.music.MusicManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MusicListScreen extends Screen {
    private final Screen parent;
    private MusicListWidget widget;
    private boolean initialized = false;

    public MusicListScreen(@Nullable Screen parent) {
        super(Text.translatable("screen.%s.bgm_screen.title".formatted(BGMPlayer.MOD_ID)));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        if (this.initialized) this.widget.setDimensionsAndPosition(this.width - 120, this.height - 50, 100, 25);
        else {
            this.initialized = true;
            this.widget = new MusicListWidget(this, this.client, this.width - 120, this.height - 50, 100, 25, 36);
            this.widget.setData(MusicManager.getData());
        }
        this.addDrawableChild(this.widget);
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.bgm_player.close"), button -> this.close()).dimensions(10, 25, 80, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.bgm_player.previous"), button -> {
            AudioPlayer player = MusicManager.getPlayer();
            if (player != null) player.previous();
        }).dimensions(10, 80, 80, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable(this.isPlaying() ? "screen.bgm_player.pause" : "screen.bgm_player.resume"), button -> {
            AudioPlayer player = MusicManager.getPlayer();
            if (player == null) return;
            if (this.isPlaying()) {
                player.pause();
                button.setMessage(Text.translatable("screen.bgm_player.resume"));
            } else {
                player.resume();
                button.setMessage(Text.translatable("screen.bgm_player.pause"));
            }
        }).dimensions(10, 100, 80, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.bgm_player.next"), button -> {
            AudioPlayer player = MusicManager.getPlayer();
            if (player != null) player.next();
        }).dimensions(10, 120, 80, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.bgm_player.play_selected"), button -> {
            AudioPlayer player = MusicManager.getPlayer();
            MusicListWidget.Entry data = this.widget.getSelectedOrNull();
            if (player == null || data == null) return;
            player.play(data.getData());
        }).dimensions(10, 140, 80, 20).build());
        assert this.client != null;
        GameOptions options = this.client.options;
        this.addDrawableChild(options.getSoundVolumeOption(SoundCategory.MUSIC).createWidget(options, 10, 160, 80));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.bgm_player.title"), this.width / 2, 10, -1);
        this.getCurrentPlaying().ifPresent(data -> context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.bgm_player.now_playing", Text.literal(data.name())), this.width / 2, this.height - 18, -1));
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.parent);
    }

    public void select(MusicListWidget.Entry entry) {
        this.widget.setSelected(entry);
    }

    public Optional<MusicData> getCurrentPlaying() {
        return Optional.ofNullable(MusicManager.getPlayer()).map(AudioPlayer::getCurrentSong);
    }

    public boolean isPlaying() {
        return MusicManager.getPlayer() != null && MusicManager.getPlayer().getState() == AudioPlayer.PlayState.PLAYING;
    }
}
