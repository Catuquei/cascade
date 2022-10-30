package cascade.features.modules.movement;

import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.entity.EntityUtil;
import cascade.util.player.HoleUtil;
import cascade.util.player.MovementUtil;
import cascade.util.player.PlayerUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;

public class AutoCenter extends Module {

    public AutoCenter() {
        super("AutoCenter", Category.MOVEMENT, "zaza");
    }

    Setting<Boolean> always = register(new Setting("Always", true));
    Setting<Mode> mode = register(new Setting("Mode", Mode.Instant));
    enum Mode {Instant, Motion, Timer}
    Setting<Float> factor = register(new Setting("Factor", 2.6f, 0.1f, 15.0f, v -> mode.getValue() == Mode.Timer));

    @Override
    public void onEnable() {
        if (fullNullCheck() || always.getValue()) {
            return;
        }
        try {
            doCenter();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        disable();
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck() || !always.getValue()) {
            return;
        }
        try {
            doCenter();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //todo add isCentered check (if (isCentered) { return; }
    }

    void doCenter() {
        if (!mc.player.onGround || mc.player.noClip || PlayerUtil.isInLiquid() || PlayerUtil.isChestBelow())  {
            return;
        }
        Vec3d pos = null;
        if (HoleUtil.is2x1(mc.player.getPosition())) {
            pos = getCenter(false);
        }
        if (HoleUtil.is2x2(mc.player.getPosition())) {
            pos = getCenter(true);
        }
        switch (mode.getValue()) {
            case Instant: {
                MovementUtil.setMotion(0, 0, 0);
                mc.getConnection().sendPacket(new CPacketPlayer.Position(pos.x,  mc.player.posY, pos.z, true));
                mc.player.setPosition(pos.x, mc.player.posY, pos.z);
                break;
            }
            case Motion: {
                MovementUtil.setMotion((pos.x - mc.player.posX) / 2, mc.player.motionY, (pos.z - mc.player.posZ) / 2);
                break;
            }
            case Timer: {

                break;
            }
        }
    }

    public static Vec3d getCenter(boolean is2x2) {
        double x, y, z;
        if (is2x2) {
            x = Math.floor(mc.player.posX) + 0.5D;
            y = Math.floor(mc.player.posY);
            z = Math.floor(mc.player.posZ) + 0.5D;
        } else {
            x = Math.floor(mc.player.posX) + 0.5D;
            y = Math.floor(mc.player.posY);
            z = Math.floor(mc.player.posZ) + 0.5D;
        }
        return new Vec3d(x, y, z);
    }
}