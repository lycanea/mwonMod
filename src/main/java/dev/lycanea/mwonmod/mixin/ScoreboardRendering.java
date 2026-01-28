package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.GameState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.scores.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class ScoreboardRendering {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Unique
    java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance();
    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"))
    private void onRenderScoreboard(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (this.minecraft.level == null || !Mwonmod.onMelonKing()) return;
        Scoreboard scoreboard =  this.minecraft.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) return;
        for (PlayerScoreEntry entry : scoreboard.listPlayerScores(objective)) {
            PlayerTeam team = scoreboard.getPlayersTeam(entry.owner());
            String display = entry.owner();
            if (team == null) continue;
            if (team.getPlayerPrefix().getString().startsWith("Coins: ")) {
                float value = Float.parseFloat(team.getPlayerPrefix().getString().substring(7));
                GameState.coins = value;
                if (Config.HANDLER.instance().scoreboardImprovements) {
                    scoreboard.resetSinglePlayerScore(ScoreHolder.forNameOnly(display), objective);
                    scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly("§7Coins: §e" + formatter.format(value)), objective, true).set(entry.value());
                }
            }
            if (team.getPlayerPrefix().getString().startsWith("Bank Gold: ")) {
                float value = Float.parseFloat(team.getPlayerPrefix().getString().substring(11));
                GameState.bank_gold = value;
                if (Config.HANDLER.instance().scoreboardImprovements) {
                    objective.getScoreboard().resetSinglePlayerScore(ScoreHolder.forNameOnly(display), objective);
                    objective.getScoreboard().getOrCreatePlayerScore(ScoreHolder.forNameOnly("§7Bank Gold: §6" + formatter.format(value)), objective, true).set(entry.value());
                }
            }
            if (team.getPlayerPrefix().getString().startsWith("Path: ")) {
                GameState.currentPath = team.getPlayerPrefix().getString().substring(6);
            }
            if (team.getPlayerPrefix().getString().startsWith("King: ")) {
                GameState.currentMonarch = team.getPlayerPrefix().getString().substring(6);
            }
            if (team.getPlayerPrefix().getString().startsWith("Monarch: ")) {
                GameState.currentMonarch = team.getPlayerPrefix().getString().substring(9);
            }
            if (team.getPlayerPrefix().getString().startsWith("Queen: ")) {
                GameState.currentMonarch = team.getPlayerPrefix().getString().substring(7);
            }
        }
//        objective.getScoreboard().getOrCreateScore(ScoreHolder.fromName("§fa§1a§6a§0a§2a§5a§3a§9a§9a§9a§9a"), objective,true).setScore(5);
    }
}
