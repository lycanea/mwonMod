package dev.lycanea.mwonmod.music;

import net.minecraft.util.Identifier;

public enum CustomSong {
    NONE,
    SONG_ONE;

    public Identifier getSoundId() {
        return switch (this) {
            case SONG_ONE -> Identifier.of("mwonmod:song_one");
            default -> null;
        };
    }
}
