package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.GameState;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.NumberFormat;

@Mixin(SignBlockEntityRenderer.class)
public class SignRendering {

    @Inject(method = "render*", at = @At("HEAD"))
    private void onRender(SignBlockEntity sign, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        NumberFormat formatter = NumberFormat.getIntegerInstance();

        if (!Config.HANDLER.instance().bankSignImprovements) return;
        if (!Mwonmod.onMelonKing()) return;

        sign.changeText(signText -> {
            if (signText.getMessage(3, true).getString().startsWith("Banked: ")) {
                String prefix = "Banked: ";
                String numberPart = signText.getMessage(3, true).getString().substring(prefix.length()).trim();
                if (numberPart.matches("\\d+")) {
                    try {
                        long value = Long.parseLong(numberPart);
                        if (GameState.housing_pos != null && sign.getPos().isWithinDistance(GameState.housing_pos, 5) && signText.getMessage(0, true).getString().startsWith("Gold")) {
                            GameState.personal_bank = Math.toIntExact(value);
                        }
                        return signText.withMessage(3, Text.of(formatter.format(value)));
                    } catch (NumberFormatException e) {
                        return signText;
                    }
                }
            }
            return signText;
        }, true);
    }
}
