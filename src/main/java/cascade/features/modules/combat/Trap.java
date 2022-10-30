package cascade.features.modules.combat;

import cascade.Cascade;
import cascade.event.events.PacketEvent;
import cascade.features.modules.Module;
import cascade.features.modules.player.Mine;
import cascade.features.setting.Setting;
import cascade.util.entity.EntityUtil;
import cascade.util.misc.Timer;
import cascade.util.player.AttackUtil;
import cascade.util.player.BlockUtil;
import cascade.util.player.InventoryUtil;
import cascade.util.player.RotationUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.List;

public class Trap extends Module {

    public Trap() {
        super("Trap", Category.COMBAT, "Traps targets");
    }

    Setting<Integer> bpt = register(new Setting("BPT", 8, 1, 25));
    Setting<Integer> delay = register(new Setting("Delay", 0, 0, 250));
    Setting<Float> range = register(new Setting("Range", 4.5f, 0.1f, 6.0f));
    Setting<Boolean> packet = register(new Setting("Packet", true));
    Setting<Boolean> predict = register(new Setting("Predict", true));
    Setting<Integer> predictBpt = register(new Setting("PredictBPT", 2, 1, 8, v -> predict.getValue()));
    Setting<Cage> cage = register(new Setting("Cage", Cage.Trap));
    enum Cage {Trap, TrapTop, TrapFullRoof, TrapFullRoofTop, CrystalExa, Crystal, CrystalFullRoof}
    Setting<Boolean> rotate = register(new Setting("Rotate", false));
    Setting<Boolean> attack = register(new Setting("Attack", true));
    Setting<Integer> toggle = register(new Setting("AutoDisable", 0, 0, 250));
    Setting<Float> maxTargetSpeed = register(new Setting("MaxTargetSpeed", 20.5f, 0.1f, 50.0f));
    //todo Setting<Boolean> logSpots = register(new Setting("LogSpots", false));
    Set<BlockPos> placeList = new HashSet<>();
    Timer toggleTimer = new Timer();
    boolean isSneaking = false;
    EntityPlayer closestTarget;
    Timer timer = new Timer();
    int offsetStep = 0;
    int packets;
    int ticks;

    static Vec3d[] TRAP = new Vec3d[] { new Vec3d(0.0, 0.0, -1.0), new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(0.0, 1.0, -1.0), new Vec3d(1.0, 1.0, 0.0), new Vec3d(0.0, 1.0, 1.0), new Vec3d(-1.0, 1.0, 0.0), new Vec3d(0.0, 2.0, -1.0), new Vec3d(1.0, 2.0, 0.0), new Vec3d(0.0, 2.0, 1.0), new Vec3d(-1.0, 2.0, 0.0), new Vec3d(0.0, 3.0, -1.0), new Vec3d(0.0, 3.0, 0.0) };
    static Vec3d[] TRAPTOP = new Vec3d[] { new Vec3d(0.0, 0.0, -1.0), new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(0.0, 1.0, -1.0), new Vec3d(1.0, 1.0, 0.0), new Vec3d(0.0, 1.0, 1.0), new Vec3d(-1.0, 1.0, 0.0), new Vec3d(0.0, 2.0, -1.0), new Vec3d(1.0, 2.0, 0.0), new Vec3d(0.0, 2.0, 1.0), new Vec3d(-1.0, 2.0, 0.0), new Vec3d(0.0, 3.0, -1.0), new Vec3d(0.0, 3.0, 0.0), new Vec3d(0.0, 4.0, 0.0) };
    static Vec3d[] TRAPFULLROOF = new Vec3d[] { new Vec3d(0.0, 0.0, -1.0), new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(0.0, 1.0, -1.0), new Vec3d(1.0, 1.0, 0.0), new Vec3d(0.0, 1.0, 1.0), new Vec3d(-1.0, 1.0, 0.0), new Vec3d(0.0, 2.0, -1.0), new Vec3d(1.0, 2.0, 0.0), new Vec3d(0.0, 2.0, 1.0), new Vec3d(-1.0, 2.0, 0.0), new Vec3d(0.0, 3.0, -1.0), new Vec3d(1.0, 3.0, 0.0), new Vec3d(0.0, 3.0, 1.0), new Vec3d(-1.0, 3.0, 0.0), new Vec3d(0.0, 3.0, 0.0) };
    static Vec3d[] TRAPFULLROOFTOP = new Vec3d[] { new Vec3d(0.0, 0.0, -1.0), new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(0.0, 1.0, -1.0), new Vec3d(1.0, 1.0, 0.0), new Vec3d(0.0, 1.0, 1.0), new Vec3d(-1.0, 1.0, 0.0), new Vec3d(0.0, 2.0, -1.0), new Vec3d(1.0, 2.0, 0.0), new Vec3d(0.0, 2.0, 1.0), new Vec3d(-1.0, 2.0, 0.0), new Vec3d(0.0, 3.0, -1.0), new Vec3d(1.0, 3.0, 0.0), new Vec3d(0.0, 3.0, 1.0), new Vec3d(-1.0, 3.0, 0.0), new Vec3d(0.0, 3.0, 0.0), new Vec3d(0.0, 4.0, 0.0) };
    static Vec3d[] CRYSTALEXA = new Vec3d[] { new Vec3d(0.0, 0.0, -1.0), new Vec3d(0.0, 1.0, -1.0), new Vec3d(0.0, 2.0, -1.0), new Vec3d(1.0, 2.0, 0.0), new Vec3d(0.0, 2.0, 1.0), new Vec3d(-1.0, 2.0, 0.0), new Vec3d(-1.0, 2.0, -1.0), new Vec3d(1.0, 2.0, 1.0), new Vec3d(1.0, 2.0, -1.0), new Vec3d(-1.0, 2.0, 1.0), new Vec3d(0.0, 3.0, -1.0), new Vec3d(0.0, 3.0, 0.0) };
    static Vec3d[] CRYSTAL = new Vec3d[] { new Vec3d(0.0, 0.0, -1.0), new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(-1.0, 0.0, 1.0), new Vec3d(1.0, 0.0, -1.0), new Vec3d(-1.0, 0.0, -1.0), new Vec3d(1.0, 0.0, 1.0), new Vec3d(-1.0, 1.0, -1.0), new Vec3d(1.0, 1.0, 1.0), new Vec3d(-1.0, 1.0, 1.0), new Vec3d(1.0, 1.0, -1.0), new Vec3d(0.0, 2.0, -1.0), new Vec3d(1.0, 2.0, 0.0), new Vec3d(0.0, 2.0, 1.0), new Vec3d(-1.0, 2.0, 0.0), new Vec3d(-1.0, 2.0, 1.0), new Vec3d(1.0, 2.0, -1.0), new Vec3d(0.0, 3.0, -1.0), new Vec3d(0.0, 3.0, 0.0) };
    static Vec3d[] CRYSTALFULLROOF = new Vec3d[] { new Vec3d(0.0, 0.0, -1.0), new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(-1.0, 0.0, 1.0), new Vec3d(1.0, 0.0, -1.0), new Vec3d(-1.0, 0.0, -1.0), new Vec3d(1.0, 0.0, 1.0), new Vec3d(-1.0, 1.0, -1.0), new Vec3d(1.0, 1.0, 1.0), new Vec3d(-1.0, 1.0, 1.0), new Vec3d(1.0, 1.0, -1.0), new Vec3d(0.0, 2.0, -1.0), new Vec3d(1.0, 2.0, 0.0), new Vec3d(0.0, 2.0, 1.0), new Vec3d(-1.0, 2.0, 0.0), new Vec3d(-1.0, 2.0, 1.0), new Vec3d(1.0, 2.0, -1.0), new Vec3d(0.0, 3.0, -1.0), new Vec3d(1.0, 3.0, 0.0), new Vec3d(0.0, 3.0, 1.0), new Vec3d(-1.0, 3.0, 0.0), new Vec3d(0.0, 3.0, 0.0) };

