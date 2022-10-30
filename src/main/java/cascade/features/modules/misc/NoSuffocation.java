package cascade.features.modules.misc;

import cascade.event.events.PacketEvent;
import cascade.features.command.Command;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.player.InventoryUtil;
import cascade.util.player.MovementUtil;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoSuffocation extends Module {

    public NoSuffocation() {
        super("NoSuffocation", Category.MISC, "");
    }

    Setting<Integer> resyncType = register(new Setting("ResyncType(dev)", 1, 1, 3));

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send e) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (e.getPacket() instanceof CPacketPlayer && checkCollisionBox()) {
            e.setCanceled(true);
            MovementUtil.setMotion(0d, 0d, 0d);
            mc.player.setVelocity(0d, 0d, 0d);
        }
    }

    boolean checkCollisionBox() {
        if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(0.0, 0.0, 0.0)).isEmpty()) {
            return true;
        }
        return !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, 2.0, 0.0).contract(0.0, 1.99, 0.0)).isEmpty();
    }
}