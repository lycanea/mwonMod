package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.GameState;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ClientPlayNetworkHandler.class)
public class OnChatMixin {
    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        String message = packet.content().getString();

        Pattern melonJoinPattern = Pattern.compile("^» Joined game: < Melon King > \\(\\d(\\.\\d*)*\\) by DeepSeaBlue\\.$");
        Matcher melonJoinMatcher = melonJoinPattern.matcher(message);

        if (melonJoinMatcher.find()) {
            GameState.melonJoin = LocalDateTime.now();
        }

        if (!Mwonmod.onMelonKing()) return;
        if (Config.HANDLER.instance().what && Objects.equals(message, "> What?")) {
            Mwonmod.notification("> What?", "> What?");
        }
        Pattern auctionPattern = Pattern.compile("^>\\s*(?:First up,|And next,|Next,|And now,|And lastly,|Now,|Up next,)?\\s*(?:a|an|some)\\s+(.+?)!$");
        Pattern newKingPattern = Pattern.compile("^>\\s*([\\w]+)\\s+is\\s+the\\s+new\\s+(?:king|queen|monarch)!$");

        Matcher auctionMatcher = auctionPattern.matcher(message);
        Matcher kingMatcher = newKingPattern.matcher(message);

        if (auctionMatcher.find()) {
            assert MinecraftClient.getInstance().player != null;
            if (Config.HANDLER.instance().debugMode)
                MinecraftClient.getInstance().player.sendMessage(Text.literal("Auction Item: " + auctionMatcher.group(1)), false);
            try {
                var itemKey = auctionMatcher.group(1).toLowerCase().replaceAll(" ", "_");
                var itemElement = Mwonmod.itemData.get(itemKey);
                if (itemElement == null) {
                    if (Config.HANDLER.instance().debugMode)
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("Item data not found for: " + itemKey), false);
                    return;
                }
                JsonObject itemData = itemElement.getAsJsonObject();
                MinecraftClient.getInstance().player.sendMessage(Text.literal("Auction Item: " + itemData.get("name").getAsString()).styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(itemData.get("description").getAsString())))), false);
            } catch (Exception e) {
                if (Config.HANDLER.instance().debugMode)
                    MinecraftClient.getInstance().player.sendMessage(Text.literal("Error accessing item data: " + e.getMessage()), false);
            }
        } else if (kingMatcher.find()) {
            assert MinecraftClient.getInstance().player != null;
            if (Config.HANDLER.instance().kingChangeNotification) {
                GameState.currentMonarch = kingMatcher.group(1);
                Mwonmod.notification("New Monarch", kingMatcher.group(1));
            }
        }
    }
}
