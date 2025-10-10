package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.Mwonmod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ItemDropMixin {
    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    public void dropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (Mwonmod.onMelonKing()) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;

            assert player != null;
            ItemStack handItem = player.getMainHandStack();
            handItem.getTooltip(Item.TooltipContext.DEFAULT, player, TooltipType.BASIC)
                    .stream()
                    .map(Text::getString)
                    .forEach(str -> {
                        if (Config.HANDLER.instance().preventDroppingReflectives && "Reflection".equals(str)) {
                            player.playSound(SoundEvents.ITEM_SHIELD_BLOCK.value());
                            cir.setReturnValue(false);
                            cir.cancel();
                        }
                    });
        }
    }
}
