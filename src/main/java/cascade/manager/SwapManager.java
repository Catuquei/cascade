package cascade.manager;

import cascade.event.events.PacketEvent;
import cascade.features.Feature;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SwapManager extends Feature {

    boolean hasSwapped;
    int ticks;

    public SwapManager() {
        hasSwapped = false;
        ticks = 0;
    }

    public void load() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void unload() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent e) {
        if (fullNullCheck()) {
            return;
        }
        if (hasSwapped == true) {
            ticks++;
            if (ticks >= 10) { //0.5s is default swap cooldown on most of the servers which is 10 ticks
                hasSwapped = false;
                ticks = 0;
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send e) {
        if (fullNullCheck()) {
            return;
        }
        //todo theres gotta be some mixin for that
        if (e.getPacket() instanceof CPacketHeldItemChange) {
            hasSwapped = true;
        }
    }

    public boolean hasSwapped() {
        return hasSwapped;
    }
}