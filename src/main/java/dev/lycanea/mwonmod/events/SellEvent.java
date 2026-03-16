package dev.lycanea.mwonmod.events;

import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.Config;

import net.kyori.adventure.title.Title;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class SellEvent {

    static List<String> merchants = List.of(
            "empty[siblings=[empty[style={color=white}], literal{Merchant}[style={color=green}]]]",
            "empty[siblings=[empty[style={color=white}], literal{Salesman}[style={color=red}]]]",
            "empty[siblings=[empty[style={color=white}], literal{Salesman}[style={color=dark_aqua}]]]",
            "empty[siblings=[empty[style={color=white}], literal{Salesman}[style={color=gray}]]]");

    public static InteractionResult entityInteract(Player playerEntity, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        if (!Mwonmod.onMelonKing() || Minecraft.getInstance().player == null) return InteractionResult.PASS;

//        if (Config.HANDLER.instance().debugMode) Mwonmod.LOGGER.info(String.valueOf(entity.getCustomName()));
        if (Config.HANDLER.instance().preventFullInventorySelling && merchants.contains(String.valueOf(entity.getCustomName()))) {
            if (Mwonmod.scanInventory(playerEntity, List.of()).emptySlots() <= Config.HANDLER.instance().fullInvEmptySlots) {
                Minecraft.getInstance().player.makeSound(SoundEvents.SHULKER_HURT_CLOSED);
                Minecraft.getInstance().player.showTitle(Title.title(net.kyori.adventure.text.Component.text(""), net.kyori.adventure.text.Component.text("Inventory Full"), Title.Times.times(Duration.ZERO,Duration.of(500, ChronoUnit.MILLIS),Duration.ZERO)));
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }
}
