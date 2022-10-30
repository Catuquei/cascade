package cascade.features.modules.movement;

import cascade.Cascade;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.mixin.mixins.accessor.ITimer;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;

public class AntiVoid extends Module {

    public AntiVoid() {
        super("AntiVoid", Category.MOVEMENT, "Prevents u from falling in the void");
    }

    Setting<Mode> mode = register(new Setting("Mode", Mode.MotionStop));
    enum Mode {MotionStop, Motion, Timer, Glide, Packet}

    Setting<Integer> distance = register(new Setting("Distance", 10, 1, 256));
    Setting<Integer> height = register(new Setting("Height", 4, 0, 10, v -> mode.getValue() == Mode.Packet));
    Setting<Float> speed = register(new Setting("Speed", 5.0f, 0.1f, 10.0f, v -> mode.getValue() == Mode.Motion || mode.getValue() == Mode.Glide));
    Setting<Float> timer = register(new Setting("Timer", 8.0f, 0.1f, 10.0f, v -> mode.getValue() == Mode.Timer));

    @Override
    public void onToggle() {
        if (fullNullCheck()) {
            return;
        }
        if (mode.getValue() == Mode.Timer && ((ITimer)mc.timer).getTickLength() != 50) {
            Cascade.timerManager.reset();
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (mc.player.noClip || mc.player.posY > distance.getValue() || mc.player.isRiding()) {
            return;
        }
        RayTraceResult trace = mc.world.rayTraceBlocks(mc.player.getPositionVector(), new Vec3d(mc.player.posX, 0, mc.player.posZ), false, false, false);
        if (trace == null || trace.typeOfHit != RayTraceResult.Type.BLOCK) {
            switch (mode.getValue()) {
                case MotionStop: {
                    mc.player.setVelocity(0, 0, 0);
                    mc.player.motionY = 0;
                    break;
                }
                case Motion: {
                    mc.player.motionY = speed.getValue();
                    break;
                }
                case Timer: {
                    Cascade.timerManager.set(timer.getValue());
                    break;
                }
                case Glide: {
                    mc.player.motionY *= speed.getValue();
                    break;
                }
                case Packet: {
                    mc.getConnection().sendPacket(new CPacketPlayer.Position(mc.player.posX, height.getValue(), mc.player.posZ, mc.player.onGround));
                    break;
                }
            }
        }
    }
}