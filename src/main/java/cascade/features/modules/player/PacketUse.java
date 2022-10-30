package cascade.features.modules.player;

import cascade.event.events.PacketEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import net.minecraft.item.ItemBucketMilk;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PacketUse extends Module {

    public PacketUse() {
        super("PacketUse", Category.PLAYER, "Exploits that make you fat");
        INSTANCE = this;
    }

    public Setting<Boolean> potions = register(new Setting("Potions", true));
    public Setting<Mode> mode = register(new Setting("Mode", Mode.Packet));
    public enum Mode {NoDelay, Update, Packet}
    public Setting<Float> speed = register(new Setting("Speed", 15.0f, 1.0f, 25.0f));
    public Setting<Boolean> cancel = register(new Setting("Cancel", false));
    Setting<Integer> runs = register(new Setting("Runs", 32, 1, 64, v -> mode.getValue() == Mode.Packet));
    static PacketUse INSTANCE;

    public static PacketUse getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PacketUse();
        }
        return INSTANCE;
    }

    boolean isValid(ItemStack stack) {
        return stack != null && mc.player.isHandActive() && (stack.getItem() instanceof ItemFood || (stack.getItem() instanceof ItemPotion && potions.getValue()) || stack.getItem() instanceof ItemBucketMilk);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send e) {
        if (isDisabled() || fullNullCheck()) {
            return;
        }
        if (cancel.getValue() && (mc.player.getActiveItemStack().getItem() instanceof ItemFood || mc.player.getActiveItemStack().getItem() instanceof ItemBucketMilk || (mc.player.getActiveItemStack().getItem() instanceof ItemPotion && potions.getValue())) && e.getPacket() instanceof CPacketPlayerDigging) {
            CPacketPlayerDigging packet = e.getPacket();
            if (packet.getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM && packet.getFacing() == EnumFacing.DOWN && packet.getPosition().equals(BlockPos.ORIGIN)) {
                e.setCanceled(true);
            }
        }

        if (mode.getValue() == Mode.Update && isValid(mc.player.getHeldItem(mc.player.getActiveHand())) && e.getPacket() instanceof  CPacketPlayerTryUseItem) {
            mc.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (mode.getValue() == Mode.Update && isValid(mc.player.getActiveItemStack())) {
            EnumHand hand = mc.player.getActiveHand();
            if (hand == null) {
                hand = mc.player.getHeldItemOffhand().equals(mc.player.getActiveItemStack()) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
            }

            mc.getConnection().sendPacket(new CPacketPlayerTryUseItem(hand));
        } else if (mode.getValue() == Mode.Packet && isValid(mc.player.getActiveItemStack()) && mc.player.getItemInUseMaxCount() > speed.getValue() - 1 && speed.getValue() < 25) {
            for (int i = 0; i < runs.getValue(); i++) {
                mc.getConnection().sendPacket(new CPacketPlayer(mc.player.onGround));
            }

            mc.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            mc.player.stopActiveHand();
        }
    }
}