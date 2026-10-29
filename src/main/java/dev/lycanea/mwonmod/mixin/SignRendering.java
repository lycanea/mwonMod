package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.GameState;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.SignTextSlot;
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

    @Inject(method = "extractRenderState(Lnet/minecraft/world/level/block/entity/SignBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/SignRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V", at = @At("HEAD"), cancellable = true)
    private void onRender(SignBlockEntity blockEntity, SignRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress, CallbackInfo ci) {
        if (!Mwonmod.onMelonKing()) return;

        if (Config.HANDLER.instance().codespaceHider) {
            if (GameState.beta_plot) {
                if (blockEntity.getBlockPos().getX() < beta_plot_origin.x) {
                    ci.cancel();
                    return;
                }
            } else {
                if (blockEntity.getBlockPos().getX() < plot_origin.x) {
                    ci.cancel();
                    return;
                }
            }
        }

        blockEntity.updateText(signText -> {
            if (signText.getMessages(true).get(3).getString().startsWith("Banked: ")) {
                String prefix = "Banked: ";
                String numberPart = signText.getMessages(true).get(3).getString().substring(prefix.length()).trim();
                if (numberPart.matches("\\d+")) {
                    try {
                        long value = Long.parseLong(numberPart);
                        if (GameState.housing_pos != null && blockEntity.getBlockPos().closerToCenterThan(GameState.housing_pos, 5) && signText.getMessages(true).get(0).getString().startsWith("Gold")) {
                            GameState.personal_bank = Math.toIntExact(value);
                        }
                        if (Config.HANDLER.instance().bankSignImprovements) return signText.asMutable().setLine(3, Component.nullToEmpty(formatter.format(value))).asImmutable();
                    } catch (NumberFormatException e) {
                        return signText;
                    }
                }
            }
            return signText;
        }, SignTextSlot.FRONT);
    }
}
