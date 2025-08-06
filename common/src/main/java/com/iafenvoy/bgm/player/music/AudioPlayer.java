package com.iafenvoy.bgm.player.music;

import com.iafenvoy.bgm.player.BGMPlayer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import fr.delthas.javamp3.Sound;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class AudioPlayer {
    private static final Supplier<MinecraftClient> CLIENT = MinecraftClient::getInstance;
    private static final Random RANDOM = new Random();
    private final List<MusicData> playlist;
    private final Object2IntMap<MusicData> bufferIds = new Object2IntLinkedOpenHashMap<>();
    private final int source;
    private int currentIndex = -1;
    private boolean isPaused = false;
    private boolean isStopped = true;
    private PlayMode mode = PlayMode.RANDOM;

    public AudioPlayer(List<MusicData> playlist) {
        RenderSystem.assertOnRenderThread();
        this.playlist = playlist;
        //Init OpenAL
        BGMPlayer.LOGGER.info("Creating BGM Player's OpenAL source.");
        this.source = AL10.alGenSources();
        AL10.alSourcef(this.source, AL10.AL_MIN_GAIN, 0.0f);
        AL10.alSourcef(this.source, AL10.AL_MAX_GAIN, 1.0f);
        //Preload all sound resources.
        for (MusicData data : this.playlist)
            try {
                String filepath = data.absoluteSongPath();
                BGMPlayer.LOGGER.info("Loading {}", filepath);
                int buffer;
                if (filepath.endsWith(".wav")) buffer = this.loadWav(filepath);
                else if (filepath.endsWith(".ogg")) buffer = this.loadOgg(filepath);
                else if (filepath.endsWith(".mp3")) buffer = this.loadMp3(filepath);
                else throw new UnsupportedOperationException("Unsupported file type");
                this.bufferIds.put(data, buffer);
            } catch (Exception e) {
                BGMPlayer.LOGGER.error("Failed to load {}", data.name(), e);
            }
        BGMPlayer.LOGGER.info("Finish loading all music.");
    }

    public void destroyOpenAL() {
        RenderSystem.assertOnRenderThread();
        BGMPlayer.LOGGER.info("Shutdown BGM Player's OpenAL source.");
        this.stop();
        AL10.alDeleteSources(this.source);
    }

    public void play() {
        if (!this.playlist.isEmpty()) this.play(0);
    }

    public void play(MusicData data) {
        int i = this.playlist.indexOf(data);
        if (i != -1) this.play(i);
    }

    public void play(int index) {
        RenderSystem.assertOnRenderThread();
        this.stop();
        this.currentIndex = index;
        this.isStopped = false;
        MusicData data = this.playlist.get(index);
        BGMPlayer.LOGGER.info("Start playing: {}", data.name());
        try {
            if (!this.bufferIds.containsKey(data)) {
                BGMPlayer.LOGGER.error("Failed to play {}: file {} not found.", data.name(), data.absoluteSongPath());
                return;
            }
            int buffer = this.bufferIds.getInt(data);
            AL10.alSourcei(this.source, AL10.AL_BUFFER, buffer);
            AL10.alSourcePlay(this.source);
            Thread thread = new Thread(this::monitorTick);
            thread.setName("BGM Player Monitor");
            thread.start();
        } catch (Exception e) {
            BGMPlayer.LOGGER.error("Failed to play {}", data.name(), e);
            this.isStopped = true;
        }
    }

    public void monitorTick() {
        while (true) {
            try {
                Thread.sleep(100);
                CLIENT.get().execute(() -> this.setVolume(CLIENT.get().options.getSoundVolume(SoundCategory.MUSIC)));
                if (this.isStopped) break;
                if (this.getState() == PlayState.STOPPED) {
                    CLIENT.get().execute(() -> this.play(switch (this.mode) {
                        case PLAYLIST -> (this.currentIndex + 1) % this.playlist.size();
                        case SINGLE_LOOPING -> this.currentIndex;
                        case RANDOM -> RANDOM.nextInt(this.playlist.size());
                    }));
                    break;
                }
            } catch (Exception e) {
                BGMPlayer.LOGGER.error("Failed to monitor play state.", e);
                break;
            }
        }
    }

    private int loadWav(String filepath) throws Exception {
        File file = new File(filepath);
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        AudioFormat format = ais.getFormat();
        int channels = format.getChannels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
        int sampleRate = (int) format.getSampleRate();
        byte[] bufferArray = ais.readAllBytes();
        int buffer = AL10.alGenBuffers();
        AL10.alBufferData(buffer, channels, MemoryUtil.memAlloc(bufferArray.length).put(bufferArray).flip(), sampleRate);
        return buffer;
    }

    private int loadOgg(String filepath) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer error = stack.mallocInt(1);
            long decoder = STBVorbis.stb_vorbis_open_filename(filepath, error, null);
            if (decoder == MemoryUtil.NULL) throw new RuntimeException("Failed to open OGG file");

            STBVorbisInfo info = STBVorbisInfo.malloc(stack);
            STBVorbis.stb_vorbis_get_info(decoder, info);

            int channels = info.channels();
            int sampleRate = info.sample_rate();
            int samples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);
            ShortBuffer pcm = MemoryUtil.memAllocShort(samples * channels);
            STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);

            int format = channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
            int buffer = AL10.alGenBuffers();
            AL10.alBufferData(buffer, format, pcm, sampleRate);
            STBVorbis.stb_vorbis_close(decoder);
            return buffer;
        }
    }

    private int loadMp3(String filepath) throws IOException {
        try (Sound sound = new Sound(new BufferedInputStream(Files.newInputStream(Path.of(filepath))))) {
            int channels = sound.isStereo() ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16;
            int sampleRate = sound.getSamplingFrequency();
            byte[] bufferArray = sound.readAllBytes();
            int buffer = AL10.alGenBuffers();
            AL10.alBufferData(buffer, channels, MemoryUtil.memAlloc(bufferArray.length).put(bufferArray).flip(), sampleRate);
            return buffer;
        }
    }

    public PlayState getState() {
        return switch (AL10.alGetSourcei(this.source, AL10.AL_SOURCE_STATE)) {
            case AL10.AL_PLAYING -> PlayState.PLAYING;
            case AL10.AL_PAUSED -> PlayState.PAUSED;
            default -> PlayState.STOPPED;
        };
    }

    public void next() {
        this.play((this.currentIndex + 1) % this.playlist.size());
    }

    public void previous() {
        this.play((this.currentIndex - 1) % this.playlist.size());
    }

    public void pause() {
        RenderSystem.assertOnRenderThread();
        if (!this.isPaused) {
            this.isPaused = true;
            AL10.alSourcePause(this.source);
        }
    }

    public void resume() {
        RenderSystem.assertOnRenderThread();
        if (this.isPaused) {
            this.isPaused = false;
            AL10.alSourcePlay(this.source);
        }
    }

    public void stop() {
        RenderSystem.assertOnRenderThread();
        this.isStopped = true;
        this.isPaused = false;
        AL10.alSourceStop(this.source);
    }

    @Nullable
    public MusicData getCurrentSong() {
        return this.getState() == PlayState.PLAYING && this.currentIndex >= 0 && this.currentIndex < this.playlist.size() ? this.playlist.get(this.currentIndex) : null;
    }

    public void setVolume(float volume) {
        RenderSystem.assertOnRenderThread();
        AL10.alSourcef(this.source, AL10.AL_GAIN, volume);
    }

    public void setPlayMode(PlayMode mode) {
        this.mode = mode;
    }

    public enum PlayMode implements StringIdentifiable {
        PLAYLIST("playlist"),
        SINGLE_LOOPING("single_looping"),
        RANDOM("random");

        public static final Codec<PlayMode> CODEC = StringIdentifiable.createCodec(PlayMode::values);
        private final String name;

        PlayMode(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }

    public enum PlayState {
        PLAYING, PAUSED, STOPPED
    }
}
