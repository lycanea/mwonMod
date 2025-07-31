package dev.lycanea.mwonmod.events;

import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.Config;

import dev.lycanea.mwonmod.util.GameState;
import dev.lycanea.mwonmod.util.ItemUtils;
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
        String itemId = ItemUtils.getItemID(playerEntity.getMainHandStack());
        if (Objects.equals(itemId, "hoe") || Objects.equals(itemId, "perfect_hoe") || Objects.equals(itemId, "divine_hoe") || Objects.equals(itemId, "royal_scythe")) {
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }
}
