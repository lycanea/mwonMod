package dev.lycanea.mwonmod.client.mixin;

import dev.lycanea.mwonmod.client.GameState;
import dev.lycanea.mwonmod.client.MwonmodClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(InGameHud.class)
public class ActionBarMixin {
    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    private void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        Matcher MelonKingActionbarMatcher = Pattern.compile("Trophies: (?<trophy>\\d*) Karma: (?<karma>\\d*) Medals: (?<medals>\\d*)").matcher(message.getString());
        if (MwonmodClient.onMelonKing() && MelonKingActionbarMatcher.find()) {
            GameState.trophies = Integer.valueOf(MelonKingActionbarMatcher.group("trophy"));
            GameState.karma = Integer.valueOf(MelonKingActionbarMatcher.group("karma"));
            GameState.medals = Integer.valueOf(MelonKingActionbarMatcher.group("medals"));
        }
    }
}