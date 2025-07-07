package com.iafenvoy.bgm.player.music;

import com.google.gson.JsonParser;
import com.iafenvoy.bgm.player.BGMPlayer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class MusicConfig {
    public static final Codec<MusicConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            AudioPlayer.PlayMode.CODEC.fieldOf("mode").forGetter(MusicConfig::getMode),
            Codec.STRING.listOf().fieldOf("playlist").forGetter(MusicConfig::getPlaylist)
    ).apply(i, MusicConfig::new));
    private static final String PATH = MusicManager.FOLDER + "config.json";
    private static MusicConfig INSTANCE = new MusicConfig();
    private AudioPlayer.PlayMode mode = AudioPlayer.PlayMode.RANDOM;
    //Ordinal in manager
    private List<String> playlist = new LinkedList<>();

    public MusicConfig() {
    }

    public MusicConfig(AudioPlayer.PlayMode mode, List<String> playlist) {
        this.mode = mode;
        this.playlist = playlist;
    }

    public AudioPlayer.PlayMode getMode() {
        return this.mode;
    }

    public List<String> getPlaylist() {
        return this.playlist;
    }

    public static void load() {
        try {
            INSTANCE = CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(new FileReader(PATH))).resultOrPartial(BGMPlayer.LOGGER::error).orElseThrow();
        } catch (Exception e) {
            BGMPlayer.LOGGER.error("Failed to load config {}.", PATH, e);
            save();
        }
    }

    public static void save() {
        try {
            FileUtils.write(new File(PATH), CODEC.encodeStart(JsonOps.INSTANCE, INSTANCE).resultOrPartial(BGMPlayer.LOGGER::error).orElseThrow().toString(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            BGMPlayer.LOGGER.error("Failed to save config {}.", PATH, e);
        }
    }

    public static MusicConfig getInstance() {
        return INSTANCE;
    }
}
