package cascade.util.player;

import cascade.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;

public class AttackUtil implements Util {

    public static boolean isInterceptedByCrystal(BlockPos pos) {
        for (Entity entity : mc.world.loadedEntityList) {
            if (!(entity instanceof EntityEnderCrystal)) {
                continue;
            }
            if (entity == mc.player) {
                continue;
            }
            if (entity instanceof EntityItem) {
                continue;
            }
            if (new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBlockedByCrystal(BlockPos pos) {
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityEnderCrystal && new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInterceptedByOther(BlockPos pos) {
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityEnderCrystal) {
                continue;
            }
            if (entity instanceof EntityItem) {
                continue;
            }
            if (new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
                return true;
            }
        }
        return false;
    }


    public static boolean isInterceptedByOtherTest(BlockPos pos) {
        for (Entity e : mc.world.loadedEntityList) {
            if (e instanceof EntityEnderCrystal) {
                continue;
            }
            if (e instanceof EntityItem) {
                continue;
            }
            if (e instanceof EntityExpBottle) {
                continue;
            }
            if (e instanceof EntityXPOrb) {
                continue;
            }
            if (e instanceof EntityPlayer) {
                continue;
            }
            if (e instanceof EntityArrow) {
                continue;
            }
            if (e instanceof EntityEnderPearl) {
                continue;
            }
            if (e instanceof EntityPotion) {
                continue;
            }
            if (e instanceof EntityLightningBolt) {
                continue;
            }
            if (new AxisAlignedBB(pos).intersects(e.getEntityBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInterceptedByOtherNew(BlockPos pos) {
        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
            if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb)) {
                return true;
            }
        }
        return false;
    }
}
