package cascade.util.player;

import cascade.util.Util;
import net.minecraft.block.Block;
import net.minecraft.init.MobEffects;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.potion.Potion;

public class ItemUtil implements Util {


    /**
     * switch/swap
     **/
    public static void silentSwap(int slot) {
        if (slot != -1) {
            mc.getConnection().sendPacket(new CPacketHeldItemChange(slot));
        }
    }

    public static void silentSwapRecover(int slot) {
        mc.getConnection().sendPacket(new CPacketHeldItemChange(slot));
        mc.playerController.updateController();
    }

    public static void bypassSwap(int slot) {
        if (slot != -1) {
            mc.playerController.pickItem(slot);
        }
    }

    public static void normalSwap(int slot) {
        if (slot != -1 && mc.player.inventory.currentItem != slot) {
            mc.player.inventory.currentItem = slot;
            mc.playerController.updateController();
        }
    }

    public static int getItemFromHotbar(Item item) {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == item) {
                slot = i;
            }
        }
        return slot;
    }

    public static int getBlockFromHotbar(Block block) {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Item.getItemFromBlock(block)) {
                slot = i;
            }
        }
        return slot;
    }

    public static boolean shouldAntiWeakness() {
        if (mc.player.isPotionActive(MobEffects.WEAKNESS) && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword || mc.player.getHeldItemMainhand().getItem() instanceof ItemAxe || mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe)) {
            return true;
        }
        return false;
    }
}