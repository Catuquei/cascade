package cascade.manager;

import cascade.features.Feature;
import cascade.features.command.Command;
import cascade.features.modules.core.TimingManager;
import cascade.mixin.mixins.accessor.ITimer;
import cascade.util.misc.Timer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TimerManager extends Feature {

    float timer;
    boolean flagged;
    Timer flagTimer;

    public TimerManager() {
        timer = 1.0f;
        flagged = false;
        flagTimer = new Timer();
    }

    public void load() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent e) {
        if (fullNullCheck()) {
            return;
        }
        if (flagged == true && flagTimer.passedMs((long)TimingManager.getInstance().getTimer())) {
            flagged = false;
        }
        if (mc.timer.tickLength != 50.0f) {
            flagged = true;
            flagTimer.reset();
        }
    }

    public void unload() {
        MinecraftForge.EVENT_BUS.unregister(this);
        timer = 1.0f;
        ((ITimer)mc.timer).setTickLength(50.0f);
    }

    public void set(float timer) {
        if (timer > 0.0f) {
            ((ITimer)mc.timer).setTickLength(50.0f / timer);
        }
    }

    public float getTimer() {
        return timer;
    }

    public boolean isFlagged() {
        return flagged;
    }

    @Override
    public void reset() {
        timer = 1.0f;
        ((ITimer)mc.timer).setTickLength(50.0f);
    }
}