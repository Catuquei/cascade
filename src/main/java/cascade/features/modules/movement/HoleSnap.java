package cascade.features.modules.movement;

import cascade.Cascade;
import cascade.event.events.PacketEvent;
import cascade.event.events.Render3DEvent;
import cascade.features.command.Command;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.mixin.mixins.accessor.ITimer;
import cascade.util.player.HoleUtil;
import cascade.util.misc.Timer;
import cascade.util.entity.EntityUtil;
import cascade.util.player.MovementUtil;
import cascade.util.player.PhysicsUtil;
import cascade.util.player.RotationUtil;
import cascade.util.render.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Comparator;

public class HoleSnap extends Module {

    public HoleSnap() {
        super("HoleSnap", Category.MOVEMENT, "drags u to the nearest hole");
    }

    public Setting<Float> range = register(new Setting("Range", 4.5f, 0.1f, 12.0f));
    Setting<Float> factor = register(new Setting("Factor", 2.6f, 0.1f, 15.0f));
    Setting<Boolean> step = register(new Setting("Step", true));
    Setting<Float> height = register(new Setting("Height", 2.0f, 0.1f, 2.0f, v -> step.getValue()));
    Setting<Boolean> render = register(new Setting("Render", true));
    Timer timer = new Timer();
    HoleUtil.Hole holes;

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }
        timer.reset();
        holes = null;
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) {
            return;
        }

        timer.reset();
        holes = null;
        if (step.getValue()) {
            mc.player.stepHeight = 0.6f;
        }
        if (((ITimer)mc.timer).getTickLength() != 50) {
            Cascade.timerManager.reset();
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (EntityUtil.isInLiquid()) {
            disable();
            return;
        }
        holes = RotationUtil.getTargetHoleVec3D(range.getValue());
        if (holes == null) {
            disable();
            return;
        }
        if (timer.passedMs(500)) {
            disable();
            return;
        }
        if (HoleUtil.isObbyHole(RotationUtil.getPlayerPos()) || HoleUtil.isBedrockHoles(RotationUtil.getPlayerPos())) {
            disable();
            return;
        }
        if (mc.world.getBlockState(holes.pos1).getBlock() == Blocks.AIR) {
            if (step.getValue()) {
                MovementUtil.step(height.getValue());
            }
            Cascade.timerManager.set(factor.getValue());
            Vec3d playerPos = mc.player.getPositionVector();
            Vec3d targetPos = new Vec3d((double)holes.pos1.getX() + 0.5, mc.player.posY, (double)holes.pos1.getZ() + 0.5);
            double yawRad = Math.toRadians(RotationUtil.getRotationTo(playerPos, targetPos).x);
            double dist = playerPos.distanceTo(targetPos);
            double speed = mc.player.onGround ? -Math.min(0.2805, dist / 2.0) : -EntityUtil.getMaxSpeed() + 0.02;
            mc.player.motionX = -Math.sin(yawRad) * speed;
            mc.player.motionZ = Math.cos(yawRad) * speed;
        } else {
            disable();
            return;
        }
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent e) {
        if (holes != null && render.getValue() && !fullNullCheck()) {
            /*RenderUtil.prepare();
            Vec3d targetPos = new Vec3d((double)holes.pos1.getX() + 0.5, (double)holes.pos1.getY(), (double)holes.pos1.getZ() + 0.5);
            Vec3d startPos = RenderUtil.updateToCamera(mc.player.getPositionVector());
            Vec3d endPos = RenderUtil.updateToCamera(targetPos);
            RenderUtil.builder5 = RenderUtil.tessellator5.getBuffer();
            RenderUtil.builder5.begin(1, DefaultVertexFormats.POSITION_COLOR);
            RenderUtil.addBuilderVertex(RenderUtil.builder5, startPos.x, startPos.y, startPos.z, Color.WHITE);
            RenderUtil.addBuilderVertex(RenderUtil.builder5, endPos.x, endPos.y, endPos.z, Color.WHITE);
            RenderUtil.tessellator5.draw();
            RenderUtil.release();*/
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive e) {
        if (isDisabled()) {
            return;
        }
        if (e.getPacket() instanceof SPacketPlayerPosLook) {
            disable();
            return;
        }
    }
}