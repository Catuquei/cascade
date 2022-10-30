package cascade.features.modules.movement;

import cascade.Cascade;
import cascade.features.modules.player.Freecam;
import cascade.util.entity.EntityUtil;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.player.PlayerUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import java.util.Arrays;
import java.util.List;

public class FastFall extends Module {

    public FastFall() {
        super("FastFall", Category.MOVEMENT, "reverse step");
    }

    Setting<Double> speed = register(new Setting("Speed", 3.0, 0.1, 10.0));
    Setting<Double> height = register(new Setting("Height", 10.0, 0.1, 90.0));
    Setting<Boolean> noLag = register(new Setting("NoLag", true));
    List<Block> incelBlocks = Arrays.asList(Blocks.BED, Blocks.SLIME_BLOCK); //maybe theres a way to get all blocks that make u bounce?

    @Override
    public void onUpdate() {
        if (fullNullCheck() || shouldReturn()) {
            return;
        }
        if (noLag.getValue() && Cascade.packetManager.getCaughtPPS()) {
            return;
        }
        RayTraceResult trace = mc.world.rayTraceBlocks(mc.player.getPositionVector(), new Vec3d(mc.player.posX, mc.player.posY - height.getValue(), mc.player.posZ), false, false, false);
        if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK && mc.world.getBlockState(new BlockPos(mc.player.posX, mc.player.posY - 0.1, mc.player.posZ)).getBlock() != incelBlocks) {
            mc.player.motionY =- speed.getValue();
        }
    }

    boolean shouldReturn() {
        return mc.player.capabilities.isFlying || mc.player.motionY > 0.0 ||
                (noLag.getValue() && Cascade.packetManager.getCaughtPPS()) ||
                mc.player.isElytraFlying() || PlayerUtil.isClipping() ||
                EntityUtil.isInLiquid() || mc.player.isOnLadder() ||
                mc.gameSettings.keyBindJump.isKeyDown() ||
                mc.player.noClip || !mc.player.onGround||
                mc.player.isEntityInsideOpaqueBlock() ||
                Freecam.getInstance().isEnabled();
    }
}