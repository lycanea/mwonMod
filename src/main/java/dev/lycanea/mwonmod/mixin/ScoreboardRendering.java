package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.GameState;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class ScoreboardRendering {
    @Unique
    java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance();
    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"))
    private void onRenderScoreboard(DrawContext drawContext, ScoreboardObjective objective, CallbackInfo ci) {
        if (!Mwonmod.onMelonKing()) return;
        for (ScoreboardEntry entry : objective.getScoreboard().getScoreboardEntries(objective)) {
            Team team = objective.getScoreboard().getScoreHolderTeam(entry.owner());
            String display = entry.owner();
            if (team == null) continue;
            if (team.getPrefix().getString().startsWith("Coins: ")) {
                float value = Float.parseFloat(team.getPrefix().getString().substring(7));
                GameState.coins = value;
                if (Config.HANDLER.instance().scoreboardImprovements) {
                    objective.getScoreboard().removeScore(ScoreHolder.fromName(display), objective);
                    objective.getScoreboard().getOrCreateScore(ScoreHolder.fromName("§7Coins: §e" + formatter.format(value)), objective, true).setScore(entry.value());
                }
            }
            if (team.getPrefix().getString().startsWith("Bank Gold: ")) {
                float value = Float.parseFloat(team.getPrefix().getString().substring(11));
                GameState.bank_gold = value;
                if (Config.HANDLER.instance().scoreboardImprovements) {
                    objective.getScoreboard().removeScore(ScoreHolder.fromName(display), objective);
                    objective.getScoreboard().getOrCreateScore(ScoreHolder.fromName("§7Bank Gold: §6" + formatter.format(value)), objective, true).setScore(entry.value());
                }
            }
            if (team.getPrefix().getString().startsWith("Path: ")) {
                GameState.currentPath = team.getPrefix().getString().substring(6);
            }
            if (team.getPrefix().getString().startsWith("King: ")) {
                GameState.currentMonarch = team.getPrefix().getString().substring(6);
            }
            if (team.getPrefix().getString().startsWith("Monarch: ")) {
                GameState.currentMonarch = team.getPrefix().getString().substring(9);
            }
            if (team.getPrefix().getString().startsWith("Queen: ")) {
                GameState.currentMonarch = team.getPrefix().getString().substring(7);
            }
        }
//        objective.getScoreboard().getOrCreateScore(ScoreHolder.fromName("§fa§1a§6a§0a§2a§5a§3a§9a§9a§9a§9a"), objective,true).setScore(5);
    }
}
