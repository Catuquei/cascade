package cascade.manager;

import cascade.util.Util;
import net.minecraft.network.play.client.CPacketHeldItemChange;

public class InventoryManager implements Util {

    public enum Mode {Silent, Normal}
    private int recoverySlot = -1;
    public int currentPlayerItem;

    public void update() {
        if (recoverySlot != -1) {
            mc.getConnection().sendPacket(new CPacketHeldItemChange(recoverySlot == 8 ? 7 : recoverySlot + 1));
            mc.getConnection().sendPacket(new CPacketHeldItemChange(recoverySlot));
            mc.player.inventory.currentItem = recoverySlot;
            int i = mc.player.inventory.currentItem;
            if (i != currentPlayerItem) {
                currentPlayerItem = i;
                mc.getConnection().sendPacket(new CPacketHeldItemChange(currentPlayerItem));
            }
            recoverySlot = -1;
        }
    }

    public void recoverSilent(int slot) {
        recoverySlot = slot;
    }
}