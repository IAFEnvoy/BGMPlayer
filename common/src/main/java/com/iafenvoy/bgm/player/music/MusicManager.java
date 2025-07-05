package com.iafenvoy.bgm.player.music;

import com.google.gson.JsonParser;
import com.iafenvoy.bgm.player.BGMPlayer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public final class MusicManager {
    static final String FOLDER = "./config/bgm-player/";
    @Nullable
    private static AudioPlayer PLAYER;
    private static final List<MusicData> DATA = new LinkedList<>();

    private static void load() {
        DATA.clear();
        Map<String, MusicData> byId = new LinkedHashMap<>();
        File musicFolder = new File(FOLDER + "music");
        File[] musics = musicFolder.listFiles();
        if (musics != null)
            try {
                for (File file : musics) {
                    String path = file.getName();
                    if (file.isFile() && path.endsWith(".json"))
                        byId.put(path.substring(0, path.length() - 5), MusicData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(new FileReader(file))).resultOrPartial(BGMPlayer.LOGGER::error).orElseThrow());
                }
            } catch (Exception e) {
                BGMPlayer.LOGGER.error("Failed to load music.", e);
            }
        try {
            for (String id : Codec.STRING.listOf().parse(JsonOps.INSTANCE, JsonParser.parseReader(new FileReader(FOLDER + "playlist.json"))).resultOrPartial(BGMPlayer.LOGGER::error).orElseThrow())
                if (byId.containsKey(id)) DATA.add(byId.remove(id));
                else BGMPlayer.LOGGER.warn("Unknown id in playlist.json {}.", id);
        } catch (Exception e) {
            BGMPlayer.LOGGER.error("Failed to load playlist.", e);
        }
        DATA.addAll(byId.values());
        BGMPlayer.LOGGER.info("Successfully loaded {} bgm data.", DATA.size());
    }

    public static List<MusicData> getData() {
        return List.copyOf(DATA);
    }

    public static void createPlayer() {
        load();
        PLAYER = new AudioPlayer(getData());
        PLAYER.play();
    }

    public static void destroyPlayer() {
        if (PLAYER != null) PLAYER.destroyOpenAL();
        PLAYER = null;
    }
}
