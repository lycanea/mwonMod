package dev.lycanea.mwonmod.util.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundRegister {
    public static SoundEvent CARROT_BOSS_INTRO, CARROT_BOSS_LOOP;

    public static void initialize() {
        CARROT_BOSS_INTRO = register("carrot_boss_intro");
        CARROT_BOSS_LOOP = register("carrot_boss_loop");
        BossMusicHelper.init();
    }

    private static SoundEvent register(String name) {
        SoundEvent evt = SoundEvent.of(Identifier.of("mwonmod", name));
        Registry.register(Registries.SOUND_EVENT, evt.id(), evt);
        return evt;
    }
}
