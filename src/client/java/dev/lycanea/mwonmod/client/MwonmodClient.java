package dev.lycanea.mwonmod.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.dfonline.flint.Flint;
import dev.dfonline.flint.FlintAPI;
import dev.dfonline.flint.hypercube.Mode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.title.Title;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.*;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.glfw.GLFW;
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
import java.util.regex.Pattern;

public class MwonmodClient implements ClientModInitializer {
    public static final String MOD_ID = "mwonmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftClient MC = MinecraftClient.getInstance();
    private static KeyBinding bankKeyBinding;
    private static KeyBinding forgeKeyBinding;
    public static Boolean inventory_rundown = false;
    private static final String DATA_PATH = "assets/mwonmod/data/items.json";
    private static final String UPGRADES_PATH = "assets/mwonmod/data/melonmod_upgrades.json";
    public static JsonObject itemData;
    public static Map<String, String> upgradeData;
    private static boolean auctionNotificationSent = false;

    @Override
    public void onInitializeClient() {
        Config.HANDLER.load();
        // upgrade data
        JsonObject upgrade_dataJson = loadJsonFile(UPGRADES_PATH);
        upgradeData = new HashMap<>();
        if (upgrade_dataJson != null) {
            for (String key : upgrade_dataJson.keySet()) {
                upgradeData.put(key, upgrade_dataJson.get(key).getAsString());
            }
        }
        LOGGER.info(upgradeData.toString());

        itemData = loadJsonFile(DATA_PATH);

        // make flint check the players plot
        FlintAPI.confirmLocationWithLocate();

        // setup keybinds
        bankKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mwonmod.bank", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_UNKNOWN, // The keycode of the key
            "lycanea.mwonmod.keybinds" // The translation key of the keybinding's category.
        ));
        forgeKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mwonmod.forge", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_UNKNOWN, // The keycode of the key
                "lycanea.mwonmod.keybinds" // The translation key of the keybinding's category.
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (bankKeyBinding.wasPressed()) {
                assert client.player != null;
                if (!(client.world == null) && onMelonKing()) {
//                    client.player.sendMessage(Text.literal("Bank Opened"), false);
                    client.execute(() -> client.player.networkHandler.sendChatMessage("@bank"));
                }
            }
            while (forgeKeyBinding.wasPressed()) {
                assert client.player != null;
                if (!(client.world == null) && onMelonKing()) {
//                    client.player.sendMessage(Text.literal("Forge Opened"), false);
                    client.execute(() -> client.player.networkHandler.sendChatMessage("@forge"));
                }
            }
        });

        UseEntityCallback.EVENT.register((SellEvent::entityInteract));
        AttackEntityCallback.EVENT.register((AttackEvent::entityAttack));

        // setup clientside commands
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager
                .literal("itemlookup")
                .then(ClientCommandManager.argument("value", StringArgumentType.string())
                    .executes(context -> {
                        final String value = StringArgumentType.getString(context, "value");
                        JsonObject lookupItemData = itemData.get(value.toLowerCase().replaceAll(" ", "_")).getAsJsonObject();
                        assert MinecraftClient.getInstance().player != null;
                        MinecraftClient.getInstance().player.sendMessage(Text.literal(String.valueOf(lookupItemData.get("name").getAsString())).styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(lookupItemData.get("description").getAsString() )))), false);
                        return 1;
                    }))));
        }

        // set up the overlay rendering thingy
        HudRenderCallback.EVENT.register((context, tickDelta) -> renderHUDOverlay(context));
    }

    public static JsonObject loadJsonFile(String PATH) {
        JsonObject ret = null;
        try (InputStream stream = MwonmodClient.class.getClassLoader().getResourceAsStream(PATH)) {
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
            context.drawText(client.textRenderer, "DEBUG MODE", 0, context.getScaledWindowHeight()-20, 0xFFFFFF, true);
            context.drawText(client.textRenderer, "ON MWON: " + onMelonKing(), 0, context.getScaledWindowHeight()-10, 0xFFFFFF, true);
        }

        if (!(client.player == null) && !(client.world == null) && onMelonKing()) {
            long auctionwaitMillis = TimeUtils.auctionTime();
            if (!auctionNotificationSent && auctionwaitMillis <= 30000) {
                if (Config.HANDLER.instance().auctionDesktopNotification) {
                    notification("Auction Alert", "There is an auction in 30 seconds.");
                }
                if (Config.HANDLER.instance().auctionTitleNotification) {
                    client.player.showTitle(Title.title(net.kyori.adventure.text.Component.text("There is an auction in 30 seconds."), net.kyori.adventure.text.Component.text("")));
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

        if (!(client.player == null) && !(client.world == null) && onMelonKing() && MwonmodClient.inventory_rundown) {

            // okay uhh quick note to self, you first need to completely remove the inventory scan thing and just do that in here so you have more control over it, enchanted melons should be counted seperate from regular melons for example
            int barX = client.getWindow().getScaledWidth() / 2 - 50;
            int barY = client.getWindow().getScaledHeight() - 90;
            int barWidth = 100;
            int barHeight = 6;

            List<Item> itemsToCount = Arrays.asList(Items.GOLD_NUGGET, Items.MELON_SLICE);
            InventoryScanResult result = scanInventory(client.player, itemsToCount);

            double emptyPercent = (double) result.emptySlots() / 36;
            double goldPercent = (double) result.itemSlots().get(Items.GOLD_NUGGET) / 36;
            double melonPercent = (double) result.itemSlots().get(Items.MELON_SLICE) / 36;
            double emptyWidth = (barWidth * emptyPercent);
            double goldWidth = (barWidth * goldPercent);
            double melonWidth = (barWidth * melonPercent);

            context.fill(barX-1, barY-1, barX+1 + barWidth, barY+1 + barHeight, 0xFFFFFFFF);
            context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555);
            context.fill(barX, barY, (int) (barX + emptyWidth), barY + barHeight, 0xFF888888);
            context.fill((int) (barX + emptyWidth), barY, (int) (barX + emptyWidth + goldWidth), barY + barHeight, 0xFFFFFF00);
            context.fill((int) (barX + emptyWidth + goldWidth), barY, (int) (barX + emptyWidth + goldWidth + melonWidth), barY + barHeight, 0xFF00FFFF);
        }

        if (client.getWindow() == null) return;
        if (client.player == null) return;
        if (client.world == null) return;

        if (Config.HANDLER.instance().signUpgradeTooltip) {
            Vec3d hit = client.player.raycast(4.5, 0, false).getPos();
            String vecKey = serializeVec(hit);
            BlockEntity state = client.world.getBlockEntity(new BlockPos((int) hit.x, (int) hit.y, (int) hit.z).subtract(new Vec3i(1, 0, 1)));

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

    public static InventoryScanResult scanInventory(PlayerEntity player, List<Item> itemsToCount) {
        Map<Item, Integer> counts = new HashMap<>();
        Map<Item, Integer> slotCounts = new HashMap<>();
        for (Item item : itemsToCount) {
            counts.put(item, 0);
        }
        for (Item item : itemsToCount) {
            slotCounts.put(item, 0);
        }
        int emptySlots = 0;

        // Scan main inventory
        for (var stack : player.getInventory().main) {
            if (stack.isEmpty()) {
                emptySlots++;
            } else {
                Item item = stack.getItem();
                if (counts.containsKey(item)) {
                    counts.put(item, counts.get(item) + stack.getCount());
                    slotCounts.put(item, slotCounts.get(item) + 1);
                }
            }
        }

        return new InventoryScanResult(counts, slotCounts, emptySlots);
    }

    public static boolean onMelonKing() {
//        return true;
        if (Config.HANDLER.instance().ignoreMelonKingCheck) return true;
        if (Flint.getUser().getMode() != Mode.PLAY) return false;
        assert Flint.getUser().getPlot() != null;
        return Flint.getUser().getPlot().getId() == 22467;
    }

    public static void notification(String title, String message) {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        try {
            if (os.contains("nux") || os.contains("nix")) {
                InputStream iconStream = MwonmodClient.class.getResourceAsStream("/assets/mwonmod/melon.png");
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
                            MwonmodClient.class.getResource("/icon.png")
                    );
                    TrayIcon trayIcon = new TrayIcon(icon, "MyMod Notification");
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
}
