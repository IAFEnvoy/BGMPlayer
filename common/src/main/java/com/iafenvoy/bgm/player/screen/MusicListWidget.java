package com.iafenvoy.bgm.player.screen;

import com.iafenvoy.bgm.player.music.MusicData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class MusicListWidget extends AlwaysSelectedEntryListWidget<MusicListWidget.Entry> {
    private final MusicListScreen screen;
    private final List<Entry> entries = new ArrayList<>();

    public MusicListWidget(MusicListScreen screen, MinecraftClient client, int width, int height, int x, int y, int entryHeight) {
        super(client, width, height, y, entryHeight);
        this.screen = screen;
        this.setDimensionsAndPosition(width, height, x, y);
    }

    public void setData(List<MusicData> list) {
        this.entries.clear();
        for (MusicData data : list)
            this.entries.add(new Entry(this.screen, this.client.textRenderer, data));
        this.updateEntries();
    }

    @Override
    protected int getScrollbarX() {
        return this.getX() + this.width - 5;
    }

    @Override
    public int getRowWidth() {
        return this.width - 20;
    }

    private void updateEntries() {
        this.clearEntries();
        this.entries.forEach(this::addEntry);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Entry entry = this.getSelectedOrNull();
        return entry != null && entry.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    public static class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
        private final MusicListScreen screen;
        private final TextRenderer textRenderer;
        private final MusicData data;

        public Entry(MusicListScreen screen, TextRenderer textRenderer, MusicData data) {
            this.screen = screen;
            this.textRenderer = textRenderer;
            this.data = data;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(this.textRenderer, this.data.name(), x + 32 + 3, y + 1, 0xFFFFFFFF);
            if (this.data.author().isPresent())
                context.drawTextWithShadow(this.textRenderer, Text.translatable("screen.bgm_player.author", this.data.author().get()), x + 32 + 3, y + 1 + 9, 0xFF777777);
            if (this.data.album().isPresent())
                context.drawTextWithShadow(this.textRenderer, Text.translatable("screen.bgm_player.album", this.data.album().get()), x + 32 + 3, y + 1 + 18, 0xFF777777);
            if (this.data.icon().isPresent())
                context.drawTexture(this.data.getIconId(), x, y, 0, 0, 32, 32, 32, 32);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.screen.select(this);
            return false;
        }

        @Override
        public Text getNarration() {
            return Text.literal(this.data.name());
        }

        public MusicData getData() {
            return this.data;
        }
    }
}
