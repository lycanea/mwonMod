package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.GameState;

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
        if (Mwonmod.onMelonKing() && MelonKingActionbarMatcher.find()) {
            GameState.trophies = Integer.valueOf(MelonKingActionbarMatcher.group("trophy"));
            GameState.karma = Integer.valueOf(MelonKingActionbarMatcher.group("karma"));
            GameState.medals = Integer.valueOf(MelonKingActionbarMatcher.group("medals"));
        }
    }
}
