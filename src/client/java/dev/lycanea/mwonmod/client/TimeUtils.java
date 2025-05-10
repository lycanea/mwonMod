package dev.lycanea.mwonmod.client;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TimeUtils {
    public static long millisUntilNextHalfOrHourUTC() {
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
}
