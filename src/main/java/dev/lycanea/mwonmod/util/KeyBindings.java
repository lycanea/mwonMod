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
    private static KeyMapping vaultKeyBinding;
    private static KeyMapping pouchKeyBinding;

    private static final KeyMapping[] trinketKeyBindings = new KeyMapping[6];

    public static void setup() {
        // setup keybinds
        KeyMapping.Category MAIN_CATEGORY = new KeyMapping.Category(
                Identifier.fromNamespaceAndPath(Mwonmod.MOD_ID, "keybinds")
        );

        KeyMapping.Category TRINKET_DATABASE_CATEGORY = new KeyMapping.Category(
                Identifier.fromNamespaceAndPath(Mwonmod.MOD_ID, "trinketkeybinds")
        );

        bankKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.mwonmod.bank", // The translation key of the keybinding's name
                InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_UNKNOWN, // The keycode of the key
                MAIN_CATEGORY // The translation key of the keybinding's category.
        ));
        forgeKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.mwonmod.forge", // The translation key of the keybinding's name
                InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_UNKNOWN, // The keycode of the key
                MAIN_CATEGORY // The translation key of the keybinding's category.
        ));
        vaultKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.mwonmod.vault", // The translation key of the keybinding's name
                InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_UNKNOWN, // The keycode of the key
                MAIN_CATEGORY // The translation key of the keybinding's category.
        ));
        pouchKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.mwonmod.pouch", // The translation key of the keybinding's name
                InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_UNKNOWN, // The keycode of the key
                MAIN_CATEGORY // The translation key of the keybinding's category.
        ));

        for (int i = 1; i < 7; i++) {
            var trinketKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                    "key.mwonmod.trinket" + i, // The translation key of the keybinding's name
                    InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                    GLFW.GLFW_KEY_UNKNOWN, // The keycode of the key
                    TRINKET_DATABASE_CATEGORY // The translation key of the keybinding's category.
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
        while (vaultKeyBinding.consumeClick()) {
            client.execute(() -> client.player.connection.sendChat("@vault"));
        }
        while (pouchKeyBinding.consumeClick()) {
            client.execute(() -> client.player.connection.sendChat("@pouch"));
        } // im starting to want to softcode this

        for (int i = 0; i < trinketKeyBindings.length; i++) {
            var bind = trinketKeyBindings[i];
            while (bind.consumeClick()) {
                int finalI = i;
                client.execute(() -> client.player.connection.sendChat("@t " + (finalI+1)));
            }
        }
    }

}
