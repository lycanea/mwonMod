package dev.lycanea.mwonmod.events;

import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.Config;

import dev.lycanea.mwonmod.util.GameState;
import dev.lycanea.mwonmod.util.ItemUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class AttackEvent {
    public static InteractionResult entityAttack(Player playerEntity, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        if (!Mwonmod.onMelonKing()) return InteractionResult.PASS;
        if (!Config.HANDLER.instance().preventAttackingWithHoe) return InteractionResult.PASS;
        if (!Objects.equals(GameState.playerLocation, "main_farm")) return InteractionResult.PASS;
        String itemId = ItemUtils.getItemID(playerEntity.getMainHandItem());
        if (Objects.equals(itemId, "hoe") || Objects.equals(itemId, "perfect_hoe") || Objects.equals(itemId, "divine_hoe") || Objects.equals(itemId, "royal_scythe")) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}
