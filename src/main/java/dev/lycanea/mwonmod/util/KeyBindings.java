package dev.lycanea.mwonmod.util;

import dev.lycanea.mwonmod.Mwonmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.player.PlayerEntity;


public class KeyBindings {

    private static KeyBinding bankKeyBinding;
    private static KeyBinding forgeKeyBinding;
    private static KeyBinding profileKeyBinding;

    public static void setup() {
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
        profileKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mwonmod.profile", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_UNKNOWN, // The keycode of the key
                "lycanea.mwonmod.keybinds" // The translation key of the keybinding's category.
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> { onKey(client); });
    }
    public static void onKey(MinecraftClient client) {

        // if (!Mwonmod.onMelonKing()) return;

        while (bankKeyBinding.wasPressed()) {
            assert client.player != null;
            if (!(client.world == null) && Mwonmod.onMelonKing()) {
                // client.player.sendMessage(Text.literal("Bank Opened"), false);
                client.execute(() -> client.player.networkHandler.sendChatMessage("@bank"));
            }
        }
        while (forgeKeyBinding.wasPressed()) {
            assert client.player != null;
            if (!(client.world == null) && Mwonmod.onMelonKing()) {
                client.player.sendMessage(Text.literal("Forge Opened"), false);
                client.execute(() -> client.player.networkHandler.sendChatMessage("@forge"));
            }
        }
        while (profileKeyBinding.wasPressed()) {
            assert client.player != null;
            if (!(client.world == null)) {
                var targetedEntity = client.crosshairTarget != null && client.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.ENTITY ?
                        ((net.minecraft.util.hit.EntityHitResult) client.crosshairTarget).getEntity() : null;
                if (targetedEntity instanceof PlayerEntity) {
                    client.execute(() -> client.player.networkHandler.sendCommand("profile " + targetedEntity.getName().getString()));
                }
            }
        }
    }

}
