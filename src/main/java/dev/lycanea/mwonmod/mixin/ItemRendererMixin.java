package dev.lycanea.mwonmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    public static void renderItem(ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, int[] is, List<BakedQuad> list, RenderType renderType, ItemStackRenderState.FoilType foilType) {
    }

    @Inject(method = "renderItem(Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II[ILjava/util/List;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V", at = @At("HEAD"), cancellable = true)
    private static void onRenderItemHead(ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, int[] is, List<BakedQuad> list, RenderType renderType, ItemStackRenderState.FoilType foilType, CallbackInfo ci) {
//        if (Mwonmod.onMelonKing()) {
//            ci.cancel();
//            ItemStack modifiedStack = ItemUtils.modifyItemForRendering(stack);
//            this.renderItem((LivingEntity)null, modifiedStack, displayContext, matrices, vertexConsumers, world, light, overlay, seed);
//        }
    }
}
