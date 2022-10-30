package cascade.util.misc;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class TimeVec3d extends Vec3d {
    private final long time;

    public TimeVec3d(double xIn, double yIn, double zIn, long time) {
        super(xIn, yIn, zIn);
        this.time = time;
    }

    public long getTime() {
        return time;
    }
}