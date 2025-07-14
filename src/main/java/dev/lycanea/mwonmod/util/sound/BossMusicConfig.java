package dev.lycanea.mwonmod.util.sound;

import net.minecraft.sound.SoundEvent;

public record BossMusicConfig(SoundEvent intro, SoundEvent loop, long introDurationMs, String region) {}