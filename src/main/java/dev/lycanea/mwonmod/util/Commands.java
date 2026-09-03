package dev.lycanea.mwonmod.util;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.music.CustomMusicManager;
import dev.lycanea.mwonmod.music.CustomSong;
import dev.lycanea.mwonmod.util.region.Region;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import java.net.URI;

import static dev.lycanea.mwonmod.Mwonmod.itemData;
import static dev.lycanea.mwonmod.util.region.RegionLoader.beta_plot_origin;
import static dev.lycanea.mwonmod.util.region.RegionLoader.plot_origin;

public class Commands {
    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext buildContext) {
        var mwonmodCommand = ClientCommands.literal("mwonmod")
            .then(ClientCommands.literal("discord")
                .executes(context -> {
                    assert Minecraft.getInstance().player != null;
                    Minecraft.getInstance().player.sendSystemMessage(
                            Component.literal("Join the Developer Discord plz thx :3")
                                    .withStyle(style -> style.withClickEvent(
                                            new ClickEvent.OpenUrl(URI.create("https://discord.gg/ZsyGyMuvbz"))
                                    ))
                    );
                    return 1;
                })
            );

        if (Config.HANDLER.instance().debugMode) {
            mwonmodCommand = mwonmodCommand.then(
                ClientCommands.literal("itemlookup")
                    .then(ClientCommands.argument("value", StringArgumentType.string())
                        .executes(context -> {
                            String value = StringArgumentType.getString(context, "value");
                            JsonObject lookupItemData = itemData.get(value.toLowerCase().replace(" ", "_")).getAsJsonObject();

                            assert Minecraft.getInstance().player != null;
                            Minecraft.getInstance().player.sendSystemMessage(
                                Component.literal(lookupItemData.get("name").getAsString())
                                    .withStyle(style -> style.withHoverEvent(
                                        new HoverEvent.ShowText(
                                            Component.nullToEmpty(lookupItemData.get("description").getAsString())
                                        )
                                    ))
                            );
                            return 1;
                        })
                    ))
                .then(ClientCommands.literal("helditem")
                    .executes(context -> {
                        Minecraft client = Minecraft.getInstance();
                        client.player.sendSystemMessage(
                                Component.literal("item ID: " + ItemUtils.getItemID(context.getSource().getPlayer().getMainHandItem()))
                        );
                        return 1;
                    })
                )
                .then(ClientCommands.literal("songtest")
                    .executes(context -> {
                        CustomMusicManager.setCurrentSong(CustomSong.SONG_ONE);
                        return 1;
                    })
                )
                .then(ClientCommands.literal("songtest2")
                    .executes(context -> {
                        CustomMusicManager.setCurrentSong(CustomSong.NONE);
                        return 1;
                    })
                )
                .then(ClientCommands.literal("region")
                    .then(ClientCommands.argument("action", StringArgumentType.string())
                    .then(ClientCommands.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            String action = StringArgumentType.getString(context, "action");
                            String regionName = StringArgumentType.getString(context, "name");
                            Minecraft client = Minecraft.getInstance();

                            if (action.equalsIgnoreCase("start")) {
                                assert client.player != null;
                                BlockPos pos = client.player.blockPosition().offset(-plot_origin.x, 0, -plot_origin.y);
                                if (GameState.beta_plot) {
                                    pos = client.player.blockPosition().offset(-beta_plot_origin.x, 0, -beta_plot_origin.y);
                                }
                                Vec3i playerPos = new Vec3i(
                                    pos.getX(),
                                    pos.getY(),
                                    pos.getZ()
                                );
                                Region newRegion = new Region(regionName, playerPos, playerPos);
                                Mwonmod.setActiveRegion(newRegion);
                                client.player.sendSystemMessage(
                                    Component.literal("Region creation started: " + regionName)
                                );
                            } else if (action.equalsIgnoreCase("stop")) {
                                if (Mwonmod.activeRegion != null) {
                                    client.player.sendSystemMessage(
                                        Component.literal(Mwonmod.activeRegion.getDetails())
                                    );
                                    Mwonmod.clearActiveRegion();
                                } else {
                                    client.player.sendSystemMessage(
                                        Component.literal("No active region to stop.")
                                    );
                                }
                            } else {
                                client.player.sendSystemMessage(
                                    Component.literal("Unknown region action: " + action)
                                        .withStyle(style -> style.withHoverEvent(
                                            new HoverEvent.ShowText(
                                                Component.nullToEmpty("Valid actions are: start, stop")
                                            )
                                        ))
                                );
                            }
                            return 1;
                        })
                    ))
            );
        }

        dispatcher.register(mwonmodCommand);
    }
}
