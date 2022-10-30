package cascade.features.modules.movement;

import cascade.event.events.PacketEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.misc.Timer;
import cascade.util.player.MovementUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class Flight extends Module {

    public Flight() {
        super("Flight", Category.MOVEMENT, "M");
    }

    Setting<Mode> mode = register(new Setting("Mode", Mode.Const));
    enum Mode {Const, Normal}
    Setting<ForceGround> forceGround = register(new Setting("ForceGround", ForceGround.None));
    enum ForceGround {None, True, False}
    Setting<Double> hSpeed = register(new Setting("HSpeed", 1.0d, 0.2d, 10.0d, v -> mode.getValue() == Mode.Normal));
    Setting<Float> vSpeed = register(new Setting("VSpeed", 1.0f, 0.1f, 1.0f, v -> mode.getValue() == Mode.Normal));
    Setting<Integer> packets = register(new Setting("Packets", 15, 2, 32, v -> mode.getValue() == Mode.Normal));
    Setting <Color> c = register(new Setting("Color", new Color(120, 0, 255, 205), v -> mode.getValue() == Mode.Normal));
    Timer timer = new Timer();
    int flightTicks;
    double x;
    double y;
    double z;

    @Override
    public void onToggle() {
        mc.player.capabilities.isFlying = false;
        mc.player.noClip = false;
        flightTicks = 0;

    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (mode.getValue() == Mode.Const) {
            if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, -0.1, 0)).isEmpty()) {
                mc.player.motionY = 0;
                if (!forceGround.getValue().equals(ForceGround.None)) {
                    mc.player.onGround = forceGround.getValue().equals(ForceGround.True) ? true : false;
                }
                if (flightTicks > 40) {
                    mc.player.posY -= 0.032;
                    flightTicks = 0;
                } else {
                    flightTicks++;
                }
                if (mc.player.ticksExisted % 3 != 0) {
                    mc.player.setPosition(mc.player.posX, mc.player.posY += 1.0e-9, mc.player.posZ);
                }
            }
        } else {
            mc.player.capabilities.isFlying = true;
            mc.player.noClip = true;
            if (timer.passedMs(2550)) {
                timer.reset();
                return;
            }
            if (timer.passedMs(2500)) {
                return;
            }
            double[] dir = MovementUtil.strafe(hSpeed.getValue());
            mc.player.capabilities.setFlySpeed(vSpeed.getValue() / 10.0f);
            MovementUtil.setMotion(dir[1], mc.player.motionY, dir[0]);
            for(int i = 0; i < packets.getValue(); i++) {
                mc.getConnection().sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY, mc.player.posZ,mc.player.rotationYaw,mc.player.rotationPitch, false));
                mc.getConnection().sendPacket(new CPacketPlayer.PositionRotation(mc.player.prevPosX, mc.player.prevPosY + 0.05, mc.player.prevPosZ,mc.player.rotationYaw,mc.player.rotationPitch, true));
                mc.getConnection().sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY, mc.player.posZ,mc.player.rotationYaw,mc.player.rotationPitch, false));
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive e) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (e.getPacket() instanceof SPacketPlayerPosLook && mode.getValue() == Mode.Normal) {
            if(timer.passedMs(2500)) {
                return;
            }
            x = ((SPacketPlayerPosLook) e.getPacket()).x;
            y =((SPacketPlayerPosLook) e.getPacket()).z;
            z = ((SPacketPlayerPosLook) e.getPacket()).y;
            if(true) {
                ((SPacketPlayerPosLook) e.getPacket()).y = mc.player.posY;
                ((SPacketPlayerPosLook) e.getPacket()).x = mc.player.posX;
                ((SPacketPlayerPosLook) e.getPacket()).z = mc.player.posZ;
                ((SPacketPlayerPosLook) e.getPacket()).yaw = mc.player.rotationYaw;
                ((SPacketPlayerPosLook) e.getPacket()).pitch = mc.player.rotationPitch;
                mc.getConnection().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                mc.getConnection().sendPacket(new CPacketPlayer.Position(mc.player.prevPosX, mc.player.prevPosY + 0.05, mc.player.prevPosZ, true));
                mc.getConnection().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
            }
        }
    }
}