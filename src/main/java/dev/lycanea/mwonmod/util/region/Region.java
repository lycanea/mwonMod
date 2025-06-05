package dev.lycanea.mwonmod.util.region;

import net.minecraft.util.math.Vec3i;

public class Region {
    public String name;
    public Vec3i min;
    public Vec3i max;

    public Region(String name, Vec3i min, Vec3i max) {
        this.name = name;
        this.min = min;
        this.max = max;
    }
}
