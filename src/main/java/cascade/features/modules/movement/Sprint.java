package cascade.features.modules.movement;

import cascade.event.events.MoveEvent;
import cascade.features.modules.Module;
import cascade.util.entity.EntityUtil;
import cascade.util.player.PlayerUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Sprint extends Module {

    public Sprint() {
        super("Sprint", Category.MOVEMENT, "Modifies sprinting");
    }

    @SubscribeEvent
    public void onSprint(MoveEvent e) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (e.getStage() == 1 && !EntityUtil.isMoving()) {
            e.setCanceled(true);
        }
    }

    @Override
    public void onUpdate() {
        if (check()) {
            mc.player.setSprinting(true);
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }

    boolean check() {
        return (mc.gameSettings.keyBindForward.isKeyDown()
                || mc.gameSettings.keyBindBack.isKeyDown()
                || mc.gameSettings.keyBindLeft.isKeyDown()
                || mc.gameSettings.keyBindRight.isKeyDown())
                && !(mc.player == null
                || mc.player.isSneaking()
                || mc.player.collidedHorizontally
                || mc.player.getFoodStats().getFoodLevel() <= 6.0f
                || PlayerUtil.isInLiquid());
    }
}