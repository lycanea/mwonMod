package dev.lycanea.mwonmod.music;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class CustomMusicSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
    private boolean done = false;

    public CustomMusicSoundInstance(SoundEvent soundEvent) {
        super(soundEvent, SoundSource.MUSIC, RandomSource.create());
        this.volume = 1.0f;
        this.pitch = 1.0f;
        this.looping = true; // this the loopy part
        this.relative = true; // non-positional
    }

    @Override
    public void tick() {
        // maybe add like music fading or something here?
    }

    @Override
    public boolean isStopped() {
        return done;
    }

    public void stop() {
        done = true;
    }
}
