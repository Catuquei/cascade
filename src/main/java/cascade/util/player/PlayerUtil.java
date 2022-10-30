package cascade.util.player;

import cascade.util.Util;
import cascade.util.entity.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerUtil implements Util {

    static List<Block> pistons = Arrays.asList(Blocks.PISTON, Blocks.PISTON_EXTENSION, Blocks.PISTON_HEAD, Blocks.STICKY_PISTON);

    public static boolean isElytraEquipped() {
        return mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA;
    }

    public static boolean isBoxColliding() {
        return mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, 0.21, 0.0)).size() > 0;
    }


    public static boolean isClipping() {
        return !(mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox()).isEmpty());
    }

    public static boolean isOffset() {
        Vec3d center = EntityUtil.getCenter(mc.player.posX, mc.player.posY, mc.player.posZ);
        return mc.player.getDistance(center.x, center.y, center.z) > 0.2;
    }

    public static boolean isPushable(double x, double y, double z) {
        Block temp;

        if ((temp = mc.world.getBlockState(new BlockPos(x, ++y, z)).getBlock()) == Blocks.PISTON_HEAD || temp == Blocks.PISTON_EXTENSION) {
            return true;
        }

        TileEntityShulkerBox tempShulker;
        AxisAlignedBB tempAxis;
        for (TileEntity entity : mc.world.loadedTileEntityList) {
            if (entity instanceof TileEntityShulkerBox) {
                if ((tempShulker = ((TileEntityShulkerBox) entity)).getProgress(mc.getRenderPartialTicks()) > 0) {
                    if ((tempAxis = tempShulker.getRenderBoundingBox()).minY <= y && tempAxis.maxY >= y && ((int) tempAxis.minX <= x && tempAxis.maxX >= x) || ((int) tempAxis.minZ <= z && tempAxis.maxZ >= z)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isPushable() {
        List<Vec3d> offsets = new ArrayList<>();
        offsets.add(mc.player.getPositionVector().add(1.0, 1.0, 0.0));
        offsets.add(mc.player.getPositionVector().add(0.0, 1.0, 1.0));
        offsets.add(mc.player.getPositionVector().add(1.0, 1.0, 0.0));
        offsets.add(mc.player.getPositionVector().add(0.0, 1.0, -1.0));
        for (Vec3d vec3d : offsets) {
            if (mc.world.getBlockState(new BlockPos(vec3d)).getBlock() == pistons) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBurrow() {
        Block block = mc.world.getBlockState(new BlockPos(mc.player.getPositionVector().add(0.0, 0.2, 0.0))).getBlock();
        return block == Blocks.OBSIDIAN || block == Blocks.ENDER_CHEST;
    }

    public static boolean isBurrowed() {
        Block block = mc.world.getBlockState(new BlockPos(mc.player.getPositionVector().add(0.0, 0.2, 0.0))).getBlock();
        return block != Blocks.AIR;
    }

    public static boolean isChestBelow() {
        return !isBurrow() && EntityUtil.isOnChest(mc.player);
    }

    public static boolean isInLiquid() {
        return mc.player.isInLava() || mc.player.isInWater();
    }

    public static boolean isInLiquidF() {
        if (mc.player.fallDistance >= 3.0f) {
            return false;
        }
        boolean inLiquid = false;
        AxisAlignedBB bb = (mc.player.getRidingEntity() != null) ? mc.player.getRidingEntity().getEntityBoundingBox() : mc.player.getEntityBoundingBox();
        int y = (int)bb.minY;
        for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX) + 1; ++x) {
            for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ) + 1; ++z) {
                Block block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                if (!(block instanceof BlockAir)) {
                    if (!(block instanceof BlockLiquid)) {
                        return false;
                    }
                    inLiquid = true;
                }
            }
        }
        return inLiquid;
    }


    public static boolean inLiquid() {
        return inLiquid(MathHelper.floor(mc.player.getEntityBoundingBox().minY + 0.01));
    }

    public static boolean inLiquid(boolean feet) {
        return inLiquid(MathHelper.floor(mc.player.getEntityBoundingBox().minY - (feet ? 0.03 : 0.2)));
    }

    private static boolean inLiquid(int y) {
        return findState(BlockLiquid.class, y) != null;
    }

    private static IBlockState findState(Class<? extends Block> block, int y) {
        int startX = MathHelper.floor(mc.player.getEntityBoundingBox().minX);
        int startZ = MathHelper.floor(mc.player.getEntityBoundingBox().minZ);
        int endX   = MathHelper.ceil(mc.player.getEntityBoundingBox().maxX);
        int endZ   = MathHelper.ceil(mc.player.getEntityBoundingBox().maxZ);
        for (int x = startX; x < endX; x++) {
            for (int z = startZ; z < endZ; z++) {
                IBlockState s = mc.world.getBlockState(new BlockPos(x, y, z));
                if (block.isInstance(s.getBlock())) {
                    return s;
                }
            }
        }
        return null;
    }

    public static boolean isAbove(BlockPos pos) {
        return mc.player.getEntityBoundingBox().minY >= pos.getY();
    }

    public static boolean isMovementBlocked() {
        IBlockState state = findState(Block.class, MathHelper.floor(mc.player.getEntityBoundingBox().minY - 0.01));
        return state != null  && state.getMaterial().blocksMovement();
    }

    public static boolean isAboveLiquid() {
        if (mc.player != null) {
            double n = mc.player.posY + 0.01;
            for (int i = MathHelper.floor(mc.player.posX); i < MathHelper.ceil(mc.player.posX); ++i) {
                for (int j = MathHelper.floor(mc.player.posZ); j < MathHelper.ceil(mc.player.posZ); ++j) {
                    if (EntityUtil.mc.world.getBlockState(new BlockPos(i, (int) n, j)).getBlock() instanceof BlockLiquid) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    public static boolean checkForLiquid(boolean b) {
        if (mc.player != null) {
            double posY = mc.player.posY;
            double n;
            if (b) {
                n = 0.03;
            } else if (mc.player instanceof EntityPlayer) {
                n = 0.2;
            } else {
                n = 0.5;
            }
            double n2 = posY - n;
            for (int i = MathHelper.floor(mc.player.posX); i < MathHelper.ceil(mc.player.posX); ++i) {
                for (int j = MathHelper.floor(mc.player.posZ); j < MathHelper.ceil(mc.player.posZ); ++j) {
                    if (EntityUtil.mc.world.getBlockState(new BlockPos(i, MathHelper.floor(n2), j)).getBlock() instanceof BlockLiquid) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    public static boolean isAboveBlock(BlockPos blockPos) {
        return mc.player.posY >= blockPos.getY();
    }

    public static void startSneaking() {
        mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
    }

    public static void stopSneaking() {
        mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
    }
}