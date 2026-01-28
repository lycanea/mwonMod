package dev.lycanea.mwonmod.util;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.music.CustomMusicManager;
import dev.lycanea.mwonmod.music.CustomSong;
import dev.lycanea.mwonmod.util.region.Region;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
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
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class Commands {
    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext commandRegistryAccess) {
        var mwonmodCommand = literal("mwonmod")
            .then(literal("discord")
                .executes(context -> {
                    assert Minecraft.getInstance().player != null;
                    Minecraft.getInstance().player.displayClientMessage(
                            Component.literal("Join the Developer Discord plz thx :3")
                                    .withStyle(style -> style.withClickEvent(
                                            new ClickEvent.OpenUrl(URI.create("https://discord.gg/ZsyGyMuvbz"))
                                    )),
                            false
                    );
                    return 1;
                })
            );

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            mwonmodCommand = mwonmodCommand.then(
                literal("itemlookup")
                    .then(ClientCommandManager.argument("value", StringArgumentType.string())
                        .executes(context -> {
                            String value = StringArgumentType.getString(context, "value");
                            JsonObject lookupItemData = itemData.get(value.toLowerCase().replace(" ", "_")).getAsJsonObject();

                            assert Minecraft.getInstance().player != null;
                            Minecraft.getInstance().player.displayClientMessage(
                                Component.literal(lookupItemData.get("name").getAsString())
                                    .withStyle(style -> style.withHoverEvent(
                                        new HoverEvent.ShowText(
                                            Component.nullToEmpty(lookupItemData.get("description").getAsString())
                                        )
                                    )),
                                false
                            );
                            return 1;
                        })
                    ))
                .then(literal("helditem")
                    .executes(context -> {
                        Minecraft client = Minecraft.getInstance();
                        client.player.displayClientMessage(
                                Component.literal("item ID: " + ItemUtils.getItemID(context.getSource().getPlayer().getMainHandItem())),
                                false
                        );
                        return 1;
                    })
                )
                .then(literal("songtest")
                    .executes(context -> {
                        CustomMusicManager.setCurrentSong(CustomSong.SONG_ONE);
                        return 1;
                    })
                )
                .then(literal("songtest2")
                    .executes(context -> {
                        CustomMusicManager.setCurrentSong(CustomSong.NONE);
                        return 1;
                    })
                )
                .then(literal("region")
                    .then(ClientCommandManager.argument("action", StringArgumentType.string())
                    .then(ClientCommandManager.argument("name", StringArgumentType.string())
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
                                client.player.displayClientMessage(
                                    Component.literal("Region creation started: " + regionName),
                                    false
                                );
                            } else if (action.equalsIgnoreCase("stop")) {
                                if (Mwonmod.activeRegion != null) {
                                    client.player.displayClientMessage(
                                        Component.literal(Mwonmod.activeRegion.getDetails()),
                                        false
                                    );
                                    Mwonmod.clearActiveRegion();
                                } else {
                                    client.player.displayClientMessage(
                                        Component.literal("No active region to stop."),
                                        false
                                    );
                                }
                            } else {
                                client.player.displayClientMessage(
                                    Component.literal("Unknown region action: " + action)
                                        .withStyle(style -> style.withHoverEvent(
                                            new HoverEvent.ShowText(
                                                Component.nullToEmpty("Valid actions are: start, stop")
                                            )
                                        )),
                                    false
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
