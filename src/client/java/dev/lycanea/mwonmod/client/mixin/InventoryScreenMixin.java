package dev.lycanea.mwonmod.client.mixin;


import dev.lycanea.mwonmod.client.MwonmodClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen<PlayerScreenHandler> {

    @Unique
    private ButtonWidget inventoryRundownButton;

    // Constructor stub matching the target
    protected InventoryScreenMixin(PlayerScreenHandler handler,
                                   net.minecraft.entity.player.PlayerInventory inventory,
                                   net.minecraft.text.Text title) {
        super(handler, inventory, title);
    }

    @Unique
    private void buttonPress(ButtonWidget btn) {
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.sendMessage(Text.literal("Toggled Inventory Rundown"), false);
        MwonmodClient.inventory_rundown = !MwonmodClient.inventory_rundown;
        btn.setMessage(Text.literal(MwonmodClient.inventory_rundown ? "ON" : "OFF"));
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        // Place your button relative to this.x, this.y:
        inventoryRundownButton = ButtonWidget.builder(
                Text.literal(MwonmodClient.inventory_rundown ? "ON" : "OFF"),
                this::buttonPress
        ).dimensions(0, 0, 30, 20).build();

        this.addDrawableChild(inventoryRundownButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (inventoryRundownButton != null) {
            if (!MwonmodClient.onMelonKing()) {
                inventoryRundownButton.visible = false;
                return;
            }
            int buttonX = this.x;
            int buttonY = this.y + this.backgroundHeight + 5;

            inventoryRundownButton.setPosition(buttonX, buttonY);
            inventoryRundownButton.visible = true;
        }
    }
}