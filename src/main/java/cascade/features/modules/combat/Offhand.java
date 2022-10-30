package cascade.features.modules.combat;

import cascade.Cascade;
import cascade.event.events.PacketEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.entity.EntityUtil;
import cascade.util.misc.Timer;
import cascade.util.player.InventoryUtil;
import cascade.util.player.TargetUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static cascade.util.player.InventoryUtil.Hand.Main;

public class Offhand extends Module {

    public Offhand() {
        super("Offhand", Category.COMBAT, "");
    }

    Setting<Mode> mode = register(new Setting("Mode", Mode.Crystal));
    enum Mode {Totem, Crystal, Gapple}
    Setting<Boolean> closeGUI = register(new Setting("CloseGUI", false));
    Setting<Float> totemHealth = register(new Setting("TotemHP", 20.0f, 0.1f, 36.0f, v -> mode.getValue() != Mode.Totem));
    Setting<Float> totemHoleHealth = register(new Setting("TotemHoleHP", 13.0f, 0.1f, 36.0f, v -> mode.getValue() != Mode.Totem));
    Setting<Float> fallDistance = register(new Setting("FallDistance", 40.0f, 0.1f, 90.0f, v -> mode.getValue() != Mode.Totem));
    //Setting<Float> playerRange = register(new Setting("PlayerRange", 35.0f, 0.1f, 100.0f, v -> mode.getValue() != Mode.Totem));
    Setting<Boolean> totemOnLag = register(new Setting("TotemOnLag", false, v -> mode.getValue() != Mode.Totem));
    Setting<Integer> lagTime = register(new Setting("LagTime", 1000, 500, 2000, v -> mode.getValue() != Mode.Totem && totemOnLag.getValue()));
    Setting<Boolean> totemElytra = register(new Setting("TotemElytra", true, v -> mode.getValue() != Mode.Totem));

    Setting<Boolean> gapSword = register(new Setting("GapSword", true, v ->  mode.getValue() != Mode.Gapple));
    Setting<Boolean> gapPickaxe = register(new Setting("GapPickaxe", true, v -> mode.getValue() != Mode.Gapple));
    Setting<Boolean> noWaste = register(new Setting("NoWaste", true, v -> mode.getValue() != Mode.Gapple && (gapPickaxe.getValue() || gapSword.getValue())));
    Setting<Boolean> forceGap = register(new Setting("ForceGap", true, v -> mode.getValue() != Mode.Gapple && (gapPickaxe.getValue() || gapSword.getValue())));
    //Setting<Boolean> runSwap = register(new Setting("RunSwap", true, v -> mode.getValue() != Mode.Gapple && (gapPickaxe.getValue() || gapSword.getValue())));
    //Setting<Boolean> mainhandGap = register(new Setting("MainhandGap", true, v -> mode.getValue() != Mode.Gapple && (gapPickaxe.getValue() || gapSword.getValue()) && runSwap.getValue()));
    //Setting<Priority> priority = register(new Setting("Priority", Priority.Gap, v -> mode.getValue() == Mode.Gapple || gapSword.getValue()));
    EntityPlayer nearestEnemy;
    enum Priority {Gap, EGap}

    @Override
    public String getDisplayInfo() {
        return String.valueOf(getSize(mode.getValue() == Mode.Totem ? Items.TOTEM_OF_UNDYING : mode.getValue() == Mode.Crystal ? Items.END_CRYSTAL : Items.GOLDEN_APPLE));
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        Item i = getItemType();
        if (mc.player.getHeldItemOffhand().getItem() != i) {
            if (mc.currentScreen instanceof GuiContainer && closeGUI.getValue()) {
                mc.player.closeScreen();
            }
            if ((mc.currentScreen instanceof GuiContainer && !closeGUI.getValue()) || mc.currentScreen instanceof GuiInventory) {
                return;
            }
            int l_Slot = GetItemSlot(i);
            if (l_Slot != -1) {
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_Slot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_Slot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.updateController();
            }
        }
        nearestEnemy = TargetUtil.getTarget(32.0);
        if (nearestEnemy != null) {

        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send e) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (noWaste.getValue() && mode.getValue() != Mode.Gapple) {
            if (e.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && ((gapSword.getValue() && InventoryUtil.heldItem(Items.DIAMOND_SWORD, Main)) || gapPickaxe.getValue() && InventoryUtil.heldItem(Items.DIAMOND_PICKAXE, Main)) && mc.gameSettings.keyBindUseItem.isKeyDown()) {
                CPacketPlayerTryUseItemOnBlock p = e.getPacket();
                if (p.hand == EnumHand.OFF_HAND) {
                    e.setCanceled(true);
                }
            }
        }
    }

