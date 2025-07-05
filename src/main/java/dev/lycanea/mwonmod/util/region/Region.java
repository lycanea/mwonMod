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

    public void expandTo(Vec3i position) {
        this.min = new Vec3i(
            Math.min(min.getX(), position.getX()),
            Math.min(min.getY(), position.getY()),
            Math.min(min.getZ(), position.getZ())
        );

        this.max = new Vec3i(
            Math.max(max.getX(), position.getX()),
            Math.max(max.getY(), position.getY()),
            Math.max(max.getZ(), position.getZ())
        );
    }

    public String getDetails() {
        return String.format("Region '%s': Min(%d, %d, %d), Max(%d, %d, %d)",
            name, min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }
}
