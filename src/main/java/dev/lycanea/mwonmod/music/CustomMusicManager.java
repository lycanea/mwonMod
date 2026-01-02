package dev.lycanea.mwonmod.music;

import dev.lycanea.mwonmod.util.BossState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class CustomMusicManager {
    private static CustomSong currentSong = CustomSong.NONE;
    private static CustomSong playingSong = CustomSong.NONE;
    private static SoundInstance currentSound = null;

    public static void setCurrentSong(CustomSong song) {
        currentSong = song;
    }

    public static void init() {
        BossState.BossChangeCallback.EVENT.register((previousBoss, currentBoss) -> {

            return null;
        });
    }

    public static void tick(MinecraftClient client) {
//        client.options.getSoundVolumeOption(SoundCategory.MUSIC).setValue(0.0);

        if (currentSong != playingSong) {
            stopCurrentSong(client);

            if (currentSong != CustomSong.NONE) {
                playSong(client, currentSong);
            }

            playingSong = currentSong;
        }
    }

    private static void playSong(MinecraftClient client, CustomSong song) {
        Identifier soundId = song.getSoundId();
        if (soundId == null) return;

        SoundEvent soundEvent = SoundEvent.of(soundId);
        CustomMusicSoundInstance sound = new CustomMusicSoundInstance(soundEvent);
        currentSound = sound;

        client.getSoundManager().play(sound);
    }

    public static void stopCurrentSong(MinecraftClient client) {
        if (currentSound != null) {
            if (currentSound instanceof CustomMusicSoundInstance looping) {
                looping.stop();
            }
            client.getSoundManager().stop(currentSound);
            currentSound = null;
        }
    }
}
