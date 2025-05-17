package dev.lycanea.mwonmod.client;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.AutoGen;
import dev.isxander.yacl3.config.v2.api.autogen.IntSlider;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import net.minecraft.util.Identifier;
import dev.isxander.yacl3.config.v2.api.autogen.Boolean;


public class Config {
    // Create and configure the handler: file path, format (JSON5), pretty print, etc.
    public static final ConfigClassHandler<Config> HANDLER =
        ConfigClassHandler.createBuilder(Config.class)
            .id(Identifier.of("mwonmod", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(YACLPlatform.getConfigDir().resolve("mwonmod.json5"))
                .setJson5(true)
                .build())
            .build();

    // Only fields with @SerialEntry are serialized
    @Boolean
    @AutoGen(category = "General", group = "auction")
    @SerialEntry
    public boolean auctionTimer = true;

    @Boolean
    @AutoGen(category = "General")
    @SerialEntry
    public boolean flawlessTimer = true;

    @Boolean
    @AutoGen(category = "General")
    @SerialEntry
    public boolean signUpgradeTooltip = true;

    @Boolean
    @AutoGen(category = "General", group = "misc")
    @SerialEntry
    public boolean kingChangeNotification = false;

    @IntSlider(min = 0, max = 10, step = 1)
    @AutoGen(category = "General", group = "misc")
    @SerialEntry
    public int fullInvEmptySlots = 0;

    @Boolean
    @AutoGen(category = "General", group = "misc")
    @SerialEntry
    public boolean preventFullInventorySelling = false;

    @Boolean
    @AutoGen(category = "General", group = "auction")
    @SerialEntry
    public boolean auctionDesktopNotification = false;

    @Boolean
    @AutoGen(category = "General", group = "auction")
    @SerialEntry
    public boolean auctionTitleNotification = false;

    @Boolean
    @AutoGen(category = "Silly")
    @SerialEntry
    public boolean what = false;

    @Boolean
    @AutoGen(category = "Developer")
    @SerialEntry
    public boolean debugMode = false;

    @Boolean
    @AutoGen(category = "Developer")
    @SerialEntry
    public boolean ignoreMelonKingCheck = false;
}