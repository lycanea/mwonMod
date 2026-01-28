package dev.lycanea.mwonmod.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import java.util.HashMap;
import java.util.Map;

import static dev.lycanea.mwonmod.Mwonmod.loadJsonFile;

public class BossState {
    public static Boss boss;
    public static Map<String, String> dialogueToBoss = new HashMap<>();

    private static final String BOSS_DATA_PATH = "assets/mwonmod/data/bosses.json";

    public static void init() {
        JsonObject boss_dataJson = loadJsonFile(BOSS_DATA_PATH);
        if (boss_dataJson != null) {
            for (String boss_id : boss_dataJson.keySet()) {
                // each boss
                for (JsonElement jsonElement : boss_dataJson.get(boss_id).getAsJsonObject().get("dialogue").getAsJsonArray()) {
                    // each dialogue
                    String dialogue = jsonElement.getAsString();
                    dialogueToBoss.put(dialogue, boss_id);
                }
            }
        }
    }

    public static void updateBoss(String updatedBossID) {
        Boss updatedBoss = new Boss(updatedBossID);
        InteractionResult result = BossChangeCallback.EVENT.invoker().trigger(boss, updatedBoss);
        boss = updatedBoss;
    }

    public interface BossChangeCallback {
        Event<BossChangeCallback> EVENT = EventFactory.createArrayBacked(BossChangeCallback.class,
                (listeners) -> (Boss previousBoss, Boss currentBoss) -> {
            for (BossChangeCallback event : listeners) {
                InteractionResult result = event.trigger(previousBoss, currentBoss);

                if (result != InteractionResult.PASS) { // all this actionresult stuff is useless but idk im copying from the fabric docs
                    return result;
                }
            }

            return InteractionResult.PASS;
        });
        InteractionResult trigger(Boss previousBoss, Boss currentBoss);
    }
}
