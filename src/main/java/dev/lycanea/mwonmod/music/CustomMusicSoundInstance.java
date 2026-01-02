package dev.lycanea.mwonmod.music;

import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;

public class CustomMusicSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
    private boolean done = false;

    public CustomMusicSoundInstance(SoundEvent soundEvent) {
        super(soundEvent, SoundCategory.MUSIC, Random.create());
        this.volume = 1.0f;
        this.pitch = 1.0f;
        this.repeat = true; // this the loopy part
        this.relative = true; // non-positional
    }

    @Override
    public void tick() {
        // maybe add like music fading or something here?
    }

    @Override
    public boolean isDone() {
        return done;
    }

    public void stop() {
        done = true;
    }
}
