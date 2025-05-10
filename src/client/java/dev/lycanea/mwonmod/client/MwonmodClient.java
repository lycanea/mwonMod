package dev.lycanea.mwonmod.client;

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
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MwonmodClient implements ClientModInitializer {

    public static final String MOD_ID = "mwonmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static KeyBinding keyBinding;
    public Boolean DEBUG = false;
    public static Boolean inventory_rundown = false;

    @Override
    public void onInitializeClient() {
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
        }

        // set up the overlay rendering thingy
        HudRenderCallback.EVENT.register((context, tickDelta) -> renderHUDOverlay(context));
    }

    private void renderHUDOverlay(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (DEBUG) {
            context.drawText(client.textRenderer, "DEBUG MODE", 0, 0, 0xFFFFFF, true);
            context.drawText(client.textRenderer, "ON MWON: " + onMelonKing(), 0, 10, 0xFFFFFF, true);
        }

        if (!(client.player == null) && !(client.world == null) && onMelonKing()) {
            long waitMillis = TimeUtils.millisUntilNextHalfOrHourUTC();
            context.drawTextWithShadow(client.textRenderer, "Next Auction: " + waitMillis / 60000  + "m", 3, 3, 0xFFFFFF);
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

    private InventoryScanResult scanInventory(net.minecraft.entity.player.PlayerEntity player, List<Item> itemsToCount) {
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
