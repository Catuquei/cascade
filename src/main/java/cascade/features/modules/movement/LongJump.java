package cascade.features.modules.movement;

import cascade.Cascade;
import cascade.event.events.MoveEvent;
import cascade.event.events.PacketEvent;
import cascade.event.events.UpdateWalkingPlayerEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.mixin.mixins.accessor.ITimer;
import cascade.util.entity.EntityUtil;
import cascade.util.misc.MathUtil;
import cascade.util.player.HoleUtil;
import cascade.util.player.MovementUtil;
import cascade.util.player.PlayerUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LongJump extends Module {

    public LongJump() {
        super("LongJump", Category.MOVEMENT, "Long jump dude");
    }

    Setting<Boolean> useTimer = register(new Setting("UseTimer", true));
    Setting<Boolean> noLag = register(new Setting("NoLag", true));
    Setting<Boolean> noLiquid = register(new Setting("NoLiquid", true));
    Setting<Boolean> step = register(new Setting("Step", true));
    Setting<Boolean> always = register(new Setting("Always", false));
    Setting<Float> factor = register(new Setting("Factor", 4.6f, 0.0f, 100.0f));
    Setting<Boolean> lowHop = register(new Setting("LowHop", false));
    Setting<Boolean> lagDisable = register(new Setting("LagDisable", true));
    int groundTicks;
    double distance;
    double speed;
    int airTicks;
    int stage;

    @Override
    public void onEnable() {
        if (mc.player != null) {
            distance = MovementUtil.getDistanceXZ();
            speed = MovementUtil.getSpeed();
        }
        groundTicks = 0;
        airTicks = 0;
        stage = 0;
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) {
            return;
        }
        if (step.getValue()) {
            mc.player.stepHeight = 0.6f;
        }
        if (((ITimer)mc.timer).getTickLength() != 50f) {
            Cascade.timerManager.reset();
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent e) {
        if (isDisabled() || e.isCanceled()) {
            return;
        }
        if (noLiquid.getValue() && EntityUtil.isInLiquid()) {
            return;
        }
        if (noLag.getValue() && Cascade.packetManager.getCaughtPPS()) {
            return;
        }
        if (always.getValue() && !Cascade.packetManager.isValid()) {
            return;
        }
        if (useTimer.getValue()) {
            Cascade.timerManager.set(1.0888f);
        }
        if (step.getValue()) {
            MovementUtil.step(2.0f);
        }
        if (HoleUtil.isInHole(mc.player.getPosition()) || PlayerUtil.isBurrowed()) {
            return;
        }
        if (mc.player.moveStrafing <= 0.0f && mc.player.moveForward <= 0.0f) {
            stage = 1;
        }

        if (MathUtil.round(mc.player.posY - (int) mc.player.posY, 3) == MathUtil.round(0.943, 3)) {
            mc.player.motionY -= 0.03;
            e.setY(e.getY() - 0.03);
        }

        if (stage == 1 && MovementUtil.isMoving()) {
            stage = 2;
            speed = factor.getValue() * MovementUtil.getSpeed() - 0.01;
        } else if (stage == 2) {
            stage = 3;
            if (!EntityUtil.isInLiquid() && !mc.player.isInWeb && mc.player.onGround) {
                if (!lowHop.getValue()) {
                    mc.player.motionY = 0.424;
                }
                e.setY(0.424);
            }
            speed = speed * 2.149802;
        } else if (stage == 3) {
            stage = 4;
            double difference = 0.66D * (distance - MovementUtil.getSpeed());
            speed = (distance - difference);
        } else {
            if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).size() > 0 || mc.player.collidedVertically) {
                stage = 1;
            }
            speed = distance - distance / 159.0;
        }

        speed = Math.max(speed, MovementUtil.getSpeed());
        MovementUtil.strafe(e, speed);

        float moveForward = mc.player.movementInput.moveForward;
        float moveStrafe = mc.player.movementInput.moveStrafe;
        float rotationYaw = mc.player.rotationYaw;
        if (moveForward == 0.0f && moveStrafe == 0.0f) {
            e.setX(0.0);
            e.setZ(0.0);
        } else {
            if (moveForward != 0.0f) {
                if (moveStrafe >= 1.0f) {
                    rotationYaw += ((moveForward > 0.0f) ? -45 : 45);
                    moveStrafe = 0.0f;
                } else {
                    if (moveStrafe <= -1.0f) {
                        rotationYaw += ((moveForward > 0.0f) ? 45 : -45);
                        moveStrafe = 0.0f;
                    }
                }
                if (moveForward > 0.0f) {
                    moveForward = 1.0f;
                } else if (moveForward < 0.0f) {
                    moveForward = -1.0f;
                }
            }
        }

        double cos = Math.cos(Math.toRadians(rotationYaw + 90.0f));
        double sin = Math.sin(Math.toRadians(rotationYaw + 90.0f));

        e.setX(moveForward * speed * cos + moveStrafe * speed * sin);
        e.setZ(moveForward * speed * sin - moveStrafe * speed * cos);
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent e) {
        if (isDisabled()) {
            return;
        }
        if (e.getStage() == 0) {
            distance = MovementUtil.getDistanceXZ();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive e) {
        if (isDisabled()) {
            return;
        }
        if (e.getPacket() instanceof SPacketPlayerPosLook) {
            groundTicks = 0;
            speed = 0.0;
            airTicks = 0;
            stage = 0;
            if (lagDisable.getValue()) {
                disable();
                return;
            }
        }
    }
}
/*
is on edge
mc.player.onGround && !mc.player.isSneaking() && mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, 0.0, 0.0).shrink(0.001)).isEmpty()
true
}
 */