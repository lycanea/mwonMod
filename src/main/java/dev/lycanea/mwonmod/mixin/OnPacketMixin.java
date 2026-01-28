package dev.lycanea.mwonmod.mixin;

import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.BossState;
import dev.lycanea.mwonmod.util.GameState;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(Connection.class)
public class OnPacketMixin {
    @Inject(method = "genericsFtw", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void onGameMessage(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        if (packet instanceof ClientboundSystemChatPacket(Component content, boolean overlay)) {
            handleChatPacket(content, overlay, ci);
        }
        if (packet instanceof ClientboundSoundPacket PlaySoundS2CPacket && Mwonmod.onMelonKing()) {
            if (Objects.equals(PlaySoundS2CPacket.getSound().getRegisteredName(), "minecraft:entity.sheep.shear") && Config.HANDLER.instance().muteSellingSound) {
                ci.cancel();
            }
        }
        if (packet instanceof ClientboundOpenScreenPacket openScreenPacket) {
            if (Mwonmod.onMelonKing()) {
                net.minecraft.network.chat.Component screenTitle = openScreenPacket.getTitle();
                Pattern bankPattern = Pattern.compile("Portable Bank \\| (\\d+)/10000 gold");
                Matcher bankMatcher = bankPattern.matcher(screenTitle.getString());

                if (bankMatcher.find()) {
                    GameState.portable_bank = Integer.parseInt(bankMatcher.group(1));
                }
            }
        }
    }

    @Unique
    private static void handleChatPacket(Component content, boolean overlay, CallbackInfo ci) {
        String message = content.getString();

        Pattern melonJoinPattern = Pattern.compile("^» Joined game: < Melon King > \\((\\d(?:\\.\\d*)*|BETA)\\) by DeepSeaBlue\\.$");
        Matcher melonJoinMatcher = melonJoinPattern.matcher(message);

        if (melonJoinMatcher.find()) {
//            String version = '(' + FabricLoader.getInstance().getModContainer("mwonmod").get().getMetadata().getVersion().getFriendlyString() + ')';
//            Component versionComponent = Component.text(version, Style.style()
//                    .color(TextColor.color(128, 128, 128)) // Gray color for version
//                    .decoration(TextDecoration.ITALIC, true) // Optional: to make it italicized
//                    .build());
//
//            Style mainStyle = Style.EMPTY.withColor(TextColor.fromRgb(1263));
//
//            Toaster.toast(
//                    Component.literal("MwonMod"),
//                    Component.literal("Version: ").setStyle(mainStyle)
//                            .append(versionComponent)
//                            .append(Component.literal(", bugs may occur!").setStyle(mainStyle))
//            );

            GameState.melonJoin = LocalDateTime.now();
        }

        if (!Mwonmod.onMelonKing()) return;
        if (message.equals("» All combatants perished, so the battle was lost.")) {
            BossState.updateBoss(null);
        }
        if (BossState.dialogueToBoss.containsKey(message)) {
            String bossID = BossState.dialogueToBoss.get(message);
            BossState.updateBoss(bossID);
        }

        if (Config.HANDLER.instance().hideSellFailMessage && Objects.equals(message, "> You don't have any Super Enchanted Melons. Get them by cooking four Enchanted Melon Slices, which are gotten by cooking four Melon Slices.")) {
            ci.cancel();
        }
        if (Config.HANDLER.instance().what && Objects.equals(message, "> What?") && Minecraft.getInstance().player != null) {
            Mwonmod.notification("> What?", "> What?");
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().player.connection.sendChat("> What?"));
        }
        if (Config.HANDLER.instance().down && Objects.equals(message, "> Down.") && Minecraft.getInstance().player != null) {
            Mwonmod.notification("> Down.", "> Down.");
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().player.connection.sendChat("> Down."));
        }
        Pattern auctionPattern = Pattern.compile("^>\\s*(?:First up,|And next,|Next,|And now,|And lastly,|Now,|Up next,)?\\s*(?:a|an|some)\\s+(.+?)!$");
        Pattern newKingPattern = Pattern.compile("^>\\s*([\\w]+)\\s+is\\s+the\\s+new\\s+(?:king|queen|monarch)!$");

        Matcher auctionMatcher = auctionPattern.matcher(message);
        Matcher kingMatcher = newKingPattern.matcher(message);

        if (auctionMatcher.find()) {
            assert Minecraft.getInstance().player != null;
            if (Config.HANDLER.instance().debugMode)
                Minecraft.getInstance().player.displayClientMessage(net.minecraft.network.chat.Component.literal("Auction Item: " + auctionMatcher.group(1)), false);
            try {
                var itemKey = auctionMatcher.group(1).toLowerCase().replaceAll(" ", "_");
                var itemElement = Mwonmod.itemData.get(itemKey);
                if (itemElement == null) {
                    if (Config.HANDLER.instance().debugMode)
                        Minecraft.getInstance().player.displayClientMessage(net.minecraft.network.chat.Component.literal("Item data not found for: " + itemKey), false);
                    return;
                }
                JsonObject itemData = itemElement.getAsJsonObject();
                Minecraft.getInstance().player.displayClientMessage(net.minecraft.network.chat.Component.literal("Auction Item: " + itemData.get("name").getAsString()).withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(net.minecraft.network.chat.Component.nullToEmpty(itemData.get("description").getAsString())))), false);
            } catch (Exception e) {
                if (Config.HANDLER.instance().debugMode)
                    Minecraft.getInstance().player.displayClientMessage(net.minecraft.network.chat.Component.literal("Error accessing item data: " + e.getMessage()), false);
            }
        } else if (kingMatcher.find()) {
            assert Minecraft.getInstance().player != null;
            if (Config.HANDLER.instance().kingChangeNotification) {
                GameState.bank_gold = 0;
                GameState.coins = 0;
                BossState.updateBoss(null);
                GameState.currentMonarch = kingMatcher.group(1);
                Mwonmod.notification("New Monarch", kingMatcher.group(1));
            }
        }
    }
}
