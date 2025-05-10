package dev.lycanea.mwonmod.client.mixin;

import dev.lycanea.mwonmod.client.MwonmodClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ClientPlayNetworkHandler.class)
public class OnChatMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("mwonmod");

    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (!MwonmodClient.onMelonKing()) return;
        String message = packet.content().getString();
        Pattern pattern = Pattern.compile("^>\\s*(?:First up,|And next,|Next,|And now,|And lastly,|Now,)?\\s*(?:a|an|some)\\s+(.+?)!$");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            LOGGER.info("Auction Item: {}", matcher.group(1));
        }
    }
}