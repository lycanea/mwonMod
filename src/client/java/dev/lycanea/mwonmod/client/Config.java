package dev.lycanea.mwonmod.client;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.*;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import net.minecraft.util.Identifier;

import java.lang.Boolean;


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
    @TickBox
    @AutoGen(category = "General", group = "auction")
    @SerialEntry
    public boolean auctionTimer = true;

    @TickBox
    @AutoGen(category = "General")
    @SerialEntry
    public boolean flawlessTimer = true;

    @TickBox
    @AutoGen(category = "General")
    @SerialEntry
    public boolean signUpgradeTooltip = true;

    @TickBox
    @AutoGen(category = "General", group = "misc")
    @SerialEntry
    public boolean kingChangeNotification = false;

    @MasterTickBox(value = {"fullInvEmptySlots"})
    @AutoGen(category = "General", group = "misc")
    @SerialEntry
    public boolean preventFullInventorySelling = false;

    @IntSlider(min = 0, max = 10, step = 1)
    @AutoGen(category = "General", group = "misc")
    @SerialEntry
    public int fullInvEmptySlots = 0;

    @TickBox
    @AutoGen(category = "General", group = "misc")
    @SerialEntry
    public boolean preventAttackingWithHoe = false;

    @TickBox
    @AutoGen(category = "General", group = "misc")
    @SerialEntry
    public boolean bankSignImprovements = true;

    @TickBox
    @AutoGen(category = "General", group = "misc")
    @SerialEntry
    public boolean scoreboardImprovements = true;

    @TickBox
    @AutoGen(category = "General", group = "auction")
    @SerialEntry
    public boolean auctionDesktopNotification = false;

    @TickBox
    @AutoGen(category = "General", group = "auction")
    @SerialEntry
    public boolean auctionTitleNotification = false;

    @MasterTickBox(value = {"richPresenceLine1","richPresenceLine2","richPresencePathIcon"})
    @AutoGen(category = "General", group = "discord")
    @SerialEntry
    public boolean discordRichPresence = false;

    @Dropdown(values = {"Empty","Medals","Trophies","Karma","Location","Personal Gold","Coins/City Gold","Path","Monarch"})
    @AutoGen(category = "General", group = "discord")
    @SerialEntry
    @CustomDescription(value = "Personal Gold requires entering housing once to update\nIf data is missing, mwonmod will just exclude that line from your status")
    public String richPresenceLine1 = "Monarch";

    @Dropdown(values = {"Empty","Medals","Trophies","Karma","Location","Personal Gold","Coins/City Gold","Path","Monarch"})
    @AutoGen(category = "General", group = "discord")
    @SerialEntry
    @CustomDescription(value = "Personal Gold requires entering housing once to update\nIf data is missing, mwonmod will just exclude that line from your status")
    public String richPresenceLine2 = "Coins/City Gold";

    @TickBox
    @AutoGen(category = "General", group = "discord")
    @SerialEntry
    public Boolean richPresencePathIcon = true;

    @TickBox
    @AutoGen(category = "Silly")
    @SerialEntry
    public boolean what = false;

    @TickBox
    @AutoGen(category = "Developer")
    @SerialEntry
    public boolean debugMode = false;

    @TickBox
    @AutoGen(category = "Developer")
    @SerialEntry
    public boolean ignoreMelonKingCheck = false;
}