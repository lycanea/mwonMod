package dev.lycanea.mwonmod.music;

import net.minecraft.resources.Identifier;

public enum CustomSong {
    NONE,
    SONG_ONE;

    public Identifier getSoundId() {
        return switch (this) {
            case SONG_ONE -> Identifier.parse("mwonmod:song_one");
            default -> null;
        };
    }
}
