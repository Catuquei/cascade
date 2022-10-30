package cascade.manager;

import cascade.event.events.DisconnectEvent;
import cascade.event.events.GameLoopEvent;
import cascade.event.events.WorldClientEvent;
import cascade.features.Feature;
import cascade.util.misc.Timer;
import cascade.util.player.PhysicsUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PhysicsManager extends Feature {

    Timer timer = new Timer();
    boolean blocking;
    int delay;
    int times;

    @SubscribeEvent
    public void invoke(GameLoopEvent e) {
        if (mc.player == null) {
            times = 0;
            return;
        }

        if (times > 0 && timer.passedMs(delay)) {
            blocking = true;
            for (; times > 0; times--) {
                invokePhysics();
                if (delay != 0) {
                    break;
                }
            }

            blocking = false;
            timer.reset();
        }
    }

    @SubscribeEvent
    public void invoke(DisconnectEvent event) {
        times = 0;
    }


    @SubscribeEvent
    public void invoke(WorldClientEvent.Load event) {
        times = 0;
    }

    public void invokePhysics(int times, int delay) {
        if (!blocking) {
            this.times = times;
            this.delay = delay;
        }
    }

    /**
     * Invokes {@link Entity#onUpdate()} and
     * {@link EntityPlayerSP#onUpdateWalkingPlayer()} for
     * the player.
     */
    @SuppressWarnings("JavadocReference")
    public void invokePhysics() {
        PhysicsUtil.runPhysicsTick();
    }
}