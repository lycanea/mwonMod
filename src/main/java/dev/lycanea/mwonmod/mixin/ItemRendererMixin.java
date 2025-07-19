package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.ItemUtils;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.render.item.ItemRenderer;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow protected abstract void renderItem(ItemStack stack, ModelTransformationMode transformationMode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, boolean useInventoryModel);

    @Shadow @Final private ItemModels models;

    @Shadow protected abstract BakedModel getModelOrOverride(BakedModel model, ItemStack stack, @Nullable World world, @Nullable LivingEntity entity, int seed);

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;ZF)V", at = @At("HEAD"), cancellable = true)
    public void onRenderItemHead(ItemStack stack, ModelTransformationMode transformationMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, boolean useInventoryModel, float z, CallbackInfo ci) {
        if (Mwonmod.onMelonKing()) {
            ci.cancel();
            ItemStack modifiedStack = ItemUtils.modifyItemForRendering(stack);
            matrices.push();
            model.getTransformation().getTransformation(transformationMode).apply(leftHanded, matrices);
            matrices.translate(-0.5F, -0.5F, z);
            this.renderItem(modifiedStack, transformationMode, matrices, vertexConsumers, light, overlay, model, useInventoryModel);
            matrices.pop();
        }
    }

    @Inject(method = "getModel(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)Lnet/minecraft/client/render/model/BakedModel;", at = @At("HEAD"), cancellable = true)
    public void getModelHead(ItemStack stack, World world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        if (Mwonmod.onMelonKing()) {
            cir.cancel();
            ItemStack modifiedStack = ItemUtils.modifyItemForRendering(stack);
            BakedModel bakedModel = this.models.getModel(modifiedStack);
            cir.setReturnValue(this.getModelOrOverride(bakedModel, modifiedStack, world, entity, seed));
        }
    }
}
