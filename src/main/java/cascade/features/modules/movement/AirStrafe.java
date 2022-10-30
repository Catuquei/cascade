package cascade.features.modules.movement;

import cascade.Cascade;
import cascade.event.events.MoveEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.mixin.mixins.accessor.ITimer;
import cascade.util.entity.EntityUtil;
import cascade.util.player.MovementUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AirStrafe extends Module {

    public AirStrafe() {
        super("AirStrafe", Category.MOVEMENT, "lets u strafe in air");
    }

    Setting<Boolean> noLiquid = register(new Setting("NoLiquid", true));
    Setting<Boolean> step = register(new Setting("Step", true));
    Setting<Float> height = register(new Setting("Height", 2.0f, 1.0f, 2.5f, v -> step.getValue()));

    Setting<Boolean> kbBoost = register(new Setting("KbBoost", true));
    Setting<Double> factor = register(new Setting("Factor", 6.0d, 0.1d, 20.0d, v -> kbBoost.getValue()));

    @Override
    public void onToggle() {
        if (mc.player != null && step.getValue()) {
            mc.player.stepHeight = 0.6f;
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent e) {
        if (isDisabled() || mc.player.isElytraFlying()) {
            return;
        }
        if (noLiquid.getValue() && EntityUtil.isInLiquid() || Cascade.packetManager.getCaughtPPS()) {
            return;
        }
        if (Cascade.moduleManager.isModuleEnabled("HoleSnap") || Cascade.moduleManager.isModuleEnabled("Freecam") || Cascade.moduleManager.isModuleEnabled("YPort") || Cascade.moduleManager.isModuleEnabled("Strafe")) {
            return;
        }
        if (step.getValue()) {
            MovementUtil.step(height.getValue());
        }
        MovementUtil.strafe(e, MovementUtil.getSpeed() * (shouldBoost() ? factor.getValue() / 5.0d : 1.0d));
    }

    boolean shouldBoost() {
        return kbBoost.getValue() &&
               Cascade.packetManager.getCaughtE() &&
               Cascade.packetManager.getPacketE().getStrength() == 6.0 &&
               !mc.player.onGround &&
               mc.gameSettings.keyBindJump.isKeyDown() &&
               mc.player.posY - Cascade.packetManager.getPacketE().posY >= -0.9 &&
               mc.player.getDistance(Cascade.packetManager.getPacketE().getX(), Cascade.packetManager.getPacketE().getY(), Cascade.packetManager.getPacketE().getZ()) <= 12.0;
    }
}