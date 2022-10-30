package cascade.features.modules.movement;

import cascade.Cascade;
import cascade.features.modules.Module;
import cascade.features.modules.player.Freecam;
import cascade.features.setting.Setting;
import cascade.util.misc.MathUtil;
import cascade.util.misc.Timer;
import cascade.util.player.MovementUtil;
import cascade.util.player.PhysicsUtil;
import cascade.util.player.PlayerUtil;
import com.mojang.realmsclient.gui.ChatFormatting;

public class TickBoost extends Module {

    public TickBoost() {
        super("TickBoost", Category.MOVEMENT, "");
    }

    //Setting<Page> page = register(new Setting("Page", Page.Normal));
    enum Page {Normal, HoleOnly}
    Setting<Integer> factor = register(new Setting("factor", 4, 1, 16));
    Setting<Boolean> doubleTap = register(new Setting("DoubleTap", true));
    Setting<Integer> delay = register(new Setting("Delay", 180, 0, 500));
    Setting<Double> cooldown = register(new Setting("Cooldown", 3.2d, 1.0d, 8.0d));

    //Setting<Integer> holeFactor = register(new Setting("HoleFactor", 8, 1, 24, v -> page.getValue() == Page.Normal));
    //Setting<Double> holeCooldown = register(new Setting("HoleCooldown", 8.0d, 1.0d, 16.0d, v -> page.getValue() == Page.Normal));
    Timer doubleTapTimer = new Timer();
    Timer cooldownTimer = new Timer();
    int runs = 0;

    @Override
    public String getDisplayInfo() {
        if (cooldownTimer != null) {
            return (String.valueOf(cooldownTimer.passedMs((int)(cooldown.getValue() * 1000.0d)) ? ChatFormatting.GREEN : ChatFormatting.RED) + MathUtil.round((cooldownTimer.getPassedTimeMs() / 1000.0d), 1));
        }
        return null;
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck() || shouldReturn()) {
            return;
        }
        if (Cascade.timerManager.isFlagged() || Cascade.packetManager.getCaughtPPS()) {
            cooldownTimer.reset();
        }
        if (cooldownTimer.passedMs((int)(cooldown.getValue() * 1000.0d))) {
            if (MovementUtil.isMoving()) {
                MovementUtil.strafe(MovementUtil.getSpeed());
                if (!doubleTap.getValue()) {
                    runTicks();
                } else {
                    if (!doubleTapTimer.passedMs(5)) {
                        runTicks();
                    }
                    if (doubleTapTimer.passedMs(delay.getValue())) {
                        runTicks();
                    }
                }
                cooldownTimer.reset();
            }
        }
    }

    @Override
    public void onToggle() {
        doubleTapTimer.reset();
        cooldownTimer.reset();
        runs = 0;
    }

    void runTicks() {
        if (runs < factor.getValue()) {
            runs++;
            PhysicsUtil.runPhysicsTick();
        } else {
            runs = 0;
        }
    }

    boolean shouldReturn() {
        return Cascade.serverManager.isServerNotResponding(1050) || PlayerUtil.isClipping() || PlayerUtil.isInLiquid() || Freecam.getInstance().isEnabled() || Cascade.moduleManager.isModuleEnabled("PacketFly");
    }
}