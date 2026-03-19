package dev.lycanea.mwonmod;

import dev.lycanea.mwonmod.music.CustomMusicManager;
import dev.lycanea.mwonmod.util.*;
import dev.lycanea.mwonmod.util.region.*;
import dev.lycanea.mwonmod.util.discord.DiscordManager;
import dev.lycanea.mwonmod.events.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;

public class Mwonmod implements ClientModInitializer {
    public static final String MOD_ID = "mwonmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Boolean inventory_rundown = false;
    private static final String ITEM_DATA_PATH = "assets/mwonmod/data/items.json";
    private static final String UPGRADES_PATH = "assets/mwonmod/data/melonmod_upgrades.json";
    public static JsonObject itemData;
    public static Map<String, String> upgradeData;
    public static Region activeRegion = null;
    public static List<String> players = List.of();

    @Override
    public void onInitializeClient() {
        Config.HANDLER.load();
        // upgrade data
        // i forgot why the fuck we load this here but when i tried changing it, it caused like atleast 4 different crash issues
        JsonObject upgrade_dataJson = loadJsonFile(UPGRADES_PATH);
        upgradeData = new HashMap<>();
        if (upgrade_dataJson != null) {
            for (String key : upgrade_dataJson.keySet()) {
                upgradeData.put(key, upgrade_dataJson.get(key).getAsString());
            }
        }

        itemData = loadJsonFile(ITEM_DATA_PATH);

        BossState.init();

        RegionLoader.init();
        RegionUpdater.init();

        KeyBindings.setup();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            DiscordManager.tick();
            if (client.player != null && client.player.getTeam() != null && onMelonKing()) {
                List<String> playerJoins = client.player.getTeam().getPlayers().stream()
                        .filter(item -> !players.contains(item))
                        .toList();
                List<String> playerLeaves = players.stream()
                        .filter(item -> !client.player.getTeam().getPlayers().stream().toList().contains(item))
                        .toList();
                if (!playerJoins.isEmpty()) {
                    client.player.displayClientMessage(net.minecraft.network.chat.Component.nullToEmpty("Player Join: " + playerJoins.getFirst()).copy().withStyle(ChatFormatting.AQUA), false);
                }
                if (!playerLeaves.isEmpty()) {
                    client.player.displayClientMessage(net.minecraft.network.chat.Component.nullToEmpty("Player Leave: " + playerLeaves.getFirst()).copy().withStyle(ChatFormatting.RED), false);
                }
                players = client.player.getTeam().getPlayers().stream().toList();
            }
            CustomMusicManager.tick(client);
        });

        UseEntityCallback.EVENT.register((SellEvent::entityInteract));
        AttackEntityCallback.EVENT.register((AttackEvent::entityAttack));

        DiscordManager.initialise();

        // setup clientside commands
        ClientCommandRegistrationCallback.EVENT.register(Commands::registerCommands);

        // set up the overlay rendering thingy
        HudElementRegistry.attachElementBefore(VanillaHudElements.CROSSHAIR, Identifier.parse("mwonmod:overlay"), (context, tickCounter) -> HUD.renderHUDOverlay(context));
    }

    public static JsonObject loadJsonFile(String PATH) {
        JsonObject ret = null;
        try (InputStream stream = Mwonmod.class.getClassLoader().getResourceAsStream(PATH)) {
            if (stream == null) {
                LOGGER.error("Could not find data file: " + UPGRADES_PATH);
                return null;
            }
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            JsonElement jsonElement = JsonParser.parseReader(reader);

            if (jsonElement.isJsonObject()) {
                LOGGER.info("Successfully loaded data from: " + UPGRADES_PATH);
                ret = jsonElement.getAsJsonObject();
            } else {
                LOGGER.error("Data file is not a JSON object: " + UPGRADES_PATH);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error loading data from: " + UPGRADES_PATH);
        }
        return ret;
    }

    public static InventoryScanResult scanInventory(Player player, List<String> itemsToCount) {
        Map<String, Integer> counts = new HashMap<>();
        Map<String, Integer> slotCounts = new HashMap<>();
        for (String item : itemsToCount) {
            counts.put(item, 0);
        }
        for (String item : itemsToCount) {
            slotCounts.put(item, 0);
        }
        int emptySlots = 0;

        // Scan main inventory
        for (var stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.isEmpty()) {
                emptySlots++;
            } else {
                String itemId = ItemUtils.getItemID(stack);
                if (counts.containsKey(itemId)) {
                    counts.put(itemId, counts.get(itemId) + stack.getCount());
                    slotCounts.put(itemId, slotCounts.get(itemId) + 1);
                }
            }
        }

        return new InventoryScanResult(counts, slotCounts, emptySlots);
    }

    public static boolean onMelonKing() {
        // rewrite to no longer require flint before next modrinth release, use server respawn loc data to determine node and player position to determine plot
        if (Config.HANDLER.instance().ignoreMelonKingCheck) return true;
        if (Minecraft.getInstance().getCurrentServer() == null || Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) return false;
        // check on df
        if (!Minecraft.getInstance().getCurrentServer().ip.contains("mcdiamondfire.com") && !Minecraft.getInstance().getCurrentServer().ip.contains("diamondfire.games")) return false;
        // check for node 2
        if (Minecraft.getInstance().level.getRespawnData().globalPos().pos().getX() != -675) return false;
        // check in plot bounds
        BlockPos plotRelativePos = Minecraft.getInstance().player.blockPosition().subtract(new Vec3i(RegionLoader.plot_origin.x, 0, RegionLoader.plot_origin.y));
        BlockPos betaRelativePos = Minecraft.getInstance().player.blockPosition().subtract(new Vec3i(RegionLoader.beta_plot_origin.x, 0, RegionLoader.beta_plot_origin.y));

        GameState.beta_plot = betaRelativePos.getX() >= 0 && betaRelativePos.getX() <= 1001 && betaRelativePos.getZ() >= 0 && betaRelativePos.getZ() <= 1001;
        return (plotRelativePos.getX() >= 0 && plotRelativePos.getX() <= 301 && plotRelativePos.getZ() >= 0 && plotRelativePos.getZ() <= 301) ||
                (GameState.beta_plot);

//        if (Flint.getUser().getPlot() == null) {GameState.beta_plot=false;return false;}
//        if (Flint.getUser().getMode() != Mode.PLAY) return false;
//        GameState.beta_plot = Flint.getUser().getPlot().getId() == 202028;
//        return Flint.getUser().getPlot().getId() == 22467 || Flint.getUser().getPlot().getId() == 202028;
    }

    public static void notification(String title, String message) {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        try {
            if (os.contains("nux") || os.contains("nix")) {
                InputStream iconStream = Mwonmod.class.getResourceAsStream("/assets/mwonmod/melon.png");
                if (iconStream == null) {
                    LOGGER.error("Icon resource not found.");
                    return;
                }

                Path tempIcon = Files.createTempFile("mwonmod-icon-", ".png");
                Files.copy(iconStream, tempIcon, StandardCopyOption.REPLACE_EXISTING);
                iconStream.close();
                tempIcon.toFile().deleteOnExit();
                new ProcessBuilder("notify-send", "-i", tempIcon.toString(), title, message)
                        .inheritIO()
                        .start();
            } else {
                if (SystemTray.isSupported()) {
                    SystemTray tray = SystemTray.getSystemTray();
                    Image icon = Toolkit.getDefaultToolkit().getImage(
                            Mwonmod.class.getResource("/icon.png")
                    );
                    TrayIcon trayIcon = new TrayIcon(icon, "Mwonmod Notification");
                    trayIcon.setImageAutoSize(true);
                    tray.add(trayIcon);
                    trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
                } else {
                    LOGGER.error("SystemTray not supported; fallback here.");
                }
            }
        } catch (IOException | AWTException e) {
            e.printStackTrace();
        }
    }

    public static void setActiveRegion(Region region) {
        activeRegion = region;
    }

    public static void clearActiveRegion() {
        activeRegion = null;
    }

    public static void expandActiveRegionTo(Vec3i pos) {
        activeRegion.expandTo(pos);
    }
}
