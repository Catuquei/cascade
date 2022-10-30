package cascade.features.modules.combat;

import cascade.Cascade;
import cascade.event.events.PacketEvent;
import cascade.event.events.Render3DEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.misc.MathUtil;
import cascade.util.misc.Timer;
import cascade.util.player.BlockUtil;
import cascade.util.player.InventoryUtil;
import cascade.util.player.ItemUtil;
import cascade.util.player.TargetUtil;
import cascade.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

public class CascadeAura extends Module {

    public CascadeAura() {
        super("CascadeAura", Module.Category.COMBAT, "");
        INSTANCE = this;
    }

    static CascadeAura INSTANCE;

    public static CascadeAura getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CascadeAura();
        }
        return INSTANCE;
    }

    Setting<Page> page = register(new Setting("Page", Page.Place));
    enum Page {Place, Break, SResolver, Misc, Render}

    Setting<Float> placeRange = register(new Setting("PlaceRange", 5.0f, 0.1f, 6.0f, v -> page.getValue() == Page.Place));
    Setting<Float> placeWallRange = register(new Setting("PlaceWallRange", 5.0f, 0.1f, 6.0f, v -> page.getValue() == Page.Place));
    Setting<Boolean> holePlacement = register(new Setting("HolePlacement", false, v -> page.getValue() == Page.Place));
    Setting<Boolean> swapBypass = register(new Setting("SwapBypass", false, v -> page.getValue() == Page.Place));
    //Setting<Boolean> antiWeaknessOnly = register(new Setting("AntiWeaknessOnly", ))

    Setting<Integer> breakDelay = register(new Setting("BreakDelay", 0, 0, 500, v -> page.getValue() == Page.Break));
    Setting<Float> breakRange = register(new Setting("BreakRange", 5.0f, 1.0f, 6.0f, v -> page.getValue() == Page.Break));
    Setting<Float> breakWallRange = register(new Setting("WallRange", 5.0f, 1.0f, 6.0f, v -> page.getValue() == Page.Break));
    Setting<Boolean> cooldown = register(new Setting("Cooldown", true, v -> page.getValue() == Page.Break));

    Setting<Boolean> predict = register(new Setting("Predict", true, v -> page.getValue() == Page.SResolver));
    Setting<Boolean> sound = register(new Setting("Sound", true, v -> page.getValue() == Page.SResolver));
    Setting<Boolean> explosion = register(new Setting("Explosion", true, v -> page.getValue() == Page.SResolver));
    Setting<Boolean> destroyEntity = register(new Setting("DestroyEntity", true, v -> page.getValue() == Page.SResolver));

    Setting<Double> facePlaceHp = register(new Setting("FaceplaceHP", 6.6, 0.1, 36.0, v -> page.getValue() == Page.Misc));
    Setting<Double> minDamage = register(new Setting("MinDamage", 6.6, 0.1, 36.0, v -> page.getValue() == Page.Misc));
    Setting<Double> maxSelfDamage = register(new Setting("MaxSelfDamage", 7.0, 0.1, 36.0, v -> page.getValue() == Page.Misc));
    Setting<Integer> armorPercentage = register(new Setting("Armor%", 18, 0, 100, v -> page.getValue() == Page.Misc));
    Setting<Float> range = register(new Setting("Range", 11.0f, 1.0f, 15.0f, v -> page.getValue() == Page.Misc));
    public Setting<Swing> swing = register(new Setting("Swing", Swing.None, v -> page.getValue() == Page.Misc));
    public enum Swing {Main, Off, None}
    Setting<SwingOn> swingOn = register(new Setting("SwingOn", SwingOn.Break, v -> page.getValue() == Page.Misc && swing.getValue() != Swing.None));
    enum SwingOn {Break, Place, Both}

    Setting<Boolean> positionRender = register(new Setting("PositionRender", false, v -> page.getValue() == Page.Render));
    Setting<Color> positionColor = register(new Setting("ColorColor", new Color(63, 255, 15), v -> page.getValue() == Page.Render && positionRender.getValue()));
    public Setting<Boolean> crystalChams = register(new Setting("CrystalChams", false, v -> page.getValue() == Page.Render));
    public Setting<Color> chamsColor = register(new Setting("ChamsColor", new Color(30, 255, 20), v -> page.getValue() == Page.Render && crystalChams.getValue()));
    Setting<Boolean> targetName = register(new Setting("TargetNameHUD", true, v -> page.getValue() == Page.Render));
    //Setting<Boolean> targetDamage = register(new Setting("TargetDamage", false, v -> page.getValue() == Page.Render));
    //Setting<Boolean> targetPops = register(new Setting("TargetPops", true, v -> page.getValue() == Page.Render));

    static DamageSource EXPLOSION_SOURCE = new DamageSource("explosion").setDifficultyScaled().setExplosion();
    public Set<BlockPos> placeSet = new HashSet<>();
    public Set<Integer> breakSet = new HashSet<>();
    Timer breakTimer = new Timer();
    EntityPlayer currentTarget;
    double currentDamage;
    BlockPos placingPos;
    BlockPos renderPos;
    boolean lowArmor;
    int ticks;

    @Override
    public void onToggle() {
        placeSet.clear();
        breakSet.clear();
        placingPos = null;
        renderPos = null;
    }

    @Override
    public String getDisplayInfo() {
        StringBuilder name = null;
        if (!fullNullCheck() && currentTarget != null && targetName.getValue()) {
            return currentTarget.getName();
            /*if (targetName.getValue()) {
                name = new StringBuilder(currentTarget.getName()).append(ChatFormatting.GRAY).append(", ");
            }
            if (targetDamage.getValue()) {
                if (currentDamage < minDamage.getValue()) {
                    name.append(ChatFormatting.RED);
                }
                if (currentDamage < (minDamage.getValue() * 1.2d) && currentDamage >= minDamage.getValue()) {
                    name.append(ChatFormatting.YELLOW);
                }
                if (currentDamage >= minDamage.getValue() * 1.21d) {
                    name.append(ChatFormatting.GREEN);
                }
                name.append(MathUtil.round(currentDamage, 2)).toString();
            }
            if (targetPops.getValue()) {

            }*/

            //return String.valueOf(name);
        }

        return null;
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (ticks++ > 20) {
            ticks = 0;
            placeSet.clear();
            breakSet.clear();
            placingPos = null;
            renderPos = null;
        }
        currentTarget = TargetUtil.getTarget(range.getValue());
        if (currentTarget == null) {
            return;
        }
        lowArmor = isArmorLow(currentTarget, armorPercentage.getValue());
        doBreak();
        doPlace();
    }

    void doBreak() {
        Entity maxCrystal = null;
        double maxDamage = 0.5;
        for (Entity en : mc.world.loadedEntityList) {
            if (isBreakValid(en)) {
                double targetDmg = calculate(en.posX, en.posY, en.posZ, currentTarget);
                if (targetDmg < minDamage.getValue() && getHealth(currentTarget) > facePlaceHp.getValue() && !lowArmor) {
                    continue;
                }
                double selfDamage = calculate(en.posX, en.posY, en.posZ, mc.player);
                if (selfDamage >= getHealth(mc.player) || selfDamage >= targetDmg) {
                    continue;
                }
                if (maxSelfDamage.getValue() < selfDamage) {
                    continue;
                }
                if (maxDamage > targetDmg) {
                    continue;
                }
                maxCrystal = en;
                maxDamage = targetDmg;
            }
        }
        if (breakTimer.passedMs(breakDelay.getValue())) {
            breakTimer.reset();
            //return;
        }
        if (maxCrystal != null && (!cooldown.getValue() || (cooldown.getValue() && !Cascade.swapManager.hasSwapped()))) {
            breakSet.add(maxCrystal.getEntityId());
            mc.getConnection().sendPacket(new CPacketUseEntity(maxCrystal));
            if (swing.getValue() != Swing.None && swingOn.getValue() != SwingOn.Place) {
                EnumHand hand = swing.getValue() == Swing.Main ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                mc.player.swingArm(hand);
            }
        }
    }

    void doPlace() {
        BlockPos placePos = null;
        double maxDamage = 0.5;
        for (BlockPos pos : getSphere(placeRange.getValue(), true)) {
            if (!canPlaceCrystal(pos, holePlacement.getValue())) {
                continue;
            }

            double targetDmg = calculate(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, currentTarget);
            if (targetDmg < minDamage.getValue() && getHealth(currentTarget) > facePlaceHp.getValue() && !lowArmor) {
                continue;
            }
            double selfDmg = calculate(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, mc.player);
            if (selfDmg >= getHealth(mc.player) || selfDmg >= targetDmg) {
                continue;
            }
            if (maxSelfDamage.getValue() < selfDmg) {
                continue;
            }
            if (maxDamage > targetDmg) {
                continue;
            }
            placePos = pos;
            maxDamage = targetDmg;
        }
        //idk if its certified hood classics or nah bruh
        if (!InventoryUtil.heldItem(Items.END_CRYSTAL, InventoryUtil.Hand.Both) && !swapBypass.getValue() && ItemUtil.getItemFromHotbar(Items.ENDER_PEARL) != -1) {
            return;
        }
        if (maxDamage != 0.5) {
            if (mc.world.getBlockState(placePos.up()).getBlock() == Blocks.FIRE) {
                mc.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, placePos.up(), EnumFacing.DOWN));
                mc.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, placePos.up(), EnumFacing.DOWN));
            }
            int crystalSlot = ItemUtil.getItemFromHotbar(Items.END_CRYSTAL);
            int oldSlot = mc.player.inventory.currentItem;
            boolean swapped = false;
            if (swapBypass.getValue() && !swapped && crystalSlot != -1) {
                ItemUtil.bypassSwap(crystalSlot);
                swapped = true;
            }
            mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(placePos, EnumFacing.UP, InventoryUtil.heldItem(Items.END_CRYSTAL, InventoryUtil.Hand.Off) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, (float)placePos.x, (float)placePos.y, (float)placePos.z));
            if (swapBypass.getValue() && swapped) {
                ItemUtil.bypassSwap(oldSlot);
                swapped = false;
            }
            placeSet.add(placePos);
            placingPos = placePos;
            renderPos = placePos;
            currentDamage = maxDamage;
            if (swing.getValue() != Swing.None && swingOn.getValue() != SwingOn.Break) {
                EnumHand hand = swing.getValue() == Swing.Main ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                mc.player.swingArm(hand);
            }
        } else {
            renderPos = null;
        }
    }

    boolean isBreakValid(Entity en) {
        if (!(en instanceof EntityEnderCrystal)) {
            return false;
        }
        if (en.getDistance(mc.player) > breakRange.getValue()) {
            return false;
        }

        if (!mc.player.canEntityBeSeen(en) && en.getDistance(mc.player) > breakWallRange.getValue()) {
            return false;
        }
        if (en.isDead) {
            return false;
        }
        return true;
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent e) {
        if (renderPos != null && positionRender.getValue()) {
            RenderUtil.drawBoxESP(renderPos, new Color(positionColor.getValue().getRed(), positionColor.getValue().getGreen(), positionColor.getValue().getBlue(), positionColor.getValue().getAlpha()), false, new Color(positionColor.getValue().getRed(), positionColor.getValue().getGreen(), positionColor.getValue().getBlue(), positionColor.getValue().getAlpha()), 0.6f, true, true, positionColor.getValue().getAlpha(), true);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive e) {
        if (isDisabled() || fullNullCheck()) {
            return;
        }
        if (e.getPacket() instanceof SPacketSpawnObject && predict.getValue()) {
            SPacketSpawnObject p = e.getPacket();
            if (p.getType() == 51 && placeSet.contains(new BlockPos(p.getX(), p.getY(), p.getZ()).down()) && (!cooldown.getValue() || (cooldown.getValue() && !Cascade.swapManager.hasSwapped()))) {
                CPacketUseEntity predict = new CPacketUseEntity();
                predict.entityId = p.getEntityID();
                predict.action = CPacketUseEntity.Action.ATTACK;
                mc.getConnection().sendPacket(predict);
            }
        }
        if (e.getPacket() instanceof SPacketSoundEffect && sound.getValue()) {
            SPacketSoundEffect p = e.getPacket();
            if (p.getCategory() == SoundCategory.BLOCKS && p.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                for (Entity en : new ArrayList<>(mc.world.loadedEntityList)) {
                    if (!breakSet.contains(en.getEntityId())) {
                        return;
                    }
                    if (en instanceof EntityEnderCrystal && en.getDistance(p.getX(), p.getY(), p.getZ()) < 6.0d) {
                        en.setDead();
                        mc.addScheduledTask(() -> {
                            mc.world.removeEntity(en);
                            mc.world.removeEntityDangerously(en);
                        });
                    }
                }
            }
        }
        if (e.getPacket() instanceof SPacketExplosion && explosion.getValue()) {
            SPacketExplosion p = e.getPacket();
            if (p.getStrength() == 6.0f) {
                for (Entity en : new ArrayList<>(mc.world.loadedEntityList)) {
                    if (!breakSet.contains(en.getEntityId())) {
                        return;
                    }
                    if (en instanceof EntityEnderCrystal && en.getDistance(p.getX(), p.getY(), p.getZ()) <= 6.0d) {
                        en.setDead();
                        mc.addScheduledTask(() -> {
                            mc.world.removeEntity(en);
                            mc.world.removeEntityDangerously(en);
                        });
                    }
                }
            }
        }
        if (e.getPacket() instanceof SPacketDestroyEntities && destroyEntity.getValue()) {
            SPacketDestroyEntities p = e.getPacket();
            for (int enID : p.getEntityIDs()) {
                Entity en = mc.world.getEntityByID(enID);
                if (en instanceof EntityEnderCrystal && en.getDistance(mc.player) < 6.0f) {
                    if ((!breakSet.isEmpty() || !breakSet.contains(en.getEntityId()))) {
                        return;
                    }
                    en.setDead();
                    mc.addScheduledTask(() -> {
                        mc.world.removeEntity(en);
                        mc.world.removeEntityDangerously(en);
                    });
                }
            }
        }
        /*if (e.getPacket() instanceof SPacketBlockChange && antiSurround.getValue()) {
            SPacketBlockChange p = e.getPacket();
            try {
                if (p.getBlockState().getBlock().equals(Blocks.AIR) && mc.player.getDistanceSq(p.getBlockPosition()) < (double)(placeRange.getValue() * placeRange.getValue()) && currentTarget.getDistanceSq(p.getBlockPosition()) < 4.0d) {
                    BlockPos pos = p.getBlockPosition();
                    placeSet.add(pos);
                    mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, EnumFacing.UP, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, (float)pos.x, (float)pos.y, (float)pos.z));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }*/
    }

    boolean isArmorLow(EntityPlayer player, int durability) {
        for (int i = 0; i < 4; ++i) {
            if (getDamageInPercent(player.inventory.armorInventory.get(i)) < durability) {
                return true;
            }
        }
        return false;
    }

    float getDamageInPercent(ItemStack stack) {
        float green = (stack.getMaxDamage() - (float)stack.getItemDamage()) / stack.getMaxDamage();
        float red = 1.0f - green;
        return (float)(100 - (int)(red * 100.0f));
    }

    float calculate(double posX, double posY, double posZ, EntityLivingBase entity) {
        double v = (1.0 - entity.getDistance(posX, posY, posZ) / 12.0) * getBlockDensity(new Vec3d(posX, posY, posZ), entity.getEntityBoundingBox());
        return getBlastReduction(entity, getDamageMultiplied((float)((v * v + v) / 2.0 * 85.0 + 1.0)));
    }

    float getBlastReduction(EntityLivingBase entity, float damageI) {
        float damage = damageI;
        damage = CombatRules.getDamageAfterAbsorb(damage, (float)entity.getTotalArmorValue(), (float)entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        damage *= 1.0f - MathHelper.clamp((float)EnchantmentHelper.getEnchantmentModifierDamage(entity.getArmorInventoryList(), EXPLOSION_SOURCE), 0.0f, 20.0f) / 25.0f;
        if (entity.isPotionActive(MobEffects.RESISTANCE)) {
            return damage - damage / 4.0f;
        }
        return damage;
    }
    float getBlockDensity(Vec3d vec, AxisAlignedBB bb) {
        double d0 = 1.0 / ((bb.maxX - bb.minX) * 2.0 + 1.0);
        double d2 = 1.0 / ((bb.maxY - bb.minY) * 2.0 + 1.0);
        double d3 = 1.0 / ((bb.maxZ - bb.minZ) * 2.0 + 1.0);
        double d4 = (1.0 - Math.floor(1.0 / d0) * d0) / 2.0;
        double d5 = (1.0 - Math.floor(1.0 / d3) * d3) / 2.0;
        float j2 = 0.0f;
        float k2 = 0.0f;
        for (float f = 0.0f; f <= 1.0f; f += (float)d0) {
            for (float f2 = 0.0f; f2 <= 1.0f; f2 += (float)d2) {
                for (float f3 = 0.0f; f3 <= 1.0f; f3 += (float)d3) {
                    double d6 = bb.minX + (bb.maxX - bb.minX) * f;
                    double d7 = bb.minY + (bb.maxY - bb.minY) * f2;
                    double d8 = bb.minZ + (bb.maxZ - bb.minZ) * f3;
                    if (rayTraceBlocks(new Vec3d(d6 + d4, d7, d8 + d5), vec, false, false, false) == null) {
                        ++j2;
                    }
                    ++k2;
                }
            }
        }
        return j2 / k2;
    }

    RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        int i = MathHelper.floor(vec32.x);
        int j = MathHelper.floor(vec32.y);
        int k = MathHelper.floor(vec32.z);
        int l = MathHelper.floor(vec31.x);
        int i2 = MathHelper.floor(vec31.y);
        int j2 = MathHelper.floor(vec31.z);
        BlockPos blockpos = new BlockPos(l, i2, j2);
        IBlockState iblockstate = mc.world.getBlockState(blockpos);
        Block block = iblockstate.getBlock();
        if ((!ignoreBlockWithoutBoundingBox || iblockstate.getCollisionBoundingBox(mc.world, blockpos) != Block.NULL_AABB) && block.canCollideCheck(iblockstate, stopOnLiquid)) {
            return iblockstate.collisionRayTrace(mc.world, blockpos, vec31, vec32);
        }
        RayTraceResult raytraceresult2 = null;
        int k2 = 200;
        while (k2-- >= 0) {
            if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z)) {
                return null;
            }
            if (l == i && i2 == j && j2 == k) {
                return returnLastUncollidableBlock ? raytraceresult2 : null;
            }
            boolean flag2 = true;
            boolean flag3 = true;
            boolean flag4 = true;
            double d0 = 999.0;
            double d2 = 999.0;
            double d3 = 999.0;
            if (i > l) {
                d0 = l + 1.0;
            } else if (i < l) {
                d0 = l + 0.0;
            } else {
                flag2 = false;
            }
            if (j > i2) {
                d2 = i2 + 1.0;
            } else if (j < i2) {
                d2 = i2 + 0.0;
            } else {
                flag3 = false;
            }
            if (k > j2) {
                d3 = j2 + 1.0;
            } else if (k < j2) {
                d3 = j2 + 0.0;
            } else {
                flag4 = false;
            }
            double d4 = 999.0;
            double d5 = 999.0;
            double d6 = 999.0;
            double d7 = vec32.x - vec31.x;
            double d8 = vec32.y - vec31.y;
            double d9 = vec32.z - vec31.z;
            if (flag2) {
                d4 = (d0 - vec31.x) / d7;
            }
            if (flag3) {
                d5 = (d2 - vec31.y) / d8;
            }
            if (flag4) {
                d6 = (d3 - vec31.z) / d9;
            }
            if (d4 == -0.0) {
                d4 = -1.0E-4;
            }
            if (d5 == -0.0) {
                d5 = -1.0E-4;
            }
            if (d6 == -0.0) {
                d6 = -1.0E-4;
            }
            EnumFacing enumfacing;
            if (d4 < d5 && d4 < d6) {
                enumfacing = ((i > l) ? EnumFacing.WEST : EnumFacing.EAST);
                vec31 = new Vec3d(d0, vec31.y + d8 * d4, vec31.z + d9 * d4);
            } else if (d5 < d6) {
                enumfacing = ((j > i2) ? EnumFacing.DOWN : EnumFacing.UP);
                vec31 = new Vec3d(vec31.x + d7 * d5, d2, vec31.z + d9 * d5);
            } else {
                enumfacing = ((k > j2) ? EnumFacing.NORTH : EnumFacing.SOUTH);
                vec31 = new Vec3d(vec31.x + d7 * d6, vec31.y + d8 * d6, d3);
            }
            l = MathHelper.floor(vec31.x) - ((enumfacing == EnumFacing.EAST) ? 1 : 0);
            i2 = MathHelper.floor(vec31.y) - ((enumfacing == EnumFacing.UP) ? 1 : 0);
            j2 = MathHelper.floor(vec31.z) - ((enumfacing == EnumFacing.SOUTH) ? 1 : 0);
            blockpos = new BlockPos(l, i2, j2);
            IBlockState iblockstate2 = mc.world.getBlockState(blockpos);
            Block block2 = iblockstate2.getBlock();
            if (ignoreBlockWithoutBoundingBox && iblockstate2.getMaterial() != Material.PORTAL && iblockstate2.getCollisionBoundingBox(mc.world, blockpos) == Block.NULL_AABB) {
                continue;
            }
            if (block2.canCollideCheck(iblockstate2, stopOnLiquid) && !(block2 instanceof BlockWeb)) {
                return iblockstate2.collisionRayTrace(mc.world, blockpos, vec31, vec32);
            }
            raytraceresult2 = new RayTraceResult(RayTraceResult.Type.MISS, vec31, enumfacing, blockpos);
        }
        return returnLastUncollidableBlock ? raytraceresult2 : null;
    }

    float getDamageMultiplied(float damage) {
        int diff = mc.world.getDifficulty().getId();
        return damage * ((diff == 0) ? 0.0f : ((diff == 2) ? 1.0f : ((diff == 1) ? 0.5f : 1.5f)));
    }

    float getHealth(EntityLivingBase player) {
        return player.getHealth() + player.getAbsorptionAmount();
    }

    List<BlockPos> getSphere(float radius, boolean ignoreAir) {
        List<BlockPos> sphere = new ArrayList<>();
        BlockPos pos = new BlockPos(mc.player.getPositionVector());
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();
        int radiuss = (int)radius;
        for (int x = posX - radiuss; x <= posX + radius; ++x) {
            for (int z = posZ - radiuss; z <= posZ + radius; ++z) {
                for (int y = posY - radiuss; y < posY + radius; ++y) {
                    if ((posX - x) * (posX - x) + (posZ - z) * (posZ - z) + (posY - y) * (posY - y) < radius * radius) {
                        BlockPos position = new BlockPos(x, y, z);
                        if (!ignoreAir || mc.world.getBlockState(position).getBlock() != Blocks.AIR) {
                            sphere.add(position);
                        }
                    }
                }
            }
        }
        return sphere;
    }

    boolean canPlaceCrystal(BlockPos blockPos, boolean check) {
        if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
            return false;
        }
        if (!BlockUtil.rayTracePlaceCheck(blockPos) && mc.player.getDistanceSq(blockPos) > MathUtil.square(placeWallRange.getValue())) {
            return false;
        }
        BlockPos boost = blockPos.add(0, 1, 0);
        return mc.world.getBlockState(boost).getBlock() == Blocks.AIR && mc.world.getBlockState(blockPos.add(0, 2, 0)).getBlock() == Blocks.AIR && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost.getX(), boost.getY(), boost.getZ(), (boost.getX() + 1), (boost.getY() + (check ? 2 : 1)), (boost.getZ() + 1)), e -> !(e instanceof EntityEnderCrystal)).size() == 0;
    }
}