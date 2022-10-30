package cascade.features.modules.combat;

import cascade.util.player.BlockUtil;
import cascade.util.entity.EntityUtil;
import cascade.util.player.InventoryUtil;
import cascade.util.misc.Timer;
import com.mojang.realmsclient.gui.ChatFormatting;
import cascade.Cascade;
import cascade.features.command.Command;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.List;

public class WebAura extends Module {

    public WebAura() {
        super("WebAura", Category.COMBAT, "Traps enemies with webs");
    }

    Setting<Integer> delay = register(new Setting("Delay", 0, 0, 250));
    Setting<Boolean> packet = register(new Setting("Packet", true));
    Setting<Float> range = register(new Setting("Range",  6.0f, 0.1f, 6.0f));
    Setting<Boolean> motionPredict = register(new Setting("MotionPredict",  true));
    Setting<Boolean> head = register(new Setting("Head",  true));
    Setting<Boolean> feet = register(new Setting("Feet",  true));
    Setting<Boolean> rotate = register(new Setting("Rotate",  false));
    List<Vec3d> placeTargets = new ArrayList<>();
    EntityPlayer target = null;
    Timer timer = new Timer();

    @Override
    public void onDisable() {
        placeTargets.clear();
        target = null;
        timer.reset();
    }

    @Override
    public String getDisplayInfo() {
        if (target != null) {
            return target.getName();
        }
        return null;
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        target = null;
        getTarget();
        if (target != null) {
            mc.addScheduledTask(() -> doWeb());
        }
    }

    void doWeb() {
        for (Vec3d pos : placeTargets) {
            if (mc.world.getBlockState(new BlockPos(pos)).getBlock() == Blocks.AIR) {
                int webSlot = InventoryUtil.findHotbarBlock(BlockWeb.class);
                int ogSlot = mc.player.inventory.currentItem;
                if (webSlot != -1) {
                    if (timer.passedMs(delay.getValue())) {
                        InventoryUtil.packetSwap(webSlot);
                        BlockUtil.placeBlock(new BlockPos(pos), packet.getValue(), rotate.getValue());
                        InventoryUtil.packetSwap(ogSlot);
                    }
                } else {
                    disable();
                    return;
                }
            }
        }
    }

    void getTarget() {
        target = null;
        placeTargets = new ArrayList<>();
        for (EntityPlayer e : mc.world.playerEntities) {
            if (e == null) {
                continue;
            }
            if (e.getHealth() > 0.0f) {
                continue;
            }
            if (e == mc.player) {
                continue;
            }
            if (mc.player.getDistance(e) > range.getValue()) {
                continue;
            }
            if (Cascade.friendManager.isFriend((e).getName())) {
                continue;
            }
            target = e;
        }
        if (target != null) {
            placeTargets = getPlacements();
        }
    }

    List<Vec3d> getPlacements() {
        ArrayList<Vec3d> list = new ArrayList<>();
        Vec3d baseVec = target.getPositionVector();
        if (feet.getValue()) {
            if (motionPredict.getValue()) {
                list.add(baseVec.add(0.0 + target.motionX, 0.0, 0.0 + target.motionZ));
            }
            list.add(baseVec.add(0.0, 0.0, 0.0));
        }
        if (head.getValue()) {
            if (motionPredict.getValue()) {
                list.add(baseVec.add(0.0 + target.motionX, 1.0, 0.0 + target.motionZ));
            }
            list.add(baseVec.add(0.0, 1.0, 0.0));
        }
        return list;
    }
}