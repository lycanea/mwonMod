package dev.lycanea.mwonmod.events;

import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.Config;

import dev.lycanea.mwonmod.util.GameState;
import dev.lycanea.mwonmod.util.ItemUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class AttackEvent {
    static List<String> hoes = List.of(
            "hoe",
            "perfect_hoe",
            "divine_hoe",
            "royal_scythe");

    public static InteractionResult entityAttack(Player playerEntity, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        if (!Mwonmod.onMelonKing()) return InteractionResult.PASS;
        if (Config.HANDLER.instance().preventAttackingWithHoe
                && Objects.equals(GameState.playerLocation, "main_farm")
                && !playerEntity.getMainHandItem().isEmpty()
                && hoes.contains(ItemUtils.getItemID(playerEntity.getMainHandItem()))) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}
