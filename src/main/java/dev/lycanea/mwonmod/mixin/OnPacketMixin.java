package dev.lycanea.mwonmod.mixin;

import dev.dfonline.flint.util.Toaster;
import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.GameState;

import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ClientConnection.class)
public class OnPacketMixin {
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void onGameMessage(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        if (packet instanceof GameMessageS2CPacket(Text content, boolean overlay)) {
            handleChatPacket(content, overlay, ci);
        }
        if (packet instanceof OpenScreenS2CPacket openScreenPacket) {
            if (Mwonmod.onMelonKing()) {
                Text screenTitle = openScreenPacket.getName();
                Pattern bankPattern = Pattern.compile("Portable Bank \\| (\\d+)/10000 gold");
                Matcher bankMatcher = bankPattern.matcher(screenTitle.getString());

                if (bankMatcher.find()) {
                    GameState.portable_bank = Integer.parseInt(bankMatcher.group(1));
                }
            }
        }
    }

    @Unique
    private static void handleChatPacket(Text content, boolean overlay, CallbackInfo ci) {
        String message = content.getString();

        Pattern melonJoinPattern = Pattern.compile("^» Joined game: < Melon King > \\((\\d(?:\\.\\d*)*|BETA)\\) by DeepSeaBlue\\.$");
        Matcher melonJoinMatcher = melonJoinPattern.matcher(message);

        if (melonJoinMatcher.find()) {
            String version = '(' + FabricLoader.getInstance().getModContainer("mwonmod").get().getMetadata().getVersion().getFriendlyString() + ')';
            Component versionComponent = Component.text(version, Style.style()
                    .color(TextColor.color(128, 128, 128)) // Gray color for version
                    .decoration(TextDecoration.ITALIC, true) // Optional: to make it italicized
                    .build());

            Style mainStyle = Style.style().color(TextColor.color(203, 64, 22)).build();

            Toaster.toast(
                    Component.text("MwonMod"),
                    Component.text("Using Alpha version ", mainStyle)
                            .append(versionComponent)
                            .append(Component.text(", please report bugs on the Discord!", mainStyle))
            );

            GameState.melonJoin = LocalDateTime.now();
        }

        if (!Mwonmod.onMelonKing()) return;
        if (Config.HANDLER.instance().hideSellFailMessage && Objects.equals(message, "> You don't have any Super Enchanted Melons. Get them by cooking four Enchanted Melon Slices, which are gotten by cooking four Melon Slices.")) {
            ci.cancel();
        }
        if (Config.HANDLER.instance().what && Objects.equals(message, "> What?")) {
            Mwonmod.notification("> What?", "> What?");
        }
        Pattern auctionPattern = Pattern.compile("^>\\s*(?:First up,|And next,|Next,|And now,|And lastly,|Now,|Up next,)?\\s*(?:a|an|some)\\s+(.+?)!$");
        Pattern newKingPattern = Pattern.compile("^>\\s*([\\w]+)\\s+is\\s+the\\s+new\\s+(?:king|queen|monarch)!$");

        Matcher auctionMatcher = auctionPattern.matcher(message);
        Matcher kingMatcher = newKingPattern.matcher(message);

        if (auctionMatcher.find()) {
            assert MinecraftClient.getInstance().player != null;
            if (Config.HANDLER.instance().debugMode)
                MinecraftClient.getInstance().player.sendMessage(Text.literal("Auction Item: " + auctionMatcher.group(1)), false);
            try {
                var itemKey = auctionMatcher.group(1).toLowerCase().replaceAll(" ", "_");
                var itemElement = Mwonmod.itemData.get(itemKey);
                if (itemElement == null) {
                    if (Config.HANDLER.instance().debugMode)
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("Item data not found for: " + itemKey), false);
                    return;
                }
                JsonObject itemData = itemElement.getAsJsonObject();
                MinecraftClient.getInstance().player.sendMessage(Text.literal("Auction Item: " + itemData.get("name").getAsString()).styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(itemData.get("description").getAsString())))), false);
            } catch (Exception e) {
                if (Config.HANDLER.instance().debugMode)
                    MinecraftClient.getInstance().player.sendMessage(Text.literal("Error accessing item data: " + e.getMessage()), false);
            }
        } else if (kingMatcher.find()) {
            assert MinecraftClient.getInstance().player != null;
            if (Config.HANDLER.instance().kingChangeNotification) {
                GameState.currentMonarch = kingMatcher.group(1);
                Mwonmod.notification("New Monarch", kingMatcher.group(1));
            }
        }
    }
}
