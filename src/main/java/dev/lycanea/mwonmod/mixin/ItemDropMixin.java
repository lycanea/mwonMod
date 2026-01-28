package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.Mwonmod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class ItemDropMixin {
    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    public void dropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (Mwonmod.onMelonKing()) {
            LocalPlayer player = Minecraft.getInstance().player;

            assert player != null;
            ItemStack handItem = player.getMainHandItem();
            handItem.getTooltipLines(Item.TooltipContext.EMPTY, player, TooltipFlag.NORMAL)
                    .stream()
                    .map(Component::getString)
                    .forEach(str -> {
                        if (Config.HANDLER.instance().preventDroppingReflectives && "Reflection".equals(str)) {
                            player.makeSound(SoundEvents.SHIELD_BLOCK.value());
                            cir.setReturnValue(false);
                            cir.cancel();
                        }
                    });
        }
    }
}
