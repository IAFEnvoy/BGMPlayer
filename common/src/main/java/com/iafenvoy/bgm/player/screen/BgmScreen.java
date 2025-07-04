package com.iafenvoy.bgm.player.screen;

import com.iafenvoy.bgm.player.BGMPlayer;
import com.iafenvoy.bgm.player.music.MusicManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class BgmScreen extends Screen {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private final Screen parent;
    private MusicDataListWidget widget;
    private boolean initialized = false;

    public BgmScreen(@Nullable Screen parent) {
        super(Text.translatable("screen.%s.bgm_screen.title".formatted(BGMPlayer.MOD_ID)));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        if (this.initialized)
            this.widget.updateSize(100, this.width - 80, 32, this.height - 32);
        else {
            this.initialized = true;
            this.widget = new MusicDataListWidget(this, client, 100, this.width - 80, 32, this.height - 32, 36);
            this.widget.setData(MusicManager.getData());
        }
        this.addDrawableChild(this.widget);

//        this.addButton(new ButtonWidget(10, 15, 80, 20, new TranslatableText("as.gui.Close"), button -> this.openParent()));
//        this.addButton(new ButtonWidget(10, 115, 80, 20, new TranslatableText("as.gui.UseAccount"), button -> {
//            if (this.widget.getSelected() != null && this.widget.getSelected() instanceof MusicDataListWidget.MusicDataEntry)
//                ((MusicDataListWidget.MusicDataEntry) this.widget.getSelected()).getData().use(this);
//        }));
//        this.addButton(new ButtonWidget(10, 135, 80, 20, new TranslatableText("as.gui.RefreshAccount"), button -> {
//            if (this.widget.getSelected() != null && this.widget.getSelected() instanceof MusicDataListWidget.MusicDataEntry)
//                ((MusicDataListWidget.MusicDataEntry) this.widget.getSelected()).getData().refresh(this);
//        }));
//        this.addButton(new ButtonWidget(10, 180, 80, 20, new TranslatableText("as.gui.DeleteAccount"), button -> {
//            if (this.widget.getSelected() != null && this.widget.getSelected() instanceof MusicDataListWidget.MusicDataEntry)
//                AccountManager.INSTANCE.deleteAccountByUuid(((MusicDataListWidget.MusicDataEntry) this.widget.getSelected()).getData().getUuid());
//            this.refreshWidget();
//        }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context);
        this.widget.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    public void select(MusicDataListWidget.Entry entry) {
        this.widget.setSelected(entry);
    }
}
