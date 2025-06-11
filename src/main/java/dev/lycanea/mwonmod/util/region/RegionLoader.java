package dev.lycanea.mwonmod.util.region;

import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.GameState;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

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
    public static final Vector2i beta_plot_origin = new Vector2i(104000,14000);
    public static List<Region> locationData;

    public static void init() {
        InputStream inputStream = Mwonmod.class.getClassLoader().getResourceAsStream("assets/mwonmod/data/locations.json");
        locationData = RegionLoader.loadRegionsFromJson(inputStream);
    }

    public static Region getCurrentRegion() {
        if (MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().world == null || locationData == null || !Mwonmod.onMelonKing()) return null;

        BlockPos pos = MinecraftClient.getInstance().player.getBlockPos().add(-plot_origin.x, 0, -plot_origin.y);
        if (GameState.beta_plot) {
            pos = MinecraftClient.getInstance().player.getBlockPos().add(-beta_plot_origin.x, 0, -beta_plot_origin.y);
        }

        for (Region region : locationData) {
            if (pos.getX() >= Math.min(region.min.getX(), region.max.getX()) &&
                    pos.getX() <= Math.max(region.min.getX(), region.max.getX()) &&
                    pos.getY() >= Math.min(region.min.getY(), region.max.getY()) &&
                    pos.getY() <= Math.max(region.min.getY(), region.max.getY()) &&
                    pos.getZ() >= Math.min(region.min.getZ(), region.max.getZ()) &&
                    pos.getZ() <= Math.max(region.min.getZ(), region.max.getZ())) {
                return region;
            }
        }

        return null;
    }

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
                        min.get("x").getAsInt(),
                        min.get("y").getAsInt(),
                        min.get("z").getAsInt()
                );

                Vec3i maxVec = new Vec3i(
                        max.get("x").getAsInt(),
                        max.get("y").getAsInt(),
                        max.get("z").getAsInt()
                );

                regions.add(new Region(name, minVec, maxVec));
            }
        }

        return regions;
    }
}
