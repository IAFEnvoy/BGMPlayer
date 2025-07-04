package com.iafenvoy.bgm.player.music;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record MusicData(String name, Optional<String> author, Optional<String> album, String path,
                        Optional<String> icon) {
    public static final String FOLDER = MusicManager.FOLDER + "resource/";
    public static final Codec<MusicData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("name").forGetter(MusicData::name),
            Codec.STRING.optionalFieldOf("author").forGetter(MusicData::author),
            Codec.STRING.optionalFieldOf("album").forGetter(MusicData::album),
            Codec.STRING.fieldOf("path").forGetter(MusicData::path),
            Codec.STRING.optionalFieldOf("icon").forGetter(MusicData::icon)
    ).apply(i, MusicData::new));

    public String absoluteSongPath() {
        return FOLDER + this.path;
    }

    public Optional<String> absoluteIconPath() {
        return this.icon.map(x -> FOLDER + x);
    }
}