    Item getItemType() {
        switch (mode.getValue()) {
            case Totem: {
                if (shouldGapSword()) {
                    return Items.GOLDEN_APPLE;
                }

                if (shouldGapPickaxe()) {
                    return Items.GOLDEN_APPLE;
                }

                return Items.TOTEM_OF_UNDYING;
            }
            case Crystal: {

                if (shouldSwitchHP() && (forceGap.getValue() && !shouldGapPickaxe() && !shouldGapSword())) {
                    return Items.TOTEM_OF_UNDYING;
                }

                if (shouldSwitchHoleHP() && (forceGap.getValue() && !shouldGapPickaxe() && !shouldGapSword())) {
                    return Items.TOTEM_OF_UNDYING;
                }

                if (shouldSwitchFall() && (forceGap.getValue() && !shouldGapPickaxe() && !shouldGapSword())) {
                    return Items.TOTEM_OF_UNDYING;
                }

                //lag check
                if (shouldSwitchLag()) {
                    return Items.TOTEM_OF_UNDYING;
                }

                if (shouldSwitchElytra() && (forceGap.getValue() && !shouldGapPickaxe() && !shouldGapSword())) {
                    return Items.TOTEM_OF_UNDYING;
                }

                if (shouldGapSword()) {
                    return Items.GOLDEN_APPLE;
                }

                if (shouldGapPickaxe()) {
                    return Items.GOLDEN_APPLE;
                }

               return Items.END_CRYSTAL;
            }
            case Gapple: {

                if (shouldSwitchHP()) {
                    return Items.TOTEM_OF_UNDYING;
                }

                if (shouldSwitchHoleHP()) {
                    return Items.TOTEM_OF_UNDYING;
                }

                if (shouldSwitchFall()) {
                    return Items.TOTEM_OF_UNDYING;
                }

                if (shouldSwitchLag()) {
                    return Items.TOTEM_OF_UNDYING;
                }

                if (shouldSwitchElytra()) {
                    return Items.TOTEM_OF_UNDYING;
                }

                return Items.GOLDEN_APPLE;
            }
        }
        return Items.TOTEM_OF_UNDYING;
    }

    boolean shouldSwitchHP() {
        return EntityUtil.getHealth(mc.player) < totemHealth.getValue() && !EntityUtil.isPlayerSafe(mc.player);
    }

    boolean shouldSwitchHoleHP() {
        return EntityUtil.getHealth(mc.player) < totemHoleHealth.getValue() && EntityUtil.isPlayerSafe(mc.player);
    }

    boolean shouldGapSword() {
        return gapSword.getValue() && InventoryUtil.heldItem(Items.DIAMOND_SWORD, Main) && mc.gameSettings.keyBindUseItem.isKeyDown();
    }

    boolean shouldGapPickaxe() {
        return gapPickaxe.getValue() && InventoryUtil.heldItem(Items.DIAMOND_PICKAXE, Main) && mc.gameSettings.keyBindUseItem.isKeyDown();
    }

    boolean shouldSwitchFall() {
        //maybe mc.player.prevPosY != mc.player.posY?
        return mc.player.motionY < 0 && mc.player.fallDistance > fallDistance.getValue() && !mc.player.isElytraFlying();
    }

    boolean shouldSwitchLag() {
        return totemOnLag.getValue() && Cascade.serverManager.isServerNotResponding(lagTime.getValue());
    }

    boolean shouldSwitchElytra() {
        return totemElytra.getValue() && mc.player.isElytraFlying();
    }

    boolean priorityCheck() {
        return false;
    }

    int GetItemSlot(Item item) {
        if (mc.player == null) {
            return 0;
        }
        for (int i = 0; i < mc.player.inventoryContainer.getInventory().size(); ++i) {
            if (i != 0 && i != 5 && i != 6 && i != 7) {
                if (i != 8) {
                    ItemStack s = mc.player.inventoryContainer.getInventory().get(i);
                    if (!s.isEmpty()) {
                        if (s.getItem() == item) {
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    int getSize(Item item) {
        int amt = 0;
        for (int i = 45; i < 0; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                amt += mc.player.inventory.getStackInSlot(i).getCount();
            }
        }
        return amt;
    }

    int getStackSize() {
        int amt = 0;
        switch (mode.getValue()) {
            case Totem: {
                for (int i = 45; i > 0; --i) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TOTEM_OF_UNDYING) {
                        amt += mc.player.inventory.getStackInSlot(i).getCount();
                    }
                }
                break;
            }
            case Crystal: {
                for (int i = 45; i > 0; --i) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() == Items.END_CRYSTAL) {
                        amt += mc.player.inventory.getStackInSlot(i).getCount();
                    }
                }
                break;
            }
            case Gapple: {
                for (int i = 45; i > 0; --i) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() == Items.GOLDEN_APPLE) {
                        amt += mc.player.inventory.getStackInSlot(i).getCount();
                    }
                }
                break;
            }
        }
        return amt;
    }
}