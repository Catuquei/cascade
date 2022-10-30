package cascade.util.misc;

import cascade.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class CalcUtil implements Util {

    public static double getDistance(double posX, double posY, double posZ) {
        double x = mc.player.posX - posX;
        double y = mc.player.posY - posY;
        double z = mc.player.posY - posZ;
        return MathHelper.sqrt((x * x) + (y * y) + (z * z));
    }

    public static double getDistance(Entity entity) {
        double x = mc.player.posX - entity.posX;
        double y = mc.player.posY - entity.posY;
        double z = mc.player.posY - entity.posZ;
        return MathHelper.sqrt((x * x) + (y * y) + (z * z));
    }

}