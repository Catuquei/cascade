package cascade.features.modules.player;

import cascade.event.events.UpdateWalkingPlayerEvent;
import cascade.util.player.*;
import cascade.util.entity.EntityUtil;
import cascade.util.misc.MathUtil;
import cascade.util.misc.Timer;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Scaffold extends Module {

    public Scaffold() {
        super("Scaffold", Category.PLAYER, "Places blocks under ur feet");
    }

    Setting<Boolean> rotate = register(new Setting("Rotate", false));
    Setting<Boolean> safeWalk = register(new Setting("SafeWalk", true));
    Setting<Center> center = register(new Setting("Center", Center.None));
    enum Center {None, Instant, NCP}
    Timer timer = new Timer();

    @Override
    public void onEnable() {
        timer.reset();
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent e) {
        if (isDisabled() || e.getStage() == 0) {
            return;
        }
        if (!mc.gameSettings.keyBindJump.isKeyDown()) {
            timer.reset();
        }
        BlockPos playerBlock = EntityUtil.getPlayerPosWithEntity();
        if (BlockUtil.isScaffoldPos(playerBlock.add(0, -1, 0))) {
            if (BlockUtil.isValidBlock(playerBlock.add(0, -2, 0))) {
                place(playerBlock.add(0, -1, 0), EnumFacing.UP);
            } else if (BlockUtil.isValidBlock(playerBlock.add(-1, -1, 0))) {
                place(playerBlock.add(0, -1, 0), EnumFacing.EAST);
            } else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 0))) {
                place(playerBlock.add(0, -1, 0), EnumFacing.WEST);
            } else if (BlockUtil.isValidBlock(playerBlock.add(0, -1, -1))) {
                place(playerBlock.add(0, -1, 0), EnumFacing.SOUTH);
            } else if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
                place(playerBlock.add(0, -1, 0), EnumFacing.NORTH);
            } else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 1))) {
                if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
                    place(playerBlock.add(0, -1, 1), EnumFacing.NORTH);
                }
                place(playerBlock.add(1, -1, 1), EnumFacing.EAST);
            } else if (BlockUtil.isValidBlock(playerBlock.add(-1, -1, 1))) {
                if (BlockUtil.isValidBlock(playerBlock.add(-1, -1, 0))) {
                    place(playerBlock.add(0, -1, 1), EnumFacing.WEST);
                }
                place(playerBlock.add(-1, -1, 1), EnumFacing.SOUTH);
            } else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 1))) {
                if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
                    place(playerBlock.add(0, -1, 1), EnumFacing.SOUTH);
                }
                place(playerBlock.add(1, -1, 1), EnumFacing.WEST);
            } else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 1))) {
                if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
                    place(playerBlock.add(0, -1, 1), EnumFacing.EAST);
                }
                place(playerBlock.add(1, -1, 1), EnumFacing.NORTH);
            }
        }
    }

    void place(BlockPos posI, EnumFacing face) {
        BlockPos pos = posI;
        if (face == EnumFacing.UP) {
            pos = pos.add(0, -1, 0);
        } else if (face == EnumFacing.NORTH) {
            pos = pos.add(0, 0, 1);
        } else if (face == EnumFacing.SOUTH) {
            pos = pos.add(0, 0, -1);
        } else if (face == EnumFacing.EAST) {
            pos = pos.add(-1, 0, 0);
        } else if (face == EnumFacing.WEST) {
            pos = pos.add(1, 0, 0);
        }
        int oldSlot = mc.player.inventory.currentItem;
        int newSlot = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!InventoryUtil.isNull(stack) && stack.getItem() instanceof ItemBlock && Block.getBlockFromItem(stack.getItem()).getDefaultState().isFullBlock()) {
                newSlot = i;
                break;
            }
        }
        if (newSlot == -1) {
            return;
        }
        boolean crouched = false;
        if (!mc.player.isSneaking()) {
            Block block = mc.world.getBlockState(pos).getBlock();
            if (BlockUtil.blackList.contains(block)) {
                mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                crouched = true;
            }
        }
        if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)) {
            mc.getConnection().sendPacket(new CPacketHeldItemChange(newSlot));
            mc.player.inventory.currentItem = newSlot;
            mc.playerController.updateController();
        }
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            EntityPlayerSP player = mc.player;
            player.motionX *= 0.3;
            EntityPlayerSP player2 = mc.player;
            player2.motionZ *= 0.3;
            mc.player.jump();
            Vec3d CenterPos = EntityUtil.getCenter(mc.player.posX, mc.player.posY, mc.player.posZ);
            if (!EntityUtil.isPlayerSafe(mc.player) && !PlayerUtil.isChestBelow() && !EntityUtil.isInLiquid() && !MovementUtil.anyMovementKeys()) {
                switch (center.getValue()) {
                    case Instant: {
                        MovementUtil.setMotion(0, 0, 0);
                        mc.getConnection().sendPacket(new CPacketPlayer.Position(CenterPos.x, mc.player.posY, CenterPos.z, true));
                        mc.player.setPosition(CenterPos.x,  mc.player.posY, CenterPos.z);
                        break;
                    }
                    case NCP: {
                        MovementUtil.setMotion((CenterPos.x - mc.player.posX) / 2, mc.player.motionY, (CenterPos.z - mc.player.posZ) / 2);
                        break;
                    }
                }
            }
            if (timer.passedMs(1500L)) {
                mc.player.motionY = -0.28;
                timer.reset();
            }
        }
        if (rotate.getValue()) {
            float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d((double) (pos.getX() + 0.5f), (double) (pos.getY() - 0.5f), (double) (pos.getZ() + 0.5f)));
            mc.getConnection().sendPacket(new CPacketPlayer.Rotation(angle[0], (float) MathHelper.normalizeAngle((int) angle[1], 360), mc.player.onGround));
        }
        mc.playerController.processRightClickBlock(mc.player, mc.world, pos, face, new Vec3d(0.5, 0.5, 0.5), EnumHand.MAIN_HAND);
        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.getConnection().sendPacket(new CPacketHeldItemChange(oldSlot));
        mc.player.inventory.currentItem = oldSlot;
        mc.playerController.updateController();
        if (crouched) {
            mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }
}