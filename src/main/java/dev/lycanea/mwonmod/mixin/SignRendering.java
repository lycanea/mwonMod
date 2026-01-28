package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.GameState;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.NumberFormat;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;

import static dev.lycanea.mwonmod.util.region.RegionLoader.beta_plot_origin;
import static dev.lycanea.mwonmod.util.region.RegionLoader.plot_origin;

@Mixin(AbstractSignRenderer.class)
public class SignRendering {

    @Unique
    NumberFormat formatter = NumberFormat.getIntegerInstance();

    @Inject(method = "extractRenderState*", at = @At("HEAD"), cancellable = true)
    private void onRender(SignBlockEntity signBlockEntity, SignRenderState signRenderState, float f, Vec3 vec3, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay, CallbackInfo ci) {

        if (!Config.HANDLER.instance().bankSignImprovements) return;
        if (!Mwonmod.onMelonKing()) return;
        int pos = signRenderState.blockPos.getX() - plot_origin.x;
        if (GameState.beta_plot) {
            pos = signRenderState.blockPos.getX() - beta_plot_origin.x;
        }
        if (pos < 0) {
            if (Config.HANDLER.instance().codespaceHider) ci.cancel();
            return;
        }

        signBlockEntity.updateText(signText -> {
            if (signText.getMessage(3, true).getString().startsWith("Banked: ")) {
                String prefix = "Banked: ";
                String numberPart = signText.getMessage(3, true).getString().substring(prefix.length()).trim();
                if (numberPart.matches("\\d+")) {
                    try {
                        long value = Long.parseLong(numberPart);
                        if (GameState.housing_pos != null && signRenderState.blockPos.closerToCenterThan(GameState.housing_pos, 5) && signText.getMessage(0, true).getString().startsWith("Gold")) {
                            GameState.personal_bank = Math.toIntExact(value);
                        }
                        return signText.setMessage(3, Component.nullToEmpty(formatter.format(value)));
                    } catch (NumberFormatException e) {
                        return signText;
                    }
                }
            }
            return signText;
        }, true);
    }
}
