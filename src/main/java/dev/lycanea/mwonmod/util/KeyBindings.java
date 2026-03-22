package dev.lycanea.mwonmod.util;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lycanea.mwonmod.Mwonmod;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;


public class KeyBindings {

    private static KeyMapping bankKeyBinding;
    private static KeyMapping forgeKeyBinding;

    private static final KeyMapping[] trinketKeyBindings = new KeyMapping[5];

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

        for (int i = 1; i < 6; i++) {
            var trinketKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                    "key.mwonmod.trinket" + i, // The translation key of the keybinding's name
                    InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                    GLFW.GLFW_KEY_UNKNOWN, // The keycode of the key
                    CATEGORY // The translation key of the keybinding's category.
            ));
            trinketKeyBindings[i-1] = trinketKeybind;
        }

        ClientTickEvents.END_CLIENT_TICK.register(KeyBindings::onKey);
    }
    public static void onKey(Minecraft client) {
        if (!Mwonmod.onMelonKing() || client.level == null || client.player == null) return;

        while (bankKeyBinding.consumeClick()) {
            client.execute(() -> client.player.connection.sendChat("@bank"));
        }
        while (forgeKeyBinding.consumeClick()) {
            client.execute(() -> client.player.connection.sendChat("@forge"));
        }

        for (int i = 0; i < trinketKeyBindings.length; i++) {
            var bind = trinketKeyBindings[i];
            while (bind.consumeClick()) {
                int finalI = i;
                client.execute(() -> client.player.connection.sendChat("@t " + finalI +1));
            }
        }
    }

}