    static List<Block> blackList = Arrays.asList(Blocks.ENDER_CHEST, (Block)Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.BREWING_STAND, (Block)Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER);
    static List<Block> shulkerList = Arrays.asList(Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX);

    @Override
    public void onDisable() {
        if (fullNullCheck()) {
            return;
        }
        placeList.clear();
        toggleTimer.reset();
        isSneaking = false;
        closestTarget = null;
        timer.reset();
        offsetStep = 0;
        packets = 0;
        ticks = 0;
        if (isSneaking) {
            mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            isSneaking = false;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onPacketReceive(PacketEvent.Receive e) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (predict.getValue()) {
            if (e.getPacket() instanceof SPacketBlockChange) {
                SPacketBlockChange p = e.getPacket();
                if (p.blockState.getBlock() == Blocks.AIR && placeList.contains(p.blockPosition)) {
                    if (predictBpt.getValue() <= packets) {
                        mc.addScheduledTask(() -> doTrap());
                        packets++;
                    }
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        ticks++;
        mc.addScheduledTask(() -> findClosestTarget());
        if (closestTarget == null) {
            return;
        }
        mc.addScheduledTask(() -> doTrap());
        if (toggle.getValue() != 0) {
            if (timer.passedMs(toggle.getValue())) {
                disable();
                return;
            }
        }
    }

    void doTrap() {
        try {
            List<Vec3d> placeTargets = new ArrayList<>();
            switch (cage.getValue()) {
                case Trap: {
                    Collections.addAll(placeTargets, TRAP);
                    break;
                }
                case TrapTop: {
                    Collections.addAll(placeTargets, TRAPTOP);
                    break;
                }
                case TrapFullRoof: {
                    Collections.addAll(placeTargets, TRAPFULLROOF);
                    break;
                }
                case TrapFullRoofTop: {
                    Collections.addAll(placeTargets, TRAPFULLROOFTOP);
                    break;
                }
                case CrystalExa: {
                    Collections.addAll(placeTargets, CRYSTALEXA);
                    break;
                }
                case Crystal: {
                    Collections.addAll(placeTargets, CRYSTAL);
                    break;
                }
                case CrystalFullRoof: {
                    Collections.addAll(placeTargets, CRYSTALFULLROOF);
                    break;
                }
            }
            int blocksPlaced = 0;
            while (blocksPlaced < bpt.getValue()) {
                if (offsetStep >= placeTargets.size()) {
                    offsetStep = 0;
                    break;
                }
                BlockPos offsetPos = new BlockPos(placeTargets.get(offsetStep));
                BlockPos targetPos = new BlockPos(closestTarget.getPositionVector()).down().add(offsetPos.x, offsetPos.y, offsetPos.z);
                placeList.add(targetPos);
                try {
                    if (AttackUtil.isInterceptedByOtherTest(targetPos)) {
                        continue;
                    }
                } catch (Exception ex) {
                    Cascade.LOGGER.info("Caught an exception from Trap");
                    ex.printStackTrace();
                }

                if (targetPos == Mine.getInstance().currentPos) {
                    continue;
                }

                if (AttackUtil.isInterceptedByCrystal(targetPos)) {
                    if (!attack.getValue()) {
                        continue;
                    }
                    EntityEnderCrystal crystal = null;
                    for (Entity entity : mc.world.loadedEntityList) {
                        if (entity == null) {
                            continue;
                        }
                        if (mc.player.getDistance(entity) > range.getValue()) {
                            continue;
                        }
                        if (!(entity instanceof EntityEnderCrystal)) {
                            continue;
                        }
                        if (entity.isDead) {
                            continue;
                        }
                        crystal = (EntityEnderCrystal) entity;
                    }
                    if (crystal != null) {
                        if (this.rotate.getValue()) {
                            RotationUtil.faceEntity(crystal);
                        }
                        mc.getConnection().sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                        mc.getConnection().sendPacket(new CPacketUseEntity(crystal));
                    }
                }
                if (placeBlockInRange(targetPos, range.getValue())) {
                    blocksPlaced++;
                }
                offsetStep++;
            }
            if (blocksPlaced > 0) {
                if (isSneaking) {
                    mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                    isSneaking = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean placeBlockInRange(BlockPos pos, double range) {
        Block block = mc.world.getBlockState(pos).getBlock();
        if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid)) { // && !(block instanceof BlockGrass) && !(block instanceof BlockTallGrass) && !(block instanceof BlockFire) && !(block instanceof BlockSnow)
            placeList.remove(pos);
            return false;
        }
        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
            if (entity instanceof EntityItem || entity instanceof EntityXPOrb) {
                return true;
            }
        }
        EnumFacing side = getPlaceableSide(pos);
        if (side == null) {
            return false;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        if (!canBeClicked(neighbour)) {
            return false;
        }
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();
        if (mc.player.getPositionVector().distanceTo(hitVec) > range) {
            return false;
        }
        int ogSlot = mc.player.inventory.currentItem;
        int obiSlot = findObiInHotbar();
        if (obiSlot == -1) {
            disable();
            return true;
        }
        if (timer.passedMs(delay.getValue())) {
            InventoryUtil.packetSwap(obiSlot);
            if ((!isSneaking && blackList.contains(neighbourBlock)) || shulkerList.contains(neighbourBlock)) {
                mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                isSneaking = true;
            }
            if (rotate.getValue()) {
                BlockUtil.faceVectorPacketInstant(hitVec);
            }
            BlockUtil.rightClickBlock(neighbour, hitVec, EnumHand.MAIN_HAND, opposite, packet.getValue(), false);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            mc.rightClickDelayTimer = 4;
            InventoryUtil.packetSwap(ogSlot);
        }
        return true;
    }

    private int findObiInHotbar() {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock)stack.getItem()).getBlock();
                if (block instanceof BlockObsidian) {
                    slot = i;
                    break;
                }
            }
        }
        return slot;
    }

    private void findClosestTarget() {
        List<EntityPlayer> playerList = mc.world.playerEntities;
        closestTarget = null;
        for (EntityPlayer target : playerList) {
            if (target == mc.player) {
                continue;
            }
            if (Cascade.friendManager.isFriend(target.getName())) {
                continue;
            }
            if (!EntityUtil.isLiving(target)) {
                continue;
            }
            if (target.getDistance(mc.player) > 7.0f) {
                continue;
            }
            if (target.getHealth() <= 0.0f) {
                continue;
            }
            if (Cascade.speedManager.getPlayerSpeed(target) > maxTargetSpeed.getValue()) {
                continue;
            }
            if (closestTarget == null) {
                closestTarget = target;
            } else {
                if (mc.player.getDistance(target) >= mc.player.getDistance(closestTarget)) {
                    continue;
                }
                closestTarget = target;
            }
        }
    }

    EnumFacing getPlaceableSide(BlockPos pos) {
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbour = pos.offset(side);
            if (mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)) {
                IBlockState blockState = mc.world.getBlockState(neighbour);
                if (!blockState.getMaterial().isReplaceable()) {
                    return side;
                }
            }
        }
        return null;
    }

    boolean canBeClicked(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().canCollideCheck(mc.world.getBlockState(pos), false);
    }

    @Override
    public String getDisplayInfo() {
        return ticks + ""; //todo make it return seconds(0.5)
    }
}