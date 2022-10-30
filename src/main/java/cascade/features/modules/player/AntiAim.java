package cascade.features.modules.player;

import cascade.Cascade;
import cascade.event.events.PacketEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.player.MovementUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiAim extends Module {

    public AntiAim() {
        super("AntiAim", Category.PLAYER, "ion kno");
    }

    Setting<Mode> mode = register(new Setting("Mode", Mode.Spin));
    Setting<Integer> spinSpeed = register(new Setting("SpinSpeed", 10, 0, 50, v -> mode.getValue() == Mode.Spin));
    Setting<Boolean> clientside = register(new Setting("ClientSide", false));
    int nextValue;

    @Override
    public void onUpdate() {
        nextValue += spinSpeed.getValue();
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send e) {
        if (isDisabled()) {
            return;
        }
        if (e.getPacket() instanceof CPacketPlayer && !mc.player.isHandActive() && !mc.gameSettings.keyBindUseItem.isKeyDown()) {
            if (mode.getValue() == Mode.Spin) {
                ((CPacketPlayer) e.getPacket()).yaw = nextValue;
                ((CPacketPlayer) e.getPacket()).pitch = nextValue;
            } else {
                double cos = Math.cos(Math.toRadians(mc.player.rotationYaw + 90.0f));
                double sin = Math.sin(Math.toRadians(mc.player.rotationYaw + 90.0f));
                ((CPacketPlayer) e.getPacket()).yaw = angleCalc();
                if (clientside.getValue()) {
                    mc.player.rotationYaw = angleCalc();
                }
            }
        }
    }

    float angleCalc() {
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;
        if (forward != 0.0 && strafe != 0.0) {
            if (strafe > 0.0) {
                yaw += ((forward > 0.0) ? -45 : 45);
                return yaw;
            }
            if (strafe < 0.0) {
                yaw += ((forward > 0.0) ? 45 : -45);
                return yaw;
            }
            if (forward > 0.0) {
                forward = 1.0;
            }
            if (forward < 0.0) {
                forward = -1.0;
            }
        }
        return mc.player.rotationYaw;
    }

    public enum Mode {
        Spin,
        FakeAngle
    }
}