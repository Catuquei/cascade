package cascade.features.modules.player;

import cascade.Cascade;
import cascade.event.events.BlockEvent;
import cascade.event.events.PacketEvent;
import cascade.event.events.ReachEvent;
import cascade.event.events.Render3DEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.player.BlockUtil;
import cascade.util.misc.MathUtil;
import cascade.util.misc.Timer;
import cascade.util.render.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class Mine extends Module {

    public Mine() {
        super("Mine", Category.PLAYER, "tweaks mining shit");
        INSTANCE = this;
    }

    Setting<Boolean> render = register(new Setting("Render", true));
    Setting<Boolean> reach = register(new Setting("Reach", false));
    Setting<Float> add = register(new Setting("Add", 1.0f, 0.0f, 2.0f, v -> reach.getValue()));
    Setting<Integer> boxAlpha = register(new Setting("BoxAlpha", 85, 0, 255, v -> render.getValue()));
    Setting<Float> lineWidth = register(new Setting("LineWidth", 1.0f, 0.0f, 5.0f, v -> render.getValue()));
    Setting<Float> range = register(new Setting("Range", 10.0f, 0.0f, 50.0f));
    static Mine INSTANCE = new Mine();
    IBlockState blockState;
    EnumFacing lastFacing = null;
    Timer timer = new Timer();
    boolean isMining = false;
    BlockPos lastPos = null;
    public BlockPos currentPos;
    Boolean switched;
    EnumFacing facing;

    public static Mine getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Mine();
        }
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        switched = false;
    }

    @Override
    public void onDisable() {
        switched = false;
        facing = null;
    }

    @SubscribeEvent
    public void onReachEvent(ReachEvent e) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (mc.gameSettings.keyBindAttack.isKeyDown()) {
            e.setDistance(4.5f + add.getValue());
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        mc.playerController.blockHitDelay = 0;
        if (isMining && lastPos != null && lastFacing != null) {
            mc.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, lastPos, lastFacing));
        }
        if (currentPos != null) {
            if (mc.player.getDistanceSq(currentPos) > MathUtil.square(range.getValue())) {
                currentPos = null;
                blockState = null;
                return;
            }
            if (mc.world.getBlockState(currentPos) != blockState || mc.world.getBlockState(currentPos).getBlock() == Blocks.AIR) {
                currentPos = null;
                blockState = null;
            }
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (render.getValue() && currentPos != null) {
            Color color = new Color(this.timer.passedMs((int)(2000.0f * Cascade.serverManager.getTpsFactor())) ? 0 : 255, timer.passedMs((int)(2000.0f * Cascade.serverManager.getTpsFactor())) ? 255 : 0, 0, 255);
            RenderUtil.drawBoxESP(this.currentPos, color, false, color, lineWidth.getValue(), true, true, boxAlpha.getValue(), false);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send e) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (e.getPacket() instanceof CPacketPlayerDigging) {
            CPacketPlayerDigging packet = e.getPacket();
            if (packet != null && packet.getPosition() != null) {
                try {
                    for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(packet.getPosition()))) {
                        if (entity instanceof EntityEnderCrystal) {
                            showAnimation(false, null, null);
                            return;
                        }
                    }
                } catch (Exception ex) {
                    Cascade.LOGGER.info("Caught an exception from Mine");
                    ex.printStackTrace();
                }
                if (packet.getAction().equals(CPacketPlayerDigging.Action.START_DESTROY_BLOCK)) {
                    showAnimation(true, packet.getPosition(), packet.getFacing());
                }
                if (packet.getAction().equals(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK)) {
                    showAnimation(false, null, null);
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent e) {
        if (isDisabled()) {
            return;
        }
        /*if (event.getStage() == 3 && this.reset.getValue() && mc.playerController.curBlockDamageMP > 0.1f) {
            mc.playerController.isHittingBlock = true;
        }*/
        if (e.getStage() == 4) {
            if (BlockUtil.canBreak(e.pos)) {
                /*if (reset.getValue()) {
                    mc.playerController.isHittingBlock = false;
                }*/
                if (currentPos == null) {
                    currentPos = e.pos;
                    blockState = mc.world.getBlockState(currentPos);
                    timer.reset();
                }
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, e.pos, e.facing));
                mc.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, e.pos, e.facing));
                facing = e.facing;
                e.setCanceled(true);
            }
        }
    }

    void showAnimation(boolean isMining, BlockPos lastPos, EnumFacing lastFacing) {
        this.isMining = isMining;
        this.lastPos = lastPos;
        this.lastFacing = lastFacing;
    }
}