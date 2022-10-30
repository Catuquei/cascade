package cascade.features.modules.combat;

import cascade.Cascade;
import cascade.event.events.PacketEvent;
import cascade.event.events.Render3DEvent;
import cascade.features.modules.Module;
import cascade.features.modules.exploit.ChorusDelay;
import cascade.features.modules.player.Freecam;
import cascade.features.setting.Setting;
import cascade.util.entity.EntityUtil;
import cascade.util.misc.Timer;
import cascade.util.player.*;
import cascade.util.render.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Surround extends Module {

    public Surround() {
        super("Surround", Category.COMBAT, "");
    }

    Timer timer = new Timer();
    Setting<Page> page = register(new Setting("Page", Page.Placements));
    enum Page {Delays, Placements, Break, Rotations}

    Setting<Integer> delay = register(new Setting("Delay", 0, 0, 500, v -> page.getValue() == Page.Delays));
    Setting<Integer> blocksPerTick = register(new Setting("BPT", 20, 1, 20, v -> page.getValue() == Page.Delays));
    Setting<Integer> retryAmount = register(new Setting("Retries", 1, 1, 50, v -> page.getValue() == Page.Delays));

    Setting<Boolean> predict = register(new Setting("Predict", true, v -> page.getValue() == Page.Placements));
    Setting<Boolean> freecamSync = register(new Setting("FreecamSync", false, v -> page.getValue() == Page.Placements));
    Setting<Center> center = register(new Setting("Center", Center.None, v -> page.getValue() == Page.Placements));
    enum Center {None, Instant, Motion}
    Setting<Boolean> centerY = register(new Setting("CenterY", false, v -> page.getValue() == Page.Placements && center.getValue() == Center.Instant));
    Setting<Boolean> resync = register(new Setting("Resync", false, v -> page.getValue() == Page.Placements && center.getValue() == Center.Instant));

    Setting<Boolean> breakCrystals = register(new Setting("BreakCrystals", false, v -> page.getValue() == Page.Break));
    Setting<Boolean> cooldown = register(new Setting("Cooldown", true, v -> page.getValue() == Page.Break && breakCrystals.getValue()));
    Setting<Boolean> antiWeakness = register(new Setting("AntiWeakness", false, v -> page.getValue() == Page.Break && breakCrystals.getValue()));
    //Setting<Boolean> prioAttack = register(new Setting("PrioAttack", true, v -> page.getValue() == Page.Break && breakCrystals.getValue()));
    //Setting<Boolean> breakDelay = register(new Setting("BreakDelay", 0, 0, 500, v -> page.getValue() == Page.Break && breakCrystals.getValue()));
    //Setting<Float> wallRange = register(new Setting("WallRange", 3.0f, 0.1f, 6.0f, v -> page.getValue() == Page.Break && breakCrystals.getValue()));

    Setting<Boolean> rotate = register(new Setting("Rotate", false, v -> page.getValue() == Page.Rotations));
    List<BlockPos> activeBlocks = new ArrayList<>();
    List<BlockPos> offsets = new ArrayList<>();
    double startY = 0.0d;

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }
        if (freecamCheck()) {
            startY = Freecam.getInstance().getFreecamEntity().posY;
        } else {
            startY = mc.player.posY;
        }
        Vec3d CenterPos = EntityUtil.getCenter(mc.player.posX, mc.player.posY, mc.player.posZ);
        if (!EntityUtil.isPlayerSafe(mc.player) && !PlayerUtil.isChestBelow() && !EntityUtil.isInLiquid() && !mc.player.noClip && !Freecam.getInstance().isEnabled()) {
            switch (center.getValue()) {
                case Instant: {
                    MovementUtil.setMotion(0, 0, 0);
                    mc.getConnection().sendPacket(new CPacketPlayer.Position(CenterPos.x, centerY.getValue() ? CenterPos.y : mc.player.posY, CenterPos.z, true));
                    mc.player.setPosition(CenterPos.x, centerY.getValue() ? CenterPos.y : mc.player.posY, CenterPos.z);
                    if (resync.getValue()) {
                        mc.getConnection().sendPacket(new CPacketPlayer.Position(CenterPos.x, 1337.0, CenterPos.z, mc.player.onGround));
                    }
                    break;
                }
                case Motion: {
                    MovementUtil.setMotion((CenterPos.x - mc.player.posX) / 2, mc.player.motionY, (CenterPos.z - mc.player.posZ) / 2);
                    break;
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        if (!fullNullCheck()) {
            doSurround();
        }
    }

    void doSurround() {
        if ((!freecamCheck() && mc.player.posY != startY) || (freecamCheck() && Freecam.getInstance().getFreecamEntity().posY != startY)) {
            disable();
            return;
        }
        if (timer.passedMs(delay.getValue())) {
            activeBlocks.clear();
            int oldSlot = mc.player.inventory.currentItem;
            int blockSlot = getSlot();
            if (blockSlot == -1) {
                disable();
                return;
            }
            int blocksInTick = 0;
            for (int i = 0; i < retryAmount.getValue(); ++i) {
                offsets = getOffsets();
                for (BlockPos pos : offsets) {
                    if (blocksInTick > blocksPerTick.getValue()) {
                        continue;
                    }
                    if (!canPlaceBlock(pos)) {
                        continue;
                    }
                    activeBlocks.add(pos);
                    placeBlock(pos, blockSlot, oldSlot);
                    ++blocksInTick;
                }
            }
            timer.reset();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive e) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }

        if (e.getPacket() instanceof SPacketBlockChange && predict.getValue()) {
            SPacketBlockChange p = e.getPacket();
            if (p.blockState.getBlock() == Blocks.AIR && mc.player.getDistance(p.blockPosition.x, p.blockPosition.y, p.blockPosition.z) < 1.75) {
                doSurround();
            }
        }
        if (e.getPacket() instanceof SPacketPlayerPosLook) {
            if (!EntityUtil.isPlayerSafe(mc.player) && !PlayerUtil.isChestBelow() && !EntityUtil.isInLiquid() && !mc.player.noClip && !Freecam.getInstance().isEnabled()) {
                Vec3d CenterPos = EntityUtil.getCenter(mc.player.posX, mc.player.posY, mc.player.posZ);
                switch (center.getValue()) {
                    case Instant: {
                        MovementUtil.setMotion(0, 0, 0);
                        mc.getConnection().sendPacket(new CPacketPlayer.Position(CenterPos.x, centerY.getValue() ? CenterPos.y : mc.player.posY, CenterPos.z, true));
                        mc.player.setPosition(CenterPos.x, centerY.getValue() ? CenterPos.y : mc.player.posY, CenterPos.z);
                        break;
                    }
                    case Motion: {
                        MovementUtil.setMotion((CenterPos.x - mc.player.posX) / 2, mc.player.motionY, (CenterPos.z - mc.player.posZ) / 2);
                        break;
                    }
                }
            }
        }
    }

    void placeBlock(BlockPos pos, int blockSlot, int oldSlot) {
        ItemUtil.silentSwap(blockSlot);
        //rotate
        BlockUtil.placeBlock5(pos, true);
        ItemUtil.silentSwapRecover(oldSlot);
    }

    int getSlot() {
        int slot = -1;
        slot = getHotbarItemSlot(Item.getItemFromBlock(Blocks.OBSIDIAN));
        if (slot == -1) {
            slot = getHotbarItemSlot(Item.getItemFromBlock(Blocks.ENDER_CHEST));
        }
        return slot;
    }

    int getAwSlot() {
        int slot = -1;
        slot = getHotbarItemSlot(Items.DIAMOND_SWORD);
        if (slot == -1) {
            slot = getHotbarItemSlot(Items.DIAMOND_PICKAXE);
        }
        if (slot == -1) {
            slot = getHotbarItemSlot(Items.DIAMOND_AXE);
        }

        return slot;
    }

    int getHotbarItemSlot(Item item) {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            if (!mc.player.inventory.getStackInSlot(i).getItem().equals(item)) {
                continue;
            }
            slot = i;
            break;
        }
        return slot;
    }

    boolean canPlaceBlock(BlockPos pos) {
        boolean allow = true;
        if (!mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
            allow = false;
        }
        if (AttackUtil.isInterceptedByOther(pos)) {
            allow = false;
        }

        if (AttackUtil.isInterceptedByCrystal(pos)) {
            if (!breakCrystals.getValue()) {
                allow = false;
            }
            EntityEnderCrystal crystal = null;
            for (Entity entity : mc.world.loadedEntityList) {
                if (entity == null) {
                    continue;
                }
                if ((!freecamCheck() && mc.player.getDistance(entity) > 2.4) || (freecamCheck() && Freecam.getInstance().getFreecamEntity().getDistance(entity) > 2.4)) {
                    continue;
                }
                if (!(entity instanceof EntityEnderCrystal)) {
                    continue;
                }
                if (entity.isDead) {
                    continue;
                }
                crystal = (EntityEnderCrystal)entity;
            }
            //todo bro idk seems chinese
            if (crystal != null && (!cooldown.getValue() || (cooldown.getValue() && !Cascade.swapManager.hasSwapped()))) {
                /*if (rotate.getValue()) {
                    RotationUtil.faceEntity(crystal);
                }*/
                int previousSlot = mc.player.inventory.currentItem;
                boolean awSwitched = false;
                if (antiWeakness.getValue() && ItemUtil.shouldAntiWeakness()) {
                    int awSlot = getAwSlot();
                    if (awSlot != -1) {
                        ItemUtil.silentSwap(awSlot);
                        awSwitched = true;
                    }
                }
                mc.getConnection().sendPacket(new CPacketUseEntity(crystal));
                mc.getConnection().sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                if (antiWeakness.getValue() && ItemUtil.shouldAntiWeakness() && awSwitched) {
                    ItemUtil.silentSwapRecover(previousSlot);
                }
                allow = true;
            }
        }
        return allow;
    }

    List<BlockPos> getOffsets() {
        double calcPosX = 0.0d;
        double calcPosZ = 0.0d;
        if (!freecamCheck()) {
            calcPosX = mc.player.posX;
            calcPosZ = mc.player.posZ;
        } else {
            calcPosX = Freecam.getInstance().getFreecamEntity().posX;
            calcPosZ = Freecam.getInstance().getFreecamEntity().posZ;
        }
        BlockPos playerPos = getPlayerPos();
        ArrayList<BlockPos> offsets = new ArrayList<>();
        int z;
        int x;
        double decimalX = Math.abs(calcPosX) - Math.floor(Math.abs(calcPosX));
        double decimalZ = Math.abs(calcPosZ) - Math.floor(Math.abs(calcPosZ));
        int lengthXPos = calcLength(decimalX, false);
        int lengthXNeg = calcLength(decimalX, true);
        int lengthZPos = calcLength(decimalZ, false);
        int lengthZNeg = calcLength(decimalZ, true);
        ArrayList<BlockPos> tempOffsets = new ArrayList<>();
        offsets.addAll(getOverlapPos());
        for (x = 1; x < lengthXPos + 1; ++x) {
            tempOffsets.add(addToPlayer(playerPos, x, 0.0, 1 + lengthZPos));
            tempOffsets.add(addToPlayer(playerPos, x, 0.0, -(1 + lengthZNeg)));
        }
        for (x = 0; x <= lengthXNeg; ++x) {
            tempOffsets.add(addToPlayer(playerPos, -x, 0.0, 1 + lengthZPos));
            tempOffsets.add(addToPlayer(playerPos, -x, 0.0, -(1 + lengthZNeg)));
        }
        for (z = 1; z < lengthZPos + 1; ++z) {
            tempOffsets.add(addToPlayer(playerPos, 1 + lengthXPos, 0.0, z));
            tempOffsets.add(addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, z));
        }
        for (z = 0; z <= lengthZNeg; ++z) {
            tempOffsets.add(addToPlayer(playerPos, 1 + lengthXPos, 0.0, -z));
            tempOffsets.add(addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, -z));
        }
        for (BlockPos pos2 : tempOffsets) {
            if (!hasSurroundingBlock(pos2)) {
                offsets.add(pos2.add(0, -1, 0));
            }
            offsets.add(pos2);
        }
        return offsets;
    }

    boolean hasSurroundingBlock(BlockPos pos) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (mc.world.getBlockState(pos.offset(facing)).getBlock() == Blocks.AIR) {
                continue;
            }
            return true;
        }
        return false;
    }

    BlockPos addToPlayer(BlockPos playerPos, double x, double y, double z) {
        if (playerPos.getX() < 0) {
            x = -x;
        }
        if (playerPos.getY() < 0) {
            y = -y;
        }
        if (playerPos.getZ() < 0) {
            z = -z;
        }
        return playerPos.add(x, y, z);
    }

    int calcLength(double decimal, boolean negative) {
        if (negative) {
            return decimal <= 0.3 ? 1 : 0;
        }
        return decimal >= 0.7 ? 1 : 0;
    }

    boolean isOverlapping(int offsetX, int offsetZ) {
        boolean overlapping = false;
        double decimalX = mc.player.posX - Math.floor(mc.player.posX);
        decimalX = Math.abs(decimalX);
        double decimalZ = mc.player.posZ - Math.floor(mc.player.posZ);
        decimalZ = Math.abs(decimalZ);
        if (offsetX > 0 && decimalX > 0.7) {
            overlapping = true;
        }
        if (offsetX < 0 && decimalX < 0.3) {
            overlapping = true;
        }
        if (offsetZ > 0 && decimalZ >= 0.7) {
            overlapping = true;
        }
        if (offsetZ < 0 && decimalZ < 0.3) {
            overlapping = true;
        }
        return overlapping;
    }

    List<BlockPos> getOverlapPos() {
        double calcPosX = 0.0d;
        double calcPosZ = 0.0d;
        if (!freecamCheck()) {
            calcPosX = mc.player.posX;
            calcPosZ = mc.player.posZ;
        } else {
            calcPosX = Freecam.getInstance().getFreecamEntity().posX;
            calcPosZ = Freecam.getInstance().getFreecamEntity().posZ;
        }
        ArrayList<BlockPos> positions = new ArrayList<>();
        double decimalX = calcPosX - Math.floor(calcPosX);
        double decimalZ = calcPosZ - Math.floor(calcPosZ);
        int offX = calcOffset(decimalX);
        int offZ = calcOffset(decimalZ);
        positions.add(getPlayerPos());
        for (int x = 0; x <= Math.abs(offX); ++x) {
            for (int z = 0; z <= Math.abs(offZ); ++z) {
                int properX = x * offX;
                int properZ = z * offZ;
                positions.add(getPlayerPos().add(properX, -1, properZ));
            }
        }
        return positions;
    }

    int calcOffset(double dec) {
        return dec >= 0.7 ? 1 : (dec <= 0.3 ? -1 : 0);
    }

    BlockPos getPlayerPos() {
        double calcPosX = 0.0d;
        double calcPosY = 0.0d;
        double calcPosZ = 0.0d;
        if (!freecamCheck()) {
            calcPosX = mc.player.posX;
            calcPosY = mc.player.posY;
            calcPosZ = mc.player.posZ;
        } else {
            calcPosX = Freecam.getInstance().getFreecamEntity().posX;
            calcPosY = Freecam.getInstance().getFreecamEntity().posY;
            calcPosZ = Freecam.getInstance().getFreecamEntity().posZ;
        }
        double decimalPoint = calcPosY - Math.floor(calcPosY);
        return new BlockPos(calcPosX, decimalPoint > 0.8 ? Math.floor(calcPosY) + 1.0 : Math.floor(calcPosY), calcPosZ);
    }

    boolean checkForEntities(BlockPos pos) {
        Iterator iterator = mc.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos)).iterator();
        if (iterator.hasNext()) {
            Entity e = (Entity) iterator.next();
            return false;
        }
        return true;
    }

    boolean freecamCheck() {
        return Freecam.getInstance().isEnabled() && freecamSync.getValue();
    }
}