package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.GameState;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.AbstractSignBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.NumberFormat;

import static dev.lycanea.mwonmod.util.region.RegionLoader.beta_plot_origin;
import static dev.lycanea.mwonmod.util.region.RegionLoader.plot_origin;

@Mixin(AbstractSignBlockEntityRenderer.class)
public class SignRendering {

    @Unique
    NumberFormat formatter = NumberFormat.getIntegerInstance();

    @Inject(method = "render(Lnet/minecraft/block/entity/SignBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
    private void onRender(SignBlockEntity sign, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, Vec3d vec3d, CallbackInfo ci) {
        if (!Config.HANDLER.instance().bankSignImprovements) return;
        if (!Mwonmod.onMelonKing()) return;
        int pos = sign.getPos().getX() - plot_origin.x;
        if (GameState.beta_plot) {
            pos = sign.getPos().getX() - beta_plot_origin.x;
        }
        if (pos < 0) {
            if (Config.HANDLER.instance().codespaceHider) ci.cancel();
            return;
        }

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
