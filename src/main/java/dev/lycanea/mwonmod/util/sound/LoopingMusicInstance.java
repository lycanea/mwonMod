package dev.lycanea.mwonmod.util.sound;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class LoopingMusicInstance extends MovingSoundInstance {

    public LoopingMusicInstance(Identifier bossId, SoundEvent loopSound) {
        super(loopSound, SoundCategory.MASTER, SoundInstance.createRandom());
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 1.0f;
        this.pitch = 1.0f;
        this.relative = true;
    }

    @Override
    public boolean shouldAlwaysPlay() {
        return true;
    }

    @Override
    public void tick() {
        if (isDone()) return;
    }
}
