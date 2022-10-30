package cascade.features.modules.movement;

import cascade.Cascade;
import cascade.event.events.PacketEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.mixin.mixins.accessor.ITimer;
import cascade.util.entity.EntityUtil;
import cascade.util.misc.Timer;
import cascade.util.player.MovementUtil;
import cascade.util.player.PlayerUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class YPort extends Module {

    public YPort() {
        super("YPort", Category.MOVEMENT, "");
        INSTANCE = this;
    }

    Setting<Boolean> noLag = register(new Setting("NoLag", true));
    Setting<Boolean> step = register(new Setting("Step", true));
    Setting<Double> speed = register(new Setting("Speed", 0.0f, 0.0f, 1.0f));
    Setting<Integer> upSpeed = register(new Setting("UpSpeed", 1, 1, 500));
    Setting<Integer> downSpeed = register(new Setting("DownSpeed", 1, 1, 500));
    Setting<Double> height = register(new Setting("MaxHeight", 3.0d, 1.0d, 5.0d));
    Timer upTimer = new Timer();
    Timer downTimer = new Timer();
    static YPort INSTANCE;

    public static YPort getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new YPort();
        }
        return INSTANCE;
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) {
            return;
        }
        if (step.getValue()) {
            mc.player.stepHeight = 0.6f;
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }

        if (MovementUtil.isMoving() && !PlayerUtil.inLiquid() && Strafe.getInstance().isDisabled()) {
            if (noLag.getValue() && Cascade.packetManager.getCaughtPPS()) {
                return;
            }
            if (step.getValue()) {
                MovementUtil.step(2.0f);
            }
            if (mc.player.onGround) {
                if (upTimer.passedMs(upSpeed.getValue())) {
                    if (!mc.player.collidedHorizontally) {
                        mc.player.jump();
                        upTimer.reset();
                    }
                    MovementUtil.setMoveSpeed(MovementUtil.getSpeed() + speed.getValue());
                }
            } else if ((double)mc.player.fallDistance <= height.getValue() && downTimer.passedMs(downSpeed.getValue())) {
                mc.player.motionY -= 3.0d;
                downTimer.reset();
            }
        }
    }
}