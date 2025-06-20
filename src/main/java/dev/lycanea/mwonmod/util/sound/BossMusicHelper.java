package dev.lycanea.mwonmod.util.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BossMusicHelper {
    private static SoundInstance introInstance = null;
    private static SoundInstance loopInstance = null;
    private static Timer loopTimer = null;
    public static Identifier currentBoss = null;
    public static Map<Identifier, BossMusicConfig> musicMap = new HashMap<>();

    public static void init() {
        musicMap.put(Identifier.of("mwonmod", "carrot"), new BossMusicConfig(SoundRegister.CARROT_BOSS_INTRO, SoundRegister.CARROT_BOSS_LOOP, 5842, "carrot_kingdom_fight"));
    }

    public static void playBoss(Identifier bossId, MinecraftClient client) {
        if (bossId.equals(currentBoss)) return;
        stop(client);

        currentBoss = bossId;
        BossMusicConfig cfg = musicMap.get(bossId);

        introInstance = PositionedSoundInstance.master(cfg.intro(), 1.0f, 1.0f);
        client.getSoundManager().play(introInstance);

        loopTimer = new Timer();
        loopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                client.execute(() -> {
                    loopInstance = new LoopingMusicInstance(bossId, cfg.loop());
                    client.getSoundManager().play(loopInstance);
                });
            }
        }, cfg.introDurationMs());
    }

    public static void stop(MinecraftClient client) {
        if (introInstance != null) {
            client.getSoundManager().stop(introInstance);
            introInstance = null;
        }
        if (loopInstance != null) {
            client.getSoundManager().stop(loopInstance);
            loopInstance = null;
        }
        if (loopTimer != null) {
            loopTimer.cancel();
            loopTimer = null;
        }
        currentBoss = null;
    }
}
