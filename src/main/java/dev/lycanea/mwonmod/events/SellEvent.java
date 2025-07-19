package dev.lycanea.mwonmod.events;

import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.Config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public class SellEvent {
    public static ActionResult entityInteract(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        if (!Mwonmod.onMelonKing()) return ActionResult.PASS;
        if (!Config.HANDLER.instance().preventFullInventorySelling) return ActionResult.PASS;

//        if (Config.HANDLER.instance().debugMode) Mwonmod.LOGGER.info(String.valueOf(entity.getCustomName()));
        if (Objects.equals(String.valueOf(entity.getCustomName()), "empty[siblings=[empty[style={color=white}], literal{Merchant}[style={color=green}]]]") || Objects.equals(String.valueOf(entity.getCustomName()), "empty[siblings=[empty[style={color=white}], literal{Salesman}[style={color=red}]]]") || Objects.equals(String.valueOf(entity.getCustomName()), "empty[siblings=[empty[style={color=white}], literal{Salesman}[style={color=dark_aqua}]]]") || Objects.equals(String.valueOf(entity.getCustomName()), "empty[siblings=[empty[style={color=white}], literal{Salesman}[style={color=gray}]]]")) {
            if (Mwonmod.scanInventory(playerEntity, List.of()).emptySlots() <= Config.HANDLER.instance().fullInvEmptySlots) {
                assert MinecraftClient.getInstance().player != null;
                MinecraftClient.getInstance().player.playSound(Sound.sound(Key.key("minecraft:entity.shulker.hurt_closed"), Sound.Source.MASTER, 1.0F, 1.0F));
                MinecraftClient.getInstance().player.showTitle(Title.title(net.kyori.adventure.text.Component.text(""), net.kyori.adventure.text.Component.text("Inventory Full"), Title.Times.times(Duration.ZERO,Duration.of(500, ChronoUnit.MILLIS),Duration.ZERO)));
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }
}
