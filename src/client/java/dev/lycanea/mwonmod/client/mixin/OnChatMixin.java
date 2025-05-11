package dev.lycanea.mwonmod.client.mixin;

import com.google.gson.JsonObject;
import dev.lycanea.mwonmod.client.MwonmodClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ClientPlayNetworkHandler.class)
public class OnChatMixin {
    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (!MwonmodClient.onMelonKing()) return;
        String message = packet.content().getString();
        Pattern pattern = Pattern.compile("^>\\s*(?:First up,|And next,|Next,|And now,|And lastly,|Now,|Up next,)?\\s*(?:a|an|some)\\s+(.+?)!$");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            assert MinecraftClient.getInstance().player != null;
            if (MwonmodClient.DEBUG) MinecraftClient.getInstance().player.sendMessage(Text.literal("Auction Item: " + matcher.group(1)), false);
            try {
                var itemKey = matcher.group(1).toLowerCase().replaceAll(" ", "_");
                var itemElement = MwonmodClient.itemData.get(itemKey);
                if (itemElement == null) {
                    if (MwonmodClient.DEBUG) MinecraftClient.getInstance().player.sendMessage(Text.literal("Item data not found for: " + itemKey), false);
                    return;
                }
                JsonObject itemData = itemElement.getAsJsonObject();
                MinecraftClient.getInstance().player.sendMessage(Text.literal("Auction Item: " + String.valueOf(itemData.get("name").getAsString())).styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(itemData.get("description").getAsString())))), false);
            } catch (Exception e) {
                if (MwonmodClient.DEBUG) MinecraftClient.getInstance().player.sendMessage(Text.literal("Error accessing item data: " + e.getMessage()), false);
            }
        }
    }
}