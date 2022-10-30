package cascade.features.modules.player;

import cascade.Cascade;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.misc.Timer;
import cascade.util.player.InventoryUtil;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;

import java.util.concurrent.TimeUnit;

import static cascade.util.player.InventoryUtil.Hand.Both;
import static cascade.util.player.InventoryUtil.Hand.Main;

public class FastUse extends Module {

    public FastUse() {
        super("FastUse", Module.Category.PLAYER, "fast use,");
    }

    Setting<Page> page = register(new Setting("Page", Page.XP));
    enum Page {XP, Blocks}

    Setting<Boolean> packet = register(new Setting("Packet", false, v -> page.getValue() == Page.XP));
    Setting<Integer> runs = register(new Setting("Runs", 8, 0, 16, v -> page.getValue() == Page.XP && packet.getValue()));
    Setting<Integer> delay = register(new Setting("Delay", 250, 0, 1000, v -> page.getValue() == Page.XP && packet.getValue()));

    Setting<Boolean> fastPlace = register(new Setting("FastPlace", false, v -> page.getValue() == Page.Blocks));

    Timer timer = new Timer();
    boolean sentPackets;

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }

        if (InventoryUtil.heldItem(Items.EXPERIENCE_BOTTLE, Both) && mc.gameSettings.keyBindUseItem.isKeyDown()) {
            mc.rightClickDelayTimer = 0;
            if (sentPackets && timer.passedMs(delay.getValue())) {
                sentPackets = false;
                timer.reset();
            }
            if (packet.getValue() && !sentPackets) {
                try {
                    for (int i = 1; i < runs.getValue(); i++) {
                        mc.getConnection().sendPacket(new CPacketPlayerTryUseItem(InventoryUtil.heldItem(Items.EXPERIENCE_BOTTLE, Main) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND));
                        sentPackets = true;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (fastPlace.getValue()) {
            mc.rightClickDelayTimer = 0;
            mc.playerController.blockHitDelay = 0;
        }
    }
}