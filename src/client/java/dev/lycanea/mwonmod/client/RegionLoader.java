package dev.lycanea.mwonmod.client;

import com.google.gson.*;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector2i;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RegionLoader {
    public static final Vector2i plot_origin = new Vector2i(-975,-4270);

    public static List<Region> loadRegionsFromJson(InputStream jsonStream) {
        List<Region> regions = new ArrayList<>();

        JsonElement root = JsonParser.parseReader(new InputStreamReader(jsonStream, StandardCharsets.UTF_8));
        if (root.isJsonArray()) {
            JsonArray array = root.getAsJsonArray();

            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();

                String name = obj.get("name").getAsString();

                JsonObject min = obj.getAsJsonObject("min");
                JsonObject max = obj.getAsJsonObject("max");

                Vec3i minVec = new Vec3i(
                        min.get("x").getAsInt() + plot_origin.x,
                        min.get("y").getAsInt(),
                        min.get("z").getAsInt() + plot_origin.y
                );

                Vec3i maxVec = new Vec3i(
                        max.get("x").getAsInt() + plot_origin.x,
                        max.get("y").getAsInt(),
                        max.get("z").getAsInt() + plot_origin.y
                );

                regions.add(new Region(name, minVec, maxVec));
            }
        }

        return regions;
    }
}
