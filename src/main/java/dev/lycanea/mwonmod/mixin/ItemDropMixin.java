package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.Mwonmod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class ItemDropMixin {
    @Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
    public void dropSelectedItem(LocalPlayer player, boolean all, CallbackInfo ci) {
        if (!Mwonmod.onMelonKing()) return;

        if (player == null) return;

        ItemStack handItem = player.getMainHandItem();
        handItem.getTooltipLines(Item.TooltipContext.EMPTY, player, TooltipFlag.NORMAL)
                .stream()
                .map(Component::getString)
                .forEach(str -> {
                    if (Config.HANDLER.instance().preventDroppingReflectives && "Reflection".equals(str)) {
                        player.makeSound(SoundEvents.SHIELD_BLOCK.value());
                        ci.cancel();
                    }
                });
    }
}
