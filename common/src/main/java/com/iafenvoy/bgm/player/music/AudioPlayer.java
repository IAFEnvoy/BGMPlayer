package com.iafenvoy.bgm.player.music;

import com.iafenvoy.bgm.player.BGMPlayer;
import javazoom.jl.player.Player;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.*;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.List;

public class AudioPlayer {
    private final long device;
    private final long context;
    private final int source;
    private final List<MusicData> playlist;
    private int currentIndex = -1;
    private boolean isPaused = false;
    private boolean looping = false;

    public AudioPlayer(List<MusicData> playlist) {
        this.playlist = playlist;
        //Init OpenAL
        BGMPlayer.LOGGER.info("Creating BGM Player's OpenAL context.");
        this.device = ALC10.alcOpenDevice((ByteBuffer) null);
        ALCCapabilities deviceCaps = ALC.createCapabilities(this.device);
        this.context = ALC10.alcCreateContext(this.device, (IntBuffer) null);
        ALC10.alcMakeContextCurrent(this.context);
        AL.createCapabilities(deviceCaps);
        this.source = AL10.alGenSources();
    }

    public void destroyOpenAL() {
        BGMPlayer.LOGGER.info("Game closing, shutdown BGM Player's OpenAL context.");
        AL10.alDeleteSources(this.source);
        ALC10.alcDestroyContext(this.context);
        ALC10.alcCloseDevice(this.device);
    }

    public void play() throws Exception {
        if (!this.playlist.isEmpty()) {
            this.play(0);
        }
    }

    public void play(int index) throws Exception {
        this.stop();
        this.currentIndex = index;
        String filepath = this.playlist.get(index).absoluteSongPath();

        if (filepath.endsWith(".wav")) {
            this.playWAV(filepath);
        } else if (filepath.endsWith(".ogg")) {
            this.playOGG(filepath);
        } else if (filepath.endsWith(".mp3")) {
            this.playMP3(filepath);
        } else {
            throw new UnsupportedOperationException("Unsupported file type");
        }
    }

    private void playWAV(String filepath) throws Exception {
        File file = new File(filepath);
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        AudioFormat format = ais.getFormat();
        int channels = format.getChannels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
        int sampleRate = (int) format.getSampleRate();
        byte[] bufferArray = ais.readAllBytes();

        int buffer = AL10.alGenBuffers();
        AL10.alBufferData(buffer, channels, MemoryUtil.memAlloc(bufferArray.length).put(bufferArray).flip(), sampleRate);
        AL10.alSourcei(this.source, AL10.AL_BUFFER, buffer);
        AL10.alSourcei(this.source, AL10.AL_LOOPING, this.looping ? AL10.AL_TRUE : AL10.AL_FALSE);
        AL10.alSourcePlay(this.source);
    }

    private void playOGG(String filepath) throws Exception {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer error = stack.mallocInt(1);
            long decoder = STBVorbis.stb_vorbis_open_filename(filepath, error, null);
            if (decoder == MemoryUtil.NULL) {
                throw new RuntimeException("Failed to open OGG file");
            }

            STBVorbisInfo info = STBVorbisInfo.mallocStack(stack);
            STBVorbis.stb_vorbis_get_info(decoder, info);

            int channels = info.channels();
            int sampleRate = info.sample_rate();

            int samples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);
            ShortBuffer pcm = MemoryUtil.memAllocShort(samples * channels);
            STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);

            int format = channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;

            int buffer = AL10.alGenBuffers();
            AL10.alBufferData(buffer, format, pcm, sampleRate);

            AL10.alSourcei(this.source, AL10.AL_BUFFER, buffer);
            AL10.alSourcei(this.source, AL10.AL_LOOPING, this.looping ? AL10.AL_TRUE : AL10.AL_FALSE);
            AL10.alSourcePlay(this.source);

            STBVorbis.stb_vorbis_close(decoder);
        }
    }

    private void playMP3(String filepath) throws Exception {
        new Thread(() -> {
            try {
                Player mp3Player = new Player(new FileInputStream(filepath));
                mp3Player.play();
                if (this.looping) {
                    this.play(this.currentIndex);
                } else {
                    this.onSongFinished();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void next() throws Exception {
        if (this.currentIndex + 1 < this.playlist.size()) {
            this.play(this.currentIndex + 1);
        }
    }

    public void previous() throws Exception {
        if (this.currentIndex - 1 >= 0) {
            this.play(this.currentIndex - 1);
        }
    }

    public void pause() {
        if (!this.isPaused) {
            AL10.alSourcePause(this.source);
            this.isPaused = true;
        }
    }

    public void resume() {
        if (this.isPaused) {
            AL10.alSourcePlay(this.source);
            this.isPaused = false;
        }
    }

    public void stop() {
        AL10.alSourceStop(this.source);
        this.isPaused = false;
    }

    @Nullable
    public MusicData getCurrentSong() {
        if (this.currentIndex >= 0 && this.currentIndex < this.playlist.size()) {
            return this.playlist.get(this.currentIndex);
        }
        return null;
    }

    public void setVolume(float volume) {
        AL10.alSourcef(this.source, AL10.AL_GAIN, volume);
    }

    public void setLooping(boolean loop) {
        this.looping = loop;
    }

    private void onSongFinished() throws Exception {
        if (!this.looping && this.currentIndex + 1 < this.playlist.size()) {
            this.play(this.currentIndex + 1);
        }
    }
}
