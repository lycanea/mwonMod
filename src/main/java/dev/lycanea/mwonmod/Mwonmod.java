package dev.lycanea.mwonmod;

import dev.lycanea.mwonmod.music.CustomMusicManager;
import dev.lycanea.mwonmod.util.*;
import dev.lycanea.mwonmod.util.region.*;
import dev.lycanea.mwonmod.util.discord.DiscordManager;
import dev.lycanea.mwonmod.events.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.flint.Flint;
import dev.dfonline.flint.FlintAPI;
import dev.dfonline.flint.hypercube.Mode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static dev.lycanea.mwonmod.util.region.RegionLoader.beta_plot_origin;
import static dev.lycanea.mwonmod.util.region.RegionLoader.plot_origin;

public class Mwonmod implements ClientModInitializer {
    public static final String MOD_ID = "mwonmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Minecraft MC = Minecraft.getInstance();
    public static Boolean inventory_rundown = false;
    private static final String ITEM_DATA_PATH = "assets/mwonmod/data/items.json";
    private static final String UPGRADES_PATH = "assets/mwonmod/data/melonmod_upgrades.json";
    public static JsonObject itemData;
    public static Map<String, String> upgradeData;
    private static boolean auctionNotificationSent = false;
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

        // make flint check the players plot
        FlintAPI.confirmLocationWithLocate();

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
        HudElementRegistry.attachElementBefore(VanillaHudElements.CROSSHAIR, Identifier.parse("mwonmod:overlay"), (context, tickCounter) -> renderHUDOverlay(context));
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

    private void renderHUDOverlay(GuiGraphics context) {
        // we really should move this to a seperate class and... clean it up more
        Minecraft client = Minecraft.getInstance();
        if (Config.HANDLER.instance().debugMode) {
            List<String> debugLines = new ArrayList<>(List.of(
                    "DEBUG MODE",
                    "ON MWON: " + onMelonKing(),
                    "ON BETA: " + GameState.beta_plot
            ));
            if (GameState.housing_pos != null) debugLines.add("HOUSING LOCATION: " + GameState.housing_pos);
            if (GameState.currentPath != null) debugLines.add("CURRENT PATH: " + GameState.currentPath);
            if (GameState.currentMonarch != null) debugLines.add("CURRENT MONARCH: " + GameState.currentMonarch);
            if (GameState.coins != null) debugLines.add("CURRENT COINS: " + GameState.coins);
            if (GameState.bank_gold != null) debugLines.add("CURRENT BANK GOLD: " + GameState.bank_gold);
            if (GameState.playerLocation != null) debugLines.add("CURRENT LOCATION: " + GameState.playerLocation);
            if (GameState.personal_bank != null) debugLines.add("CURRENT PERSONAL BANK: " + GameState.personal_bank);
            if (GameState.medals != null) debugLines.add("CURRENT MEDALS: " + GameState.medals);
            if (GameState.trophies != null) debugLines.add("CURRENT TROPHIES: " + GameState.trophies);
            if (GameState.karma != null) debugLines.add("CURRENT KARMA: " + GameState.karma);
            if (GameState.melonJoin != null) debugLines.add("MWON TIMER: " + Duration.between(GameState.melonJoin, LocalDateTime.now()).getSeconds());
            if (BossState.boss != null) debugLines.add("CURRENT BOSS: " + BossState.boss.bossID);

            assert Minecraft.getInstance().player != null;
            BlockPos pos = Minecraft.getInstance().player.blockPosition().offset(-plot_origin.x, 0, -plot_origin.y);
            if (GameState.beta_plot) {
                pos = Minecraft.getInstance().player.blockPosition().offset(-beta_plot_origin.x, 0, -beta_plot_origin.y);
            }
            debugLines.add("PLOTSPACE POS: " + pos);

            int startY = context.guiHeight() /2 - debugLines.toArray().length*5;
            drawDebugLines(context, client, debugLines, startY, 10, 0xFF82B7ED);
        }
        if (!(client.player == null) && !(client.level == null) && onMelonKing()) {
            long auctionwaitMillis = TimeUtils.auctionTime();
            if (!auctionNotificationSent && auctionwaitMillis <= 30000) {
                if (Config.HANDLER.instance().auctionDesktopNotification) {
                    notification("Auction Alert", "There is an auction in 30 seconds.");
                }
                if (Config.HANDLER.instance().auctionTitleNotification) {
                    client.player.showTitle(Title.title(Component.text("There is an auction in 30 seconds."), Component.text("")));
                }
                auctionNotificationSent = true;
            }
            if (auctionNotificationSent && auctionwaitMillis > 30000) {
                auctionNotificationSent = false;
            }
            int flawlessTimerOffset = 3;
            if (Config.HANDLER.instance().auctionTimer) {
                long auctionminutes = auctionwaitMillis / 60000;
                long auctionseconds = (auctionwaitMillis % 60000) / 1000;
                context.drawString(client.font, String.format("Next Auction:  00:%02d:%02d", auctionminutes, auctionseconds), 3, 3, 0xFFFFFFFF);
                flawlessTimerOffset += 9;
            }
            if (Config.HANDLER.instance().flawlessTimer) {
                long flawlessWait = TimeUtils.flawlessTime();
                if (flawlessWait < 4428) {
                    context.drawString(client.font, String.format("Next Flawless: %02d:%02d:%02d", flawlessWait / 60 / 60, (flawlessWait + 1) / 60 % 60, (flawlessWait + 1)%60), 3, flawlessTimerOffset, 0xFFFFFFFF);
                } else {
                    context.drawString(client.font, "Next Flawless: Now", 3, flawlessTimerOffset, 0xFFFFFFFF);
                }
            }
        }

        if (!(client.player == null) && !(client.level == null) && onMelonKing() && Mwonmod.inventory_rundown) {
            int barY = client.getWindow().getGuiScaledHeight() - 90;
            int barWidth = 100;
            int barHeight = 6;
            if(Config.HANDLER.instance().showPercentageInInventoryOverview) {
                barHeight = 9;
                barWidth = 120;
            }
            int barX = client.getWindow().getGuiScaledWidth() / 2 - (barWidth / 2);

            Map<String, Integer> items = java.util.Map.of("gold", 0xFFFFE100, "shard", 0xFF00AAFF, "compressed_shard", 0xFF0066DB, "melon", 0xFF00FF43, "enchanted_melon", 0xFF00BF32, "super_enchanted_melon", 0xFF008A24);
            InventoryScanResult result = scanInventory(client.player, items.keySet().stream().toList());

            double emptyPercent = (double) result.emptySlots() / 36;
            double emptyWidth = (barWidth * emptyPercent);

            context.fill(barX-1, barY-1, barX+1 + barWidth, barY+1 + barHeight, 0xFF000000); // Border
            context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555); // Misc
            context.fill(barX, barY, (int) (barX + emptyWidth), barY + barHeight, 0xFF888888); // Empty

            double offset = emptyWidth;
            for (Map.Entry<String, Integer> entry : items.entrySet()) {
                double percent = (double) result.itemSlots().get(entry.getKey()) / 36;
                double width = (barWidth * percent);
                context.fill((int) (barX + offset), barY, (int) (barX + offset + width), barY + barHeight, entry.getValue());
                offset += width;
            }

            if(Config.HANDLER.instance().showPercentageInInventoryOverview) {
                String inventoryOverviewText =
                        " "
                        + String.valueOf(Math.floor(emptyPercent * 100)).replace(".0", "") +
                        "% empty" +
                        " " +
                        "(" + result.emptySlots() + " slots)";
                context.drawString(
                        Minecraft.getInstance().font,
                        inventoryOverviewText,
                        barX + 1,
                        barY + 1,
                        CommonColors.BLACK,
                        false
                );
            }
        }

        if (client.getWindow() == null) return;
        if (client.player == null) return;
        if (client.level == null) return;

        if (Config.HANDLER.instance().signUpgradeTooltip && onMelonKing()) {

            Vec3 hit = client.player.pick(4.5, 0, false).getLocation();
            String vecKey = serializeVec(hit);
            BlockEntity state = client.level.getBlockEntity(new BlockPos((int) hit.x, (int) hit.y, (int) hit.z).subtract(new Vec3i(GameState.beta_plot ? 0:1, 0, GameState.beta_plot ? 0:1))); // beta plot is +x and +z so blockpos offset isnt needed im p sure

            ArrayList<ClientTooltipComponent> signTooltip = new ArrayList<>();
            if (state instanceof SignBlockEntity signBlock) {
                for (net.minecraft.network.chat.Component[] textList : new net.minecraft.network.chat.Component[][]{signBlock.getFrontText().getMessages(true), signBlock.getBackText().getMessages(true)}) {
                    if (textList.length == 4 && !textList[0].getString().trim().isEmpty()) {
                        String top = textList[0].getString().trim();
                        String m = textList[1].getString().trim();
                        String m2 = textList[2].getString().trim();
                        if (textList[3].getString().contains("Reserve")) return; // fixes Better Bankers in beneath showing the upgrade desc for Better Bankers the upgrade
                        if (!m2.isEmpty()) m += " " + m2;
                        if (m.contains("City Improvement:")) m = m2;

                        int color = 0xFFFFFFFF;

                        if (top.equals("[Right Click]")) color = 0xfcdb6d;
                        if (top.equals("Bought!")) color = CommonColors.GREEN;
                        if (top.equals("Can't Buy") || Pattern.compile(".\\d/.\\d").matcher(top).find())
                            color = CommonColors.SOFT_RED;
                        if (top.equals("Locked") || top.equals("Locked!") || top.equals("Path Locked!") || top.equals("Disabled"))
                            color = CommonColors.RED;


                        String mkey = m;
                        if (m.equals("Upgrade Town")) {
                            mkey = m + vecKey;
                        }
                        if (!upgradeData.containsKey(mkey)) return;
                        String upgradeDesc = upgradeData.get(mkey);
                        //Mod.log("n:" + mkey + " d: " + upgradeDesc);

                        upgradeDesc = upgradeDesc.replace(" queen ", " monarch ").replace(" king ", " monarch ");
                        List<FormattedCharSequence> otList = MC.font.split(FormattedText.of(upgradeDesc), client.getWindow().getGuiScaledWidth() / 3);

                        signTooltip = new ArrayList<>(List.of(ClientTooltipComponent.create(net.minecraft.network.chat.Component.literal(m).withColor(color).getVisualOrderText())));
                        for (FormattedCharSequence t : otList) {
                            net.minecraft.network.chat.Component nt = convertOrderedTextToTextWithStyle(t);
                            signTooltip.add(ClientTooltipComponent.create(net.minecraft.network.chat.Component.literal(nt.getString()).withColor(CommonColors.LIGHT_GRAY).getVisualOrderText()));
                        }
                    }
                }
            }

            if (!signTooltip.isEmpty()) {
                context.renderTooltip(client.font, signTooltip, client.getWindow().getGuiScaledWidth() / 2, client.getWindow().getGuiScaledHeight() / 2, DefaultTooltipPositioner.INSTANCE, null);
            }
        }
    }

    private static String serializeVec(Vec3 vec) {
        return "<" + (int) vec.x + ", " + (int) vec.y + ", " + (int) vec.z + ">";
    }

    public static net.minecraft.network.chat.Component convertOrderedTextToTextWithStyle(FormattedCharSequence orderedText) {
        List<net.minecraft.network.chat.Component> components = new ArrayList<>();
        StringBuilder currentText = new StringBuilder();
        final Style[] currentStyle = {Style.EMPTY};

        orderedText.accept((index, style, codePoint) -> {
            if (!style.equals(currentStyle[0])) {
                if (!currentText.isEmpty()) {
                    components.add(net.minecraft.network.chat.Component.literal(currentText.toString()).setStyle(currentStyle[0]));
                    currentText.setLength(0);
                }
                currentStyle[0] = style;
            }
            currentText.appendCodePoint(codePoint);
            return true;
        });

        if (!currentText.isEmpty()) {
            components.add(net.minecraft.network.chat.Component.literal(currentText.toString()).setStyle(currentStyle[0]));
        }

        return ComponentUtils.formatList(components, net.minecraft.network.chat.Component.empty());
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
//        return true;
        if (Config.HANDLER.instance().ignoreMelonKingCheck) return true;
        if (Flint.getUser().getPlot() == null) {GameState.beta_plot=false;return false;}
        if (Flint.getUser().getMode() != Mode.PLAY) return false;
        GameState.beta_plot = Flint.getUser().getPlot().getId() == 202028;
        return Flint.getUser().getPlot().getId() == 22467 || Flint.getUser().getPlot().getId() == 202028;
    }

    public void drawDebugLines(GuiGraphics context, Minecraft client, List<String> lines, int startY, int lineSpacing, int color) {
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, client.font.width(line));
        }

        context.fill(0, startY - 2, maxWidth + 4, startY + (lines.size() * lineSpacing), 0xFF000000);

        int y = startY;
        for (String line : lines) {
            context.drawString(client.fontFilterFishy, line, 0, y, color, false);
            y += lineSpacing;
        }
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
