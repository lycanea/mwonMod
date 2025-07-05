package dev.lycanea.mwonmod.util;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.lycanea.mwonmod.Config;
import dev.lycanea.mwonmod.Mwonmod;
import dev.lycanea.mwonmod.util.region.Region;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import static dev.lycanea.mwonmod.Mwonmod.itemData;
import static dev.lycanea.mwonmod.util.region.RegionLoader.beta_plot_origin;
import static dev.lycanea.mwonmod.util.region.RegionLoader.plot_origin;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class Commands {
    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        var mwonmodCommand = literal("mwonmod")
            .then(literal("discord")
                .executes(context -> {
                    assert MinecraftClient.getInstance().player != null;
                    MinecraftClient.getInstance().player.sendMessage(
                            Text.literal("Join the Developer Discord plz thx :3")
                                    .styled(style -> style.withClickEvent(
                                            new ClickEvent(
                                                    ClickEvent.Action.OPEN_URL,
                                                    "https://discord.gg/ZsyGyMuvbz"
                                            )
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

                            assert MinecraftClient.getInstance().player != null;
                            MinecraftClient.getInstance().player.sendMessage(
                                Text.literal(lookupItemData.get("name").getAsString())
                                    .styled(style -> style.withHoverEvent(
                                        new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            Text.of(lookupItemData.get("description").getAsString())
                                        )
                                    )),
                                false
                            );
                            return 1;
                        })
                    ))
                .then(literal("region")
                    .then(ClientCommandManager.argument("action", StringArgumentType.string())
                    .then(ClientCommandManager.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            String action = StringArgumentType.getString(context, "action");
                            String regionName = StringArgumentType.getString(context, "name");
                            MinecraftClient client = MinecraftClient.getInstance();

                            if (action.equalsIgnoreCase("start")) {
                                assert client.player != null;
                                BlockPos pos = client.player.getBlockPos().add(-plot_origin.x, 0, -plot_origin.y);
                                if (GameState.beta_plot) {
                                    pos = client.player.getBlockPos().add(-beta_plot_origin.x, 0, -beta_plot_origin.y);
                                }
                                Vec3i playerPos = new Vec3i(
                                    pos.getX(),
                                    pos.getY(),
                                    pos.getZ()
                                );
                                Region newRegion = new Region(regionName, playerPos, playerPos);
                                Mwonmod.setActiveRegion(newRegion);
                                client.player.sendMessage(
                                    Text.literal("Region creation started: " + regionName),
                                    false
                                );
                            } else if (action.equalsIgnoreCase("stop")) {
                                if (Mwonmod.activeRegion != null) {
                                    client.player.sendMessage(
                                        Text.literal(Mwonmod.activeRegion.getDetails()),
                                        false
                                    );
                                    Mwonmod.clearActiveRegion();
                                } else {
                                    client.player.sendMessage(
                                        Text.literal("No active region to stop."),
                                        false
                                    );
                                }
                            } else {
                                client.player.sendMessage(
                                    Text.literal("Unknown region action: " + action)
                                        .styled(style -> style.withHoverEvent(
                                            new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                Text.of("Valid actions are: start, stop")
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

    private static void openConfigScreen() {
        MinecraftClient.getInstance().execute(() -> {
            Screen parent = MinecraftClient.getInstance().currentScreen;
            Screen configScreen = Config.HANDLER.generateGui().generateScreen(parent);
            if (configScreen != null) {
                MinecraftClient.getInstance().setScreen(configScreen);
            } else {
                System.out.println("Failed to open config screen: screen was null");
            }
        });
    }
}
