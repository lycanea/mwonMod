package dev.lycanea.mwonmod.util.region;

import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.music.CustomMusicManager;
import dev.lycanea.mwonmod.music.CustomSong;
import dev.lycanea.mwonmod.util.GameState;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Objects;

import static dev.lycanea.mwonmod.util.region.RegionLoader.beta_plot_origin;
import static dev.lycanea.mwonmod.util.region.RegionLoader.plot_origin;

public class RegionUpdater {

  public static void init() {

    AtomicReference<String> previousRegion = new AtomicReference<>();

    ClientTickEvents.END_CLIENT_TICK.register(client -> {
        if (Mwonmod.onMelonKing()) {
            if (RegionLoader.getCurrentRegion() != null) {
                String regionName = RegionLoader.getCurrentRegion().name;
                CustomMusicManager.setCurrentSong(CustomSong.NONE);
                if (!Objects.equals(regionName, previousRegion.get())) {
                    previousRegion.set(regionName);
                    if (Objects.equals(regionName, "housing")) {
                        assert Minecraft.getInstance().player != null;
                        GameState.housing_pos = Minecraft.getInstance().player.position().align(EnumSet.of(Direction.Axis.X, Direction.Axis.Y, Direction.Axis.Z));
                    }
                }
                GameState.playerLocation = regionName;
            } else {
                GameState.playerLocation = null;
            }
        }

        if (Mwonmod.activeRegion != null && client.player != null) {
            assert Minecraft.getInstance().player != null;
            BlockPos pos = Minecraft.getInstance().player.blockPosition().offset(-plot_origin.x, 0, -plot_origin.y);
            if (GameState.beta_plot) {
                pos = Minecraft.getInstance().player.blockPosition().offset(-beta_plot_origin.x, 0, -beta_plot_origin.y);
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
