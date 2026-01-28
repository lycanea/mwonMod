package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Mwonmod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {

    @Unique
    private Button inventoryRundownButton;

    // Constructor stub matching the target
    protected InventoryScreenMixin(InventoryMenu handler,
                                   net.minecraft.world.entity.player.Inventory inventory,
                                   net.minecraft.network.chat.Component title) {
        super(handler, inventory, title);
    }

    @Unique
    private void buttonPress(Button btn) {
        assert Minecraft.getInstance().player != null;
        Minecraft.getInstance().player.displayClientMessage(Component.literal("Toggled Inventory Rundown"), false);
        Mwonmod.inventory_rundown = !Mwonmod.inventory_rundown;
        btn.setMessage(Component.literal(Mwonmod.inventory_rundown ? "ON" : "OFF"));
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        // Place your button relative to this.x, this.y:
        inventoryRundownButton = Button.builder(
                Component.literal(Mwonmod.inventory_rundown ? "ON" : "OFF"),
                this::buttonPress
        ).bounds(0, 0, 30, 20).build();

        this.addRenderableWidget(inventoryRundownButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (inventoryRundownButton != null) {
            if (!Mwonmod.onMelonKing()) {
                inventoryRundownButton.visible = false;
                return;
            }
            int buttonX = this.leftPos;
            int buttonY = this.topPos + this.imageHeight + 5;

            inventoryRundownButton.setPosition(buttonX, buttonY);
            inventoryRundownButton.visible = true;
        }
    }
}
