package dev.lycanea.mwonmod.client.mixin;

import com.google.gson.JsonElement;
import dev.lycanea.mwonmod.client.MwonmodClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
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
        if (!MwonmodClient.DEBUG) return;
        String message = packet.content().getString();
        Pattern pattern = Pattern.compile("^>\\s*(?:First up,|And next,|Next,|And now,|And lastly,|Now,|Up Next,)?\\s*(?:a|an|some)\\s+(.+?)!$");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            assert MinecraftClient.getInstance().player != null;
            MinecraftClient.getInstance().player.sendMessage(Text.literal("Auction Item: " + matcher.group(1)), false);
            JsonElement itemData = MwonmodClient.itemData.get(matcher.group(1).toLowerCase().replaceAll(" ", "_"));
            MinecraftClient.getInstance().player.sendMessage(Text.literal(String.valueOf(itemData.getAsJsonObject().get("description"))), false);
        }
    }
}