package dev.lycanea.mwonmod.util;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TimeUtils {
    public static long auctionTime() {
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);

        int minute = nowUtc.getMinute();

        int targetMinute = (minute < 30) ? 30 : 60;

        ZonedDateTime nextBoundary;
        if (targetMinute == 60) {
            nextBoundary = nowUtc
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0)
                    .plusHours(1);
        } else {
            nextBoundary = nowUtc
                    .withMinute(30)
                    .withSecond(0)
                    .withNano(0);
        }

        Duration duration = Duration.between(nowUtc, nextBoundary);
        return duration.toMillis();
    }

    public static long flawlessTime() {
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);

        long currentMinutes = nowUtc.toEpochSecond();
        long nextFlawless = 4620 - currentMinutes % 4620;

        // despawns at 1:14:38 spawn timer time
        // despawns at nextFlawless = 4478
        // if nextFlawless > 4478 then npc is spawned
        
        return nextFlawless;
    }
}
