package dev.lycanea.mwonmod.client.mixin;

import dev.lycanea.mwonmod.client.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class ScoreboardRendering {
    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"))
    private void onRenderScoreboard(DrawContext drawContext, ScoreboardObjective objective, CallbackInfo ci) {
        if (!Config.HANDLER.instance().scoreboardImprovements) return;
        java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance();
        for (ScoreboardEntry entry : objective.getScoreboard().getScoreboardEntries(objective)) {
            Team team = objective.getScoreboard().getScoreHolderTeam(entry.owner());
            String display = entry.owner();
            if (team == null) continue;
            if (team.getPrefix().getString().startsWith("Coins: ")) {
                objective.getScoreboard().removeScore(ScoreHolder.fromName(display), objective);
                float value = Float.parseFloat(team.getPrefix().getString().substring(7));
                objective.getScoreboard().getOrCreateScore(ScoreHolder.fromName("§7Coins: §e" + formatter.format(value)), objective,true).setScore(entry.value());
            }
            if (team.getPrefix().getString().startsWith("Bank Gold: ")) {
                objective.getScoreboard().removeScore(ScoreHolder.fromName(display), objective);
                float value = Float.parseFloat(team.getPrefix().getString().substring(11));
                objective.getScoreboard().getOrCreateScore(ScoreHolder.fromName("§7Bank Gold: §e" + formatter.format(value)), objective,true).setScore(entry.value());
            }
        }
//        objective.getScoreboard().getOrCreateScore(ScoreHolder.fromName("§fa§1a§6a§0a§2a§5a§3a§9a§9a§9a§9a"), objective,true).setScore(5);
    }
}
