package dev.lycanea.mwonmod.util.discord;

import dev.lycanea.mwonmod.Mwonmod;
import static dev.lycanea.mwonmod.Mwonmod.LOGGER;
import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.util.GameState;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.entities.DiscordBuild;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.time.*;
import java.util.Objects;

public class DiscordManager {
    public static IPCClient client;
    public static DiscordListener listener = new DiscordListener();
    public static boolean enabled = Config.HANDLER.instance().discordRichPresence;

    public static void initialise() {
        if (!enabled) return;

        if (client == null) {
            client = new IPCClient(1376119105136103496L);
            client.setListener(listener);
        }

        try {
            client.connect(DiscordBuild.ANY);
            LOGGER.info("Connected Discord Client");
        } catch (NoDiscordClientException e) {
            throw new RuntimeException("No Discord client found", e);
        }
    }

    public static void updateStatus() {
        if (GameState.melonJoin != null && Config.HANDLER.instance().discordRichPresence && Mwonmod.onMelonKing()) {
            // get ready for a shit ton of ternary statements
            setEnabled(true);
            if (GameState.beta_plot) {
                setStatus("On Beta Plot",
                        "Playing around and breaking things",
                        OffsetDateTime.of(GameState.melonJoin, ZoneOffset.systemDefault().getRules().getStandardOffset(Instant.now())),
                        "melonbeta",
                        "Running MwonMod " + ((FabricLoader.getInstance().getModContainer("mwonmod").map(ModContainer::getMetadata).map(ModMetadata::getVersion).map(Object::toString).orElse("0.0").equals("0.0.0")) ? "Development":FabricLoader.getInstance().getModContainer("mwonmod").map(ModContainer::getMetadata).map(ModMetadata::getVersion).map(Object::toString).orElse("0.0")),
                        "redactedpath",
                        "REDACTED :3");
                return;
            }
            java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance();
            String line1 = null;
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine1, "Coins/City Gold")) line1 = (Objects.equals(GameState.currentPath, "Underground") || Objects.equals(GameState.currentPath, "Depths")) ? ("Gold: " + formatter.format(GameState.bank_gold != null ? GameState.bank_gold:0)) : ("Coins: " + formatter.format(GameState.coins != null ? GameState.coins:0));
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine1, "Path")) line1 = "Path: " + GameState.currentPath;
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine1, "Monarch")) line1 = "Monarch: " + GameState.currentMonarch;
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine1, "Location")) line1 = ((GameState.playerLocation == null) ? null:"Location: " + (GameState.playerLocation.substring(0, 1).toUpperCase() + GameState.playerLocation.substring(1)).replace("_", " "));
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine1, "Personal Gold")) line1 = ((GameState.personal_bank == null) ? null:"Personal Gold: " + formatter.format(GameState.personal_bank));
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine1, "Medals")) line1 = ((GameState.medals == null) ? null:"Medals: " + formatter.format(GameState.medals));
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine1, "Trophies")) line1 = ((GameState.trophies == null) ? null:"Trophies: " + formatter.format(GameState.trophies));
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine1, "Karma")) line1 = ((GameState.karma == null) ? null:"Karma: " + formatter.format(GameState.karma));
            String line2 = null;
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine2, "Coins/City Gold")) line2 = (Objects.equals(GameState.currentPath, "Underground") || Objects.equals(GameState.currentPath, "Depths")) ? ("Gold: " + formatter.format(GameState.bank_gold != null ? GameState.bank_gold:0)) : ("Coins: " + formatter.format(GameState.coins != null ? GameState.coins:0));
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine2, "Path")) line2 = "Path: " + GameState.currentPath;
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine2, "Monarch")) line2 = "Monarch: " + GameState.currentMonarch;
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine2, "Location")) line2 = ((GameState.playerLocation == null) ? null:"Location: " + (GameState.playerLocation.substring(0, 1).toUpperCase() + GameState.playerLocation.substring(1)).replace("_", " "));
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine2, "Personal Gold")) line2 = ((GameState.personal_bank == null) ? null:"Personal Gold: " + formatter.format(GameState.personal_bank));
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine2, "Medals")) line2 = ((GameState.medals == null) ? null:"Medals: " + formatter.format(GameState.medals));
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine2, "Trophies")) line2 = ((GameState.trophies == null) ? null:"Trophies: " + formatter.format(GameState.trophies));
            if (Objects.equals(Config.HANDLER.instance().richPresenceLine2, "Karma")) line2 = ((GameState.karma == null) ? null:"Karma: " + formatter.format(GameState.karma));
            setStatus(line2,
                    line1,
                    OffsetDateTime.of(GameState.melonJoin, ZoneOffset.systemDefault().getRules().getStandardOffset(Instant.now())),
                    "melon",
                    "Running MwonMod " + ((FabricLoader.getInstance().getModContainer("mwonmod").map(ModContainer::getMetadata).map(ModMetadata::getVersion).map(Object::toString).orElse("0.0").equals("0.0.0")) ? "Development":FabricLoader.getInstance().getModContainer("mwonmod").map(ModContainer::getMetadata).map(ModMetadata::getVersion).map(Object::toString).orElse("0.0")),
                    ((Config.HANDLER.instance().richPresencePathIcon && GameState.currentPath != null) ? GameState.currentPath.toLowerCase():null),
                    ((Config.HANDLER.instance().richPresencePathIcon && GameState.currentPath != null) ? "Path: " + GameState.currentPath:null));
        } else {
            setEnabled(false);
        }
    }

    public static void setStatus(String state, String details, OffsetDateTime startTimestamp, String largeImageKey, String largeImageText, String smallImageKey, String smallImageText) {
        if (!enabled || client == null) return;

        if (!listener.isConnected()) return;

        RichPresence presence = new RichPresence.Builder()
                .setState(state)
                .setDetails(details)
                .setStartTimestamp(startTimestamp)
                .setLargeImage(largeImageKey, largeImageText)
                .setSmallImage(smallImageKey, smallImageText)
                .build();

        client.sendRichPresence(presence);
    }

    public static void setEnabled(boolean value) {
        if (enabled == value) return; // No change

        enabled = value;

        if (!enabled && client != null) {
            client.close();
            client = null; // Clean up
        } else if (enabled && client == null) {
            initialise();
        }
    }
}
