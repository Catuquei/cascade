package cascade.features.modules.misc;

import cascade.event.events.PacketEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.player.InventoryUtil;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.tileentity.*;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static cascade.util.player.InventoryUtil.Hand.Both;
import static cascade.util.player.InventoryUtil.Hand.Main;

public class NoInteract extends Module {

    public NoInteract() {
        super("NoInteract", Category.MISC, "Prevents u from interacting with blocks");
    }

    Setting<Boolean> pickaxe = register(new Setting("Pickaxe", false));

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send e) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        try {
            if (e.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && !mc.player.isSneaking() && mc.gameSettings.keyBindUseItem.isKeyDown() && (InventoryUtil.heldItem(Items.EXPERIENCE_BOTTLE, Both) || InventoryUtil.heldItem(Items.GOLDEN_APPLE, Both) || InventoryUtil.heldItem(Items.CHORUS_FRUIT, Both) || InventoryUtil.heldItem(Items.BOW, Both) || InventoryUtil.heldItem(Items.WRITABLE_BOOK, Both) || InventoryUtil.heldItem(Items.WRITTEN_BOOK, Both) || InventoryUtil.heldItem(Items.POTIONITEM, Both) || (pickaxe.getValue() && InventoryUtil.heldItem(Items.DIAMOND_PICKAXE, Main)))) {
                for (TileEntity entity : mc.world.loadedTileEntityList) {
                    if (entity instanceof TileEntityEnderChest || entity instanceof TileEntityBeacon || entity instanceof TileEntityFurnace || entity instanceof TileEntityHopper || entity instanceof TileEntityChest) {
                        if (mc.objectMouseOver.getBlockPos().equals(entity.getPos())) {
                            e.setCanceled(true);
                            mc.getConnection().sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                        }
                    }
                }
                if (mc.world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock() == Blocks.ANVIL) {
                    e.setCanceled(true);
                    mc.getConnection().sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock e) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (mc.world.getBlockState(e.getPos()).getBlock() == Blocks.ANVIL || mc.world.getBlockState(e.getPos()).getBlock() == Blocks.ENDER_CHEST) {
            if (!mc.player.isSneaking() && mc.gameSettings.keyBindUseItem.isKeyDown() && (InventoryUtil.heldItem(Items.EXPERIENCE_BOTTLE, Both) || InventoryUtil.heldItem(Items.GOLDEN_APPLE, Both) || InventoryUtil.heldItem(Items.CHORUS_FRUIT, Both) || InventoryUtil.heldItem(Items.BOW, Both) || InventoryUtil.heldItem(Items.WRITABLE_BOOK, Both) || InventoryUtil.heldItem(Items.WRITTEN_BOOK, Both) || InventoryUtil.heldItem(Items.POTIONITEM, Both) || (pickaxe.getValue() && InventoryUtil.heldItem(Items.DIAMOND_PICKAXE, Main)))) {
                e.setCanceled(true);
                mc.getConnection().sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            }
        }
    }
}