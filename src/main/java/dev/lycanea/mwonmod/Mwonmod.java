package dev.lycanea.mwonmod;

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
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
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
    public static MinecraftClient MC = MinecraftClient.getInstance();
    public static Boolean inventory_rundown = false;
    private static final String ITEM_DATA_PATH = "assets/mwonmod/data/items.json";
    private static final String UPGRADES_PATH = "assets/mwonmod/data/melonmod_upgrades.json";
    public static JsonObject itemData;
    public static Map<String, String> upgradeData;
    private static boolean auctionNotificationSent = false;
    public static Region activeRegion = null;

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

        RegionLoader.init();
        RegionRenderer.init();
        RegionUpdater.init();

        // make flint check the players plot
        FlintAPI.confirmLocationWithLocate();

        KeyBindings.setup();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            DiscordManager.updateStatus();
        });

        UseEntityCallback.EVENT.register((SellEvent::entityInteract));
        AttackEntityCallback.EVENT.register((AttackEvent::entityAttack));

        DiscordManager.initialise();

        // setup clientside commands
        ClientCommandRegistrationCallback.EVENT.register(Commands::registerCommands);

        // set up the overlay rendering thingy
        HudRenderCallback.EVENT.register((context, tickDelta) -> renderHUDOverlay(context));
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

    private void renderHUDOverlay(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
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

            assert MinecraftClient.getInstance().player != null;
            BlockPos pos = MinecraftClient.getInstance().player.getBlockPos().add(-plot_origin.x, 0, -plot_origin.y);
            if (GameState.beta_plot) {
                pos = MinecraftClient.getInstance().player.getBlockPos().add(-beta_plot_origin.x, 0, -beta_plot_origin.y);
            }
            debugLines.add("PLOTSPACE POS: " + pos);

            int startY = context.getScaledWindowHeight() - 390;
            drawDebugLines(context, client, debugLines, startY, 10, 0x82B7ED);
        }
        if (!(client.player == null) && !(client.world == null) && onMelonKing()) {
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
            if (Config.HANDLER.instance().auctionTimer) {
                long auctionminutes = auctionwaitMillis / 60000;
                long auctionseconds = (auctionwaitMillis % 60000) / 1000;
                context.drawTextWithShadow(client.textRenderer, String.format("Next Auction:  00:%02d:%02d", auctionminutes, auctionseconds), 3, 3, 0xFFFFFF);
            }
            if (Config.HANDLER.instance().flawlessTimer) {
                long flawlessWait = TimeUtils.flawlessTime();
                if (flawlessWait < 4428) {
                    context.drawTextWithShadow(client.textRenderer, String.format("Next Flawless: %02d:%02d:%02d", flawlessWait / 60 / 60, (flawlessWait + 1) / 60 % 60, (flawlessWait + 1)%60), 3, 12, 0xFFFFFF);
                } else {
                    context.drawTextWithShadow(client.textRenderer, "Next Flawless: Now", 3, 12, 0xFFFFFF);
                }
            }
        }

        if (!(client.player == null) && !(client.world == null) && onMelonKing() && Mwonmod.inventory_rundown) {

            // okay uhh quick note to self, you first need to completely remove the inventory scan thing and just do that in here so you have more control over it, enchanted melons should be counted seperate from regular melons for example
            int barX = client.getWindow().getScaledWidth() / 2 - 50;
            int barY = client.getWindow().getScaledHeight() - 90;
            int barWidth = 100;
            int barHeight = 6;

            List<String> itemsToCount = Arrays.asList("melon", "enchanted_melon", "super_enchanted_melon", "gold", "shard", "compressed_shard");
            InventoryScanResult result = scanInventory(client.player, itemsToCount);

            double emptyPercent = (double) result.emptySlots() / 36;
            double goldPercent = (double) result.itemSlots().get("gold") / 36;
            double shardPercent = (double) result.itemSlots().get("shard") / 36;
            double compressedShardPercent = (double) result.itemSlots().get("compressed_shard") / 36;
            double melonPercent = (double) result.itemSlots().get("melon") / 36;
            double enchantedMelonPercent = (double) result.itemSlots().get("enchanted_melon") / 36;
            double superEnchantedMelonPercent = (double) result.itemSlots().get("super_enchanted_melon") / 36;
            double emptyWidth = (barWidth * emptyPercent);
            double goldWidth = (barWidth * goldPercent);
            double shardWidth = (barWidth * shardPercent);
            double compressedShardWidth = (barWidth * compressedShardPercent);
            double melonWidth = (barWidth * melonPercent);
            double enchantedMelonWidth = (barWidth * enchantedMelonPercent);
            double superEnchantedMelonWidth = (barWidth * superEnchantedMelonPercent);

            context.fill(barX-1, barY-1, barX+1 + barWidth, barY+1 + barHeight, 0xFF000000); // Border
            context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555); // Misc
            context.fill(barX, barY, (int) (barX + emptyWidth), barY + barHeight, 0xFF888888); // Background
            context.fill((int) (barX + emptyWidth), barY, (int) (barX + emptyWidth + goldWidth), barY + barHeight, 0xFFFFE100); // Gold
            context.fill((int) (barX + emptyWidth + goldWidth), barY, (int) (barX + emptyWidth + goldWidth + shardWidth), barY + barHeight, 0xFF00AAFF); // Shard
            context.fill((int) (barX + emptyWidth + goldWidth + shardWidth), barY, (int) (barX + emptyWidth + goldWidth + shardWidth + compressedShardWidth), barY + barHeight, 0xFF0066DB); // CShard
            context.fill((int) (barX + emptyWidth + goldWidth + shardWidth + compressedShardWidth), barY, (int) (barX + emptyWidth + goldWidth + shardWidth + compressedShardWidth + melonWidth), barY + barHeight, 0xFF00FF43); // Melon
            context.fill((int) (barX + emptyWidth + goldWidth + shardWidth + compressedShardWidth + melonWidth), barY, (int) (barX + emptyWidth + goldWidth + shardWidth + compressedShardWidth + melonWidth + enchantedMelonWidth), barY + barHeight, 0xFF00BF32); // EMelon
            context.fill((int) (barX + emptyWidth + goldWidth + shardWidth + compressedShardWidth + melonWidth + enchantedMelonWidth), barY, (int) (barX + emptyWidth + goldWidth + shardWidth + compressedShardWidth + melonWidth + enchantedMelonWidth + superEnchantedMelonWidth), barY + barHeight, 0xFF008A24); // SEMelon
        }

        if (client.getWindow() == null) return;
        if (client.player == null) return;
        if (client.world == null) return;

        if (Config.HANDLER.instance().signUpgradeTooltip) {
            Vec3d hit = client.player.raycast(4.5, 0, false).getPos();
            String vecKey = serializeVec(hit);
            BlockEntity state = client.world.getBlockEntity(new BlockPos((int) hit.x, (int) hit.y, (int) hit.z).subtract(new Vec3i(GameState.beta_plot ? 0:1, 0, GameState.beta_plot ? 0:1))); // beta plot is +x and +z so blockpos offset isnt needed im p sure

            ArrayList<Text> signTooltip = new ArrayList<>();
            if (state instanceof SignBlockEntity signBlock) {
                for (Text[] textList : new Text[][]{signBlock.getFrontText().getMessages(true), signBlock.getBackText().getMessages(true)}) {
                    if (textList.length == 4 && !textList[0].getString().trim().isEmpty()) {
                        String top = textList[0].getString().trim();
                        String m = textList[1].getString().trim();
                        String m2 = textList[2].getString().trim();
                        if (textList[3].getString().contains("Reserve")) return; // fixes Better Bankers in beneath showing the upgrade desc for Better Bankers the upgrade
                        if (!m2.isEmpty()) m += " " + m2;
                        if (m.contains("City Improvement:")) m = m2;

                        int color = 0xFFFFFF;

                        if (top.equals("[Right Click]")) color = 0xfcdb6d;
                        if (top.equals("Bought!")) color = Colors.GREEN;
                        if (top.equals("Can't Buy") || Pattern.compile(".\\d/.\\d").matcher(top).find())
                            color = Colors.LIGHT_RED;
                        if (top.equals("Locked") || top.equals("Locked!") || top.equals("Path Locked!"))
                            color = Colors.RED;


                        String mkey = m;
                        if (m.equals("Upgrade Town")) {
                            mkey = m + vecKey;
                        }
                        if (!upgradeData.containsKey(mkey)) return;
                        String upgradeDesc = upgradeData.get(mkey);
                        //Mod.log("n:" + mkey + " d: " + upgradeDesc);

                        upgradeDesc = upgradeDesc.replace(" queen ", " monarch ").replace(" king ", " monarch ");
                        List<OrderedText> otList = MC.textRenderer.wrapLines(StringVisitable.plain(upgradeDesc), client.getWindow().getScaledWidth() / 3);

                        signTooltip = new ArrayList<>(List.of(Text.literal(m).withColor(color)));
                        for (OrderedText t : otList) {
                            Text nt = convertOrderedTextToTextWithStyle(t);
                            signTooltip.add(Text.literal(nt.getString()).withColor(Colors.LIGHT_GRAY));
                        }
                    }
                }
            }

            if (!signTooltip.isEmpty()) {
                context.drawTooltip(client.textRenderer, signTooltip, client.getWindow().getScaledWidth() / 2, client.getWindow().getScaledHeight() / 2);
            }
        }
    }

    private static String serializeVec(Vec3d vec) {
        return "<" + (int) vec.x + ", " + (int) vec.y + ", " + (int) vec.z + ">";
    }

    public static Text convertOrderedTextToTextWithStyle(OrderedText orderedText) {
        List<Text> components = new ArrayList<>();
        StringBuilder currentText = new StringBuilder();
        final Style[] currentStyle = {Style.EMPTY};

        orderedText.accept((index, style, codePoint) -> {
            if (!style.equals(currentStyle[0])) {
                if (!currentText.isEmpty()) {
                    components.add(Text.literal(currentText.toString()).setStyle(currentStyle[0]));
                    currentText.setLength(0);
                }
                currentStyle[0] = style;
            }
            currentText.appendCodePoint(codePoint);
            return true;
        });

        if (!currentText.isEmpty()) {
            components.add(Text.literal(currentText.toString()).setStyle(currentStyle[0]));
        }

        return Texts.join(components, Text.empty());
    }

    public static InventoryScanResult scanInventory(PlayerEntity player, List<String> itemsToCount) {
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
        for (var stack : player.getInventory().main) {
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
        if (Flint.getUser().getPlot() == null) {GameState.beta_plot=false;return false;}
        GameState.beta_plot = Flint.getUser().getPlot().getId() == 202028;
        if (Config.HANDLER.instance().ignoreMelonKingCheck) return true;
        if (Flint.getUser().getMode() != Mode.PLAY) return false;
        return Flint.getUser().getPlot().getId() == 22467 || Flint.getUser().getPlot().getId() == 202028;
    }

    public void drawDebugLines(DrawContext context, MinecraftClient client, List<String> lines, int startY, int lineSpacing, int color) {
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, client.textRenderer.getWidth(line));
        }

        context.fill(RenderLayer.getEndPortal(), 0, startY - 2, maxWidth + 4, startY + (lines.size() * lineSpacing), 0x80000000);

        int y = startY;
        for (String line : lines) {
            context.drawText(client.textRenderer, line, 0, y, color, true);
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
