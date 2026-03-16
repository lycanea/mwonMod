package dev.lycanea.mwonmod.util;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lycanea.mwonmod.Mwonmod;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;


public class KeyBindings {

    private static KeyMapping bankKeyBinding;
    private static KeyMapping forgeKeyBinding;

    public static void setup() {
        // setup keybinds
        KeyMapping.Category CATEGORY = new KeyMapping.Category(
                Identifier.fromNamespaceAndPath(Mwonmod.MOD_ID, "keybinds")
        );

        bankKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.mwonmod.bank", // The translation key of the keybinding's name
                InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_UNKNOWN, // The keycode of the key
                CATEGORY // The translation key of the keybinding's category.
        ));
        forgeKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.mwonmod.forge", // The translation key of the keybinding's name
                InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_UNKNOWN, // The keycode of the key
                CATEGORY // The translation key of the keybinding's category.
        ));

        ClientTickEvents.END_CLIENT_TICK.register(KeyBindings::onKey);
    }
    public static void onKey(Minecraft client) {

         if (!Mwonmod.onMelonKing()) return;

        while (bankKeyBinding.consumeClick()) {
            assert client.player != null;
            if (!(client.level == null)) {
                // client.player.sendMessage(Text.literal("Bank Opened"), false);
                client.execute(() -> client.player.connection.sendChat("@bank"));
            }
        }
        while (forgeKeyBinding.consumeClick()) {
            assert client.player != null;
            if (!(client.level == null)) {
                client.player.displayClientMessage(Component.literal("Forge Opened"), false);
                client.execute(() -> client.player.connection.sendChat("@forge"));
            }
        }
    }

}
