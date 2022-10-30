package cascade.util.player;

import cascade.mixin.mixins.accessor.ICPacketUseEntity;
import cascade.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CrystalUtil implements Util {

    public static List<Integer> hitCrystals = new ArrayList<>();
    public static List<BlockPos> placedPositions = new ArrayList<>();

    public static EntityEnderCrystal getCrystalToBreak(boolean inhibit, double range) {
        return (EntityEnderCrystal) mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityEnderCrystal).filter(entity -> (double)mc.player.getDistance(entity) <= range).filter(entity -> !inhibit || !hitCrystals.contains(entity.getEntityId())).min(Comparator.comparingDouble(entity -> mc.player.getDistance(entity))).orElse(null);
    }

    public static Crystal getPlacePos(Entity target, double range, double wallRange, boolean oneThirteen, double moveFactor, boolean antiSuicide, double antiSuicideFactor, double minDamage, double maxSelfDamage, boolean placeInhibit, boolean placeBlocks, int raytraceHits, double shrinkFactor) {
        ArrayList<Crystal> crystals = new ArrayList<>();
        for (BlockPos pos : getAvailablePositions(range, wallRange, oneThirteen, placeBlocks, raytraceHits, shrinkFactor)) {
            crystals.add(new Crystal(pos, target, moveFactor));
        }
        return crystals.stream().filter(crystal -> !placeInhibit || !placedPositions.contains(crystal.crystalPos)).filter(crystal -> (double)crystal.enemyDamage >= minDamage).filter(crystal -> !antiSuicide || (double)crystal.selfDamage <= maxSelfDamage).filter(crystal -> !antiSuicide || crystal.selfDamage <= crystal.enemyDamage && mc.player.getHealth() + mc.player.getAbsorptionAmount() - crystal.selfDamage > 0.0f).max(Comparator.comparingDouble(crystal -> antiSuicide ? (double)crystal.enemyDamage - (double)crystal.selfDamage * antiSuicideFactor : (double)crystal.enemyDamage)).orElse(null);
    }

    public static List<BlockPos> getAvailablePositions(double range, double wallRange, boolean oneThirteen, boolean placeBlocks, int raytraceHits, double shrinkFactor) {
        return BlockUtil.getSphere5(range, mc.player.getPosition(), true, false).stream().filter(pos -> canPlaceCrystal1(pos, oneThirteen, placeBlocks)).filter(CrystalUtil::canPlaceCrystal2).filter(pos -> doSmartRaytrace(RotationUtil.getEyesPos(), new AxisAlignedBB((double)pos.getX() + 0.5 - 1.0, (double)pos.getY(), (double)pos.getZ() + 0.5 - 1.0, (double)pos.getX() + 0.5 + 1.0, (double)(pos.getY() + 2), (double)pos.getZ() + 0.5 + 1.0).shrink(shrinkFactor), new Vec3d(mc.player.posX + (double)(mc.player.width / 2.0f), mc.player.posY, mc.player.posZ + (double)(mc.player.width / 2.0f)), wallRange, raytraceHits)).collect(Collectors.toList());
    }

    public static EnumHand getCrystalHand() {
        return mc.player.getHeldItem(EnumHand.OFF_HAND).getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
    }

    public static void placeCrystal(BlockPos pos, boolean packet, EnumHand swingArm) {
        if (packet) {
            mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, EnumFacing.UP, getCrystalHand(), 0.0f, 0.0f, 0.0f));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos, EnumFacing.UP, new Vec3d(0.0, 0.0, 0.0), getCrystalHand());
        }
        if (swingArm != null) {
            mc.player.swingArm(swingArm);
        }
        placedPositions.add(pos);
    }

    public static void breakCrystal(EntityEnderCrystal entityEnderCrystal, boolean Packet2, EnumHand hand) {
        if (Packet2) {
            mc.getConnection().sendPacket(new CPacketUseEntity(entityEnderCrystal));
        } else {
            mc.playerController.attackEntity(mc.player, entityEnderCrystal);
        }
        if (hand != null) {
            mc.player.swingArm(hand);
        }
        hitCrystals.add(entityEnderCrystal.getEntityId());
        placedPositions.clear();
    }

    public static void breakCrystal(int id, EnumHand hand) {
        CPacketUseEntity packet = new CPacketUseEntity();
        ((ICPacketUseEntity)packet).setEntityId(id);
        ((ICPacketUseEntity)packet).setAction(CPacketUseEntity.Action.ATTACK);
        mc.player.connection.sendPacket(packet);
        if (hand != null) {
            mc.player.swingArm(hand);
        }
        hitCrystals.add(id);
        placedPositions.clear();
    }

    public static void breakCrystalNoAdd(int id) {
        CPacketUseEntity packet = new CPacketUseEntity();
        ((ICPacketUseEntity)packet).setEntityId(id);
        ((ICPacketUseEntity)packet).setAction(CPacketUseEntity.Action.ATTACK);
        mc.getConnection().sendPacket(packet);
        placedPositions.clear();
    }

    public static boolean canPlaceCrystal1(BlockPos pos, boolean one13, boolean placeBlocks) {
        return (mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK || placeBlocks && BlockUtil.canPlaceBlock(pos)) && mc.world.getBlockState(pos.add(0, 1, 0)).getBlock() == Blocks.AIR && (mc.world.getBlockState(pos.add(0, 2, 0)).getBlock() == Blocks.AIR || one13);
    }

    public static boolean canPlaceCrystal2(BlockPos pos) {
        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.add(0, 1, 0)))) {
            if (entity.isDead || entity instanceof EntityEnderCrystal && hitCrystals.contains(entity.getEntityId())) continue;
            return false;
        }
        return true;
    }

    public static List<Entity> getLoadedCrystalsInRange(double range) {
        return mc.world.loadedEntityList.stream().filter(it -> it instanceof EntityEnderCrystal).filter(it -> (double)it.getDistance(mc.player) < range).collect(Collectors.toList());
    }

    public static float calculateDamage(double posX, double posY, double posZ, Entity entity, double moveFactor) {
        try {
            Vec3d applied = new Vec3d(entity.posX + entity.motionX * moveFactor, entity.posY + entity.motionY * moveFactor, entity.posZ + entity.motionZ * moveFactor);
            double factor = (1.0 - applied.distanceTo(new Vec3d(posX, posY, posZ)) / 12.0) * (double)entity.world.getBlockDensity(new Vec3d(posX, posY, posZ), entity.getEntityBoundingBox());
            float calculatedDamage = (int)((factor * factor + factor) / 2.0 * 7.0 * 12.0 + 1.0);
            double damage = 1.0;
            if (entity instanceof EntityLivingBase) {
                damage = getBlastReduction((EntityLivingBase)entity, calculatedDamage * (Minecraft.getMinecraft().world.getDifficulty().getId() == 0 ? 0.0f : (Minecraft.getMinecraft().world.getDifficulty().getId() == 2 ? 1.0f : (Minecraft.getMinecraft().world.getDifficulty().getId() == 1 ? 0.5f : 1.5f))), new Explosion((World)Minecraft.getMinecraft().world, null, posX, posY, posZ, 6.0f, false, true));
            }
            return (float)damage;
        }
        catch (Exception exception) {
            return 0.0f;
        }
    }

    public static float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            damage = CombatRules.getDamageAfterAbsorb(damage, (float)entity.getTotalArmorValue(), ((float)entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue()));
            damage *= 1.0f - MathHelper.clamp((float) EnchantmentHelper.getEnchantmentModifierDamage(entity.getArmorInventoryList(), DamageSource.causeExplosionDamage(explosion)), 0.0f, 20.0f) / 25.0f;
            if (entity.isPotionActive(MobEffects.RESISTANCE)) {
                damage -= damage / 4.0f;
            }
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float)entity.getTotalArmorValue(), ((float)entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue()));
        return damage;
    }

    public static boolean doSmartRaytrace(Vec3d startPos, AxisAlignedBB endBB, Vec3d playerPos, double wallRange, int hitCount) {
        boolean allow = false;
        int hits = 0;
        for (Vec3d pos : getSmartRaytraceVertex(endBB)) {
            RayTraceResult result = mc.world.rayTraceBlocks(startPos, pos);
            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) continue;
            ++hits;
        }
        if (hits >= hitCount) {
            allow = true;
        }
        if (!allow) {
            double centerX = (endBB.maxX - endBB.minX) / 2.0;
            double centerY = (endBB.maxY - endBB.minY) / 2.0;
            double centerZ = (endBB.maxZ - endBB.minZ) / 2.0;
            Vec3d vec3d = new Vec3d(endBB.minX + centerX, endBB.minY + centerY, endBB.minZ + centerZ);
            if (playerPos.distanceTo(vec3d) <= wallRange) {
                allow = true;
            }
        }
        return allow;
    }

    public static Vec3d[] getSmartRaytraceVertex(AxisAlignedBB boundingBox) {
        double centerX = (boundingBox.maxX - boundingBox.minX) / 2.0;
        double centerY = (boundingBox.maxY - boundingBox.minY) / 2.0;
        double centerZ = (boundingBox.maxZ - boundingBox.minZ) / 2.0;
        return new Vec3d[]{new Vec3d(boundingBox.minX + centerX, boundingBox.minY + centerY, boundingBox.minZ + centerZ), new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ), new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ), new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ), new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ), new Vec3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ), new Vec3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ), new Vec3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ), new Vec3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ)};
    }

    public static class Crystal {
        float selfDamage;
        float enemyDamage;
        long startTime;
        public boolean blockUnder;
        public BlockPos crystalPos;

        public Crystal(BlockPos crystalPos, Entity target, double moveFactor) {
            this.crystalPos = crystalPos;
            this.blockUnder = this.blockUnder;
            this.calculate(target, moveFactor);
        }

        public float getSelfDamage() {
            return this.selfDamage;
        }

        public float getEnemyDamage() {
            return this.enemyDamage;
        }

        public long getStartTime() {
            return this.startTime;
        }

        public void calculate(Entity target, double moveFactor) {
            this.enemyDamage = calculateDamage((double)this.crystalPos.getX() + 0.5, (double)this.crystalPos.getY() + 1.0, (double)this.crystalPos.getZ() + 0.5, target, moveFactor);
            this.selfDamage = calculateDamage((double)this.crystalPos.getX() + 0.5, (double)this.crystalPos.getY() + 1.0, (double)this.crystalPos.getZ() + 0.5, (Entity)Minecraft.getMinecraft().player, 0.0);
            this.startTime = System.currentTimeMillis();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            Crystal crystal = (Crystal)o;
            return this.crystalPos.equals((Object)crystal.crystalPos);
        }

        public int hashCode() {
            return Objects.hash(this.crystalPos);
        }
    }
}