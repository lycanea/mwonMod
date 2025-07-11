package dev.lycanea.mwonmod.util.region;

import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.GameState;
import dev.lycanea.mwonmod.util.region.RegionLoader;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.concurrent.atomic.AtomicReference;
import java.util.Objects;

import static dev.lycanea.mwonmod.util.region.RegionLoader.beta_plot_origin;
import static dev.lycanea.mwonmod.util.region.RegionLoader.plot_origin;

public class RegionUpdater {

  public static final void init() {

    AtomicReference<String> previousRegion = new AtomicReference<>();

    ClientTickEvents.END_CLIENT_TICK.register(client -> {
        if (Mwonmod.onMelonKing()) {
            if (RegionLoader.getCurrentRegion() != null) {
                String regionName = RegionLoader.getCurrentRegion().name;
                if (!Objects.equals(regionName, previousRegion.get())) {
                    previousRegion.set(regionName);
                    if (Objects.equals(regionName, "housing")) {
                        assert MinecraftClient.getInstance().player != null;
                        GameState.housing_pos = MinecraftClient.getInstance().player.getPos();
                    }
                }
                GameState.playerLocation = regionName;
            } else {
                GameState.playerLocation = null;
            }
        }

        if (Mwonmod.activeRegion != null && client.player != null) {
            assert MinecraftClient.getInstance().player != null;
            BlockPos pos = MinecraftClient.getInstance().player.getBlockPos().add(-plot_origin.x, 0, -plot_origin.y);
            if (GameState.beta_plot) {
                pos = MinecraftClient.getInstance().player.getBlockPos().add(-beta_plot_origin.x, 0, -beta_plot_origin.y);
            }
            Vec3i playerPos = new Vec3i(
                pos.getX(),
                pos.getY(),
                pos.getZ()
            );
            Mwonmod.expandActiveRegionTo(playerPos);
        }
    });

  }

}
