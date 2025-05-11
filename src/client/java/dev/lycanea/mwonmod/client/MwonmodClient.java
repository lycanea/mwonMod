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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MwonmodClient implements ClientModInitializer {

    public static final String MOD_ID = "mwonmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static KeyBinding keyBinding;
    public static Boolean DEBUG = false;
    public static Boolean inventory_rundown = false;
    private static final String DATA_PATH = "assets/mwonmod/data/items.json";
    public static JsonObject itemData = null;

    @Override
    public void onInitializeClient() {
        try (InputStream stream = MwonmodClient.class.getClassLoader().getResourceAsStream(DATA_PATH)) {
            if (stream == null) {
                LOGGER.error("Could not find data file: " + DATA_PATH);
                return;
            }
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            JsonElement jsonElement = JsonParser.parseReader(reader);

            if (jsonElement.isJsonObject()) {
                itemData = jsonElement.getAsJsonObject();
                LOGGER.info("Successfully loaded data from: " + DATA_PATH);
            } else {
                LOGGER.error("Data file is not a JSON object: " + DATA_PATH);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error loading data from: " + DATA_PATH);
        }

        // make flint check the players plot
        FlintAPI.confirmLocationWithLocate();

        // setup keybinds
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mwonmod.bank", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_UNKNOWN, // The keycode of the key
                "lycanea.mwonmod.keybinds" // The translation key of the keybinding's category.
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                assert client.player != null;
                if (!(client.world == null) && onMelonKing()) {
                    client.player.sendMessage(Text.literal("Bank Opened"), false);
                    client.execute(() -> client.player.networkHandler.sendChatMessage("@bank"));
                }
            }
        });

        // setup clientside commands
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager
                    .literal("debug")
                    .executes(context -> {
                        LOGGER.info("{}", onMelonKing());
                        DEBUG = !DEBUG;
                        return 1;
                    })));
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager
                    .literal("itemlookup")
                    .then(ClientCommandManager.argument("value", StringArgumentType.string())
                            .executes(context -> {
                                final String value = StringArgumentType.getString(context, "value");
                                JsonElement lookupItemData = itemData.get(value.toLowerCase().replaceAll(" ", "_"));
                                assert MinecraftClient.getInstance().player != null;
                                MinecraftClient.getInstance().player.sendMessage(Text.literal(String.valueOf(lookupItemData)), false);
                                return 1;
                            }))));
        }

        // set up the overlay rendering thingy
        HudRenderCallback.EVENT.register((context, tickDelta) -> renderHUDOverlay(context));
    }

    private void renderHUDOverlay(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (DEBUG) {
            context.drawText(client.textRenderer, "DEBUG MODE", 0, context.getScaledWindowHeight()-20, 0xFFFFFF, true);
            context.drawText(client.textRenderer, "ON MWON: " + onMelonKing(), 0, context.getScaledWindowHeight()-10, 0xFFFFFF, true);
        }

        if (!(client.player == null) && !(client.world == null) && onMelonKing()) {
            long auctionwaitMillis = TimeUtils.auctionTime();
            long auctionminutes = auctionwaitMillis / 60000;
            long auctionseconds = (auctionwaitMillis % 60000) / 1000;
            context.drawTextWithShadow(client.textRenderer, String.format("Next Auction: %02d:%02d", auctionminutes, auctionseconds), 3, 3, 0xFFFFFF);
            long flawlessWait = TimeUtils.flawlessTime();
            if (flawlessWait < 4478) {
                context.drawTextWithShadow(client.textRenderer, String.format("Next Flawless: %02d:%02d", flawlessWait / 60 / 60, flawlessWait / 60 % 60), 3, 12, 0xFFFFFF);
            } else {
                context.drawTextWithShadow(client.textRenderer, "Next Flawless: Now", 3, 12, 0xFFFFFF);
            }
        }

        if (!(client.player == null) && !(client.world == null) && onMelonKing() && MwonmodClient.inventory_rundown) {

            // okay uhh quick note to self, you first need to completely remove the inventory scan thing and just do that in here so you have more control over it, enchanted melons should be counted seperate from regular melons for example
            int barX = client.getWindow().getScaledWidth() / 2 - 50;
            int barY = client.getWindow().getScaledHeight() / 2 - 20;
            int barWidth = 100;
            int barHeight = 10;

            List<Item> itemsToCount = Arrays.asList(Items.GOLD_NUGGET, Items.MELON_SLICE);
            InventoryScanResult result = scanInventory(client.player, itemsToCount);

            double emptyPercent = (double) result.emptySlots() / 36;
            double goldPercent = (double) result.itemSlots().get(Items.GOLD_NUGGET) / 36;
            double melonPercent = (double) result.itemSlots().get(Items.MELON_SLICE) / 36;
            int emptyWidth = (int) (barWidth * emptyPercent);
            int goldWidth = (int) (barWidth * goldPercent);
            int melonWidth = (int) (barWidth * melonPercent);

            context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555);
            context.fill(barX, barY, barX + emptyWidth, barY + barHeight, 0xFF888888);
            context.fill(barX + emptyWidth, barY, barX + emptyWidth + goldWidth, barY + barHeight, 0xFFFFFF00);
            context.fill(barX + emptyWidth + goldWidth, barY, barX + emptyWidth + goldWidth + melonWidth, barY + barHeight, 0xFF00FFFF);
        }
    }

    private InventoryScanResult scanInventory(PlayerEntity player, List<Item> itemsToCount) {
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
        if (Flint.getUser().getMode() != Mode.PLAY) return false;
        assert Flint.getUser().getPlot() != null;
        return Flint.getUser().getPlot().getId() == 22467;
    }
}
