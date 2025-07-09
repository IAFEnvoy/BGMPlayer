package com.iafenvoy.bgm.player.music;

import com.iafenvoy.bgm.player.BGMPlayer;
import com.iafenvoy.bgm.player.util.ImageUtil;
import com.iafenvoy.bgm.player.util.SimpleTexture;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

import java.io.*;
import java.util.Optional;

public final class MusicData {
    public static final String FOLDER = MusicManager.FOLDER + "resource/";
    public static final Codec<MusicData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("name").forGetter(MusicData::name),
            Codec.STRING.fieldOf("path").forGetter(MusicData::path),
            Codec.STRING.optionalFieldOf("author").forGetter(MusicData::author),
            Codec.STRING.optionalFieldOf("album").forGetter(MusicData::album),
            Codec.STRING.optionalFieldOf("icon").forGetter(MusicData::icon)
    ).apply(i, MusicData::new));
    private static int TEXTURE_ID = 0;
    private final String name;
    private final String path;
    private final Optional<String> author, album;
    private final Optional<String> icon;
    private final Identifier iconId;

    public MusicData(String name, String path, Optional<String> author, Optional<String> album, Optional<String> icon) {
        this.name = name;
        this.path = path;
        this.author = author;
        this.album = album;
        this.icon = icon;
        this.iconId = Identifier.of(BGMPlayer.MOD_ID, "icon_" + TEXTURE_ID++);
        if (this.icon.isPresent())
            try {
                String p = FOLDER + this.icon.get();
                InputStream inputStream = new FileInputStream(p);
                if (!p.endsWith(".png")) inputStream = ImageUtil.convertToPng(inputStream);
                SimpleTexture texture = new SimpleTexture(NativeImage.read(inputStream));
                texture.upload(false, false);
                MinecraftClient.getInstance().getTextureManager().registerTexture(this.iconId, texture);
            } catch (IOException e) {
                BGMPlayer.LOGGER.error("Failed to load icon {}.", this.icon.get(), e);
            }
    }

    public String absoluteSongPath() {
        return FOLDER + this.path;
    }

    public String name() {
        return this.name;
    }

    public String path() {
        return this.path;
    }

    public Optional<String> author() {
        return this.author;
    }

    public Optional<String> album() {
        return this.album;
    }

    public Optional<String> icon() {
        return this.icon;
    }

    public Identifier getIconId() {
        return this.iconId;
    }
}
