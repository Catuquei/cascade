package cascade.util.player;

import cascade.Cascade;
import cascade.util.Util;
import net.minecraft.entity.player.EntityPlayer;

public class TargetUtil implements Util {

    public static EntityPlayer getTarget(double range) {
        EntityPlayer currentTarget = null;
        for (int size = mc.world.playerEntities.size(), i = 0; i < size; ++i) {
            EntityPlayer player = mc.world.playerEntities.get(i);
            if (!isntValid(player, range)) {
                if (currentTarget == null) {
                    currentTarget = player;
                } else if (mc.player.getDistanceSq(player) < mc.player.getDistanceSq(currentTarget)) {
                    currentTarget = player;
                }
            }
        }
        return currentTarget;
    }

    static boolean isntValid(EntityPlayer en, double range) {
        return Cascade.friendManager.isFriend(en) || mc.player.getDistance(en) > range || en == mc.player || en.getHealth() <= 0.0f || en.isDead;
    }
}