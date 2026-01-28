package dev.lycanea.mwonmod.events;

import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.Config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import net.minecraft.client.Minecraft;
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
import java.util.Objects;

public class SellEvent {
    public static InteractionResult entityInteract(Player playerEntity, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        if (!Mwonmod.onMelonKing()) return InteractionResult.PASS;
        if (!Config.HANDLER.instance().preventFullInventorySelling) return InteractionResult.PASS;

//        if (Config.HANDLER.instance().debugMode) Mwonmod.LOGGER.info(String.valueOf(entity.getCustomName()));
        if (Objects.equals(String.valueOf(entity.getCustomName()), "empty[siblings=[empty[style={color=white}], literal{Merchant}[style={color=green}]]]") || Objects.equals(String.valueOf(entity.getCustomName()), "empty[siblings=[empty[style={color=white}], literal{Salesman}[style={color=red}]]]") || Objects.equals(String.valueOf(entity.getCustomName()), "empty[siblings=[empty[style={color=white}], literal{Salesman}[style={color=dark_aqua}]]]") || Objects.equals(String.valueOf(entity.getCustomName()), "empty[siblings=[empty[style={color=white}], literal{Salesman}[style={color=gray}]]]")) {
            if (Mwonmod.scanInventory(playerEntity, List.of()).emptySlots() <= Config.HANDLER.instance().fullInvEmptySlots) {
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.playSound(Sound.sound(Key.key("minecraft:entity.shulker.hurt_closed"), Sound.Source.MASTER, 1.0F, 1.0F));
                Minecraft.getInstance().player.showTitle(Title.title(net.kyori.adventure.text.Component.text(""), net.kyori.adventure.text.Component.text("Inventory Full"), Title.Times.times(Duration.ZERO,Duration.of(500, ChronoUnit.MILLIS),Duration.ZERO)));
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }
}
