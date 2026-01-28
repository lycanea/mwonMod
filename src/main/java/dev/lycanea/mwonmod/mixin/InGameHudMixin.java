package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.GameState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;

@Mixin(Gui.class)
public class InGameHudMixin {
    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    private void onSetOverlayMessage(Component message, boolean tinted, CallbackInfo ci) {
        Matcher MelonKingActionbarMatcher = Pattern.compile("Trophies: (?<trophy>\\d*) Karma: (?<karma>\\d*) Medals: (?<medals>\\d*)").matcher(message.getString());
        if (Mwonmod.onMelonKing() && MelonKingActionbarMatcher.find()) {
            GameState.trophies = Integer.valueOf(MelonKingActionbarMatcher.group("trophy"));
            GameState.karma = Integer.valueOf(MelonKingActionbarMatcher.group("karma"));
            GameState.medals = Integer.valueOf(MelonKingActionbarMatcher.group("medals"));
        }
    }

    @Inject(method = "setTitle", at = @At("HEAD"))
    private void onSetTitle(Component message, CallbackInfo ci) {
//        Mwonmod.LOGGER.info("Title: {}", message.getString());
    }
}
