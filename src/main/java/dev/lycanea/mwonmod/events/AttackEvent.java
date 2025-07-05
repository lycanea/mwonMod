package dev.lycanea.mwonmod.events;

import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.Config;

import dev.lycanea.mwonmod.util.GameState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AttackEvent {
    public static ActionResult entityAttack(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        if (!Mwonmod.onMelonKing()) return ActionResult.PASS;
        if (!Config.HANDLER.instance().preventAttackingWithHoe) return ActionResult.PASS;
        if (!Objects.equals(GameState.playerLocation, "main_farm")) return ActionResult.PASS;
        if (Objects.equals(playerEntity.getInventory().getMainHandStack().getItem().getName().getString(), "Wooden Hoe")) {
            return ActionResult.FAIL;
        }
        if (Objects.equals(playerEntity.getInventory().getMainHandStack().getItem().getName().getString(), "Golden Hoe")) {
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }
}
