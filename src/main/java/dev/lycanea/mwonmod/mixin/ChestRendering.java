package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.GameState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.lycanea.mwonmod.util.region.RegionLoader.beta_plot_origin;
import static dev.lycanea.mwonmod.util.region.RegionLoader.plot_origin;

import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(ChestRenderer.class)
public class ChestRendering<T extends BlockEntity & LidBlockEntity> {

    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void onRender(ChestRenderState chestRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (!Mwonmod.onMelonKing()) return;
        if (!Config.HANDLER.instance().codespaceHider) return;
        int pos = chestRenderState.blockPos.getX() - plot_origin.x;
        if (GameState.beta_plot) {
            pos = chestRenderState.blockPos.getX() - beta_plot_origin.x;
        }
        if (pos < 0) {
            ci.cancel();
        }
    }
}
