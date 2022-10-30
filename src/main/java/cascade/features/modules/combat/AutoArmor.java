package cascade.features.modules.combat;

import cascade.Cascade;
import cascade.features.gui.CascadeGui;
import cascade.features.modules.Module;
import cascade.features.modules.player.XCarry;
import cascade.features.setting.Bind;
import cascade.features.setting.Setting;
import cascade.util.entity.CombatUtil;
import cascade.util.entity.EntityUtil;
import cascade.util.misc.MathUtil;
import cascade.util.misc.Timer;
import cascade.util.player.InventoryUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static cascade.util.entity.CombatUtil.getRoundedDamage;

public class AutoArmor extends Module {

    public AutoArmor() {
        super("AutoArmor", Category.COMBAT, "autoArmoUr bro");
    }

    Setting<Integer> delay = register(new Setting("Delay", 65, 0, 500));
    Setting<Boolean> autoMend = register(new Setting("AutoMend", false));
    Setting<Float> enemyRange = register(new Setting("EnemyRange", 8.0f, 0.1f, 25.0f, v -> autoMend.getValue()));
    Setting<Integer> minPerc = register(new Setting("Min%", 80, 1, 100, v -> autoMend.getValue()));
    Setting<Boolean> curse = register(new Setting("AllowCurseOfBind", false));
    Setting<Integer> packets = register(new Setting("Packets", 3, 1, 12));
    Setting<Bind> elytraBind = register(new Setting("ElytraSwap", new Bind(-1)));
    Setting<Boolean> updateController = register(new Setting("Update", true));
    Setting<Boolean> shiftClick = register(new Setting("ShiftClick", false));
    Timer timer = new Timer();
    Timer elytraTimer = new Timer();
    Queue<InventoryUtil.Task> taskList = new ConcurrentLinkedQueue<>();
    List<Integer> doneSlots = new ArrayList<>();
    boolean elytraOn = false;
    EntityPlayer closest;

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent e) {
        if (Keyboard.getEventKeyState() && !(mc.currentScreen instanceof CascadeGui) && elytraBind.getValue().getKey() == Keyboard.getEventKey()) {
            elytraOn = !elytraOn;
        }
    }

    @Override
    public void onDisable() {
        taskList.clear();
        doneSlots.clear();
        elytraOn = false;
    }

    @Override
    public void onLogout() {
        taskList.clear();
        doneSlots.clear();
        timer.reset();
        elytraTimer.reset();
    }

    @Override
    public void onTick() {
        if (fullNullCheck() || (mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory))) {
            return;
        }
        if (this.taskList.isEmpty()) {
            if (autoMend.getValue() && InventoryUtil.holdingItem(ItemExpBottle.class) && AutoArmor.mc.gameSettings.keyBindUseItem.isKeyDown() && (isSafe() || EntityUtil.isSafe(mc.player, 1, false))) {
                final ItemStack helm = AutoArmor.mc.player.inventoryContainer.getSlot(5).getStack();
                if (!helm.isEmpty) {
                    final int helmDamage = getRoundedDamage(helm);
                    if (helmDamage >= minPerc.getValue()) {
                        this.takeOffSlot(5);
                    }
                }
                final ItemStack chest = AutoArmor.mc.player.inventoryContainer.getSlot(6).getStack();
                if (!chest.isEmpty) {
                    final int chestDamage = getRoundedDamage(chest);
                    if (chestDamage >= minPerc.getValue()) {
                        this.takeOffSlot(6);
                    }
                }
                final ItemStack legging = AutoArmor.mc.player.inventoryContainer.getSlot(7).getStack();
                if (!legging.isEmpty) {
                    final int leggingDamage = getRoundedDamage(legging);
                    if (leggingDamage >= minPerc.getValue()) {
                        this.takeOffSlot(7);
                    }
                }
                final ItemStack feet = AutoArmor.mc.player.inventoryContainer.getSlot(8).getStack();
                if (!feet.isEmpty) {
                    final int bootDamage = getRoundedDamage(feet);
                    if (bootDamage >= minPerc.getValue()) {
                        this.takeOffSlot(8);
                    }
                }
                return;
            }
            final ItemStack helm = AutoArmor.mc.player.inventoryContainer.getSlot(5).getStack();
            if (helm.getItem() == Items.AIR) {
                final int slot = InventoryUtil.findArmorSlot(EntityEquipmentSlot.HEAD, this.curse.getValue(), XCarry.getInstance().isOn());
                if (slot != -1) {
                    this.getSlotOn(5, slot);
                }
            }
            final ItemStack chest = AutoArmor.mc.player.inventoryContainer.getSlot(6).getStack();
            if (chest.getItem() == Items.AIR) {
                if (this.taskList.isEmpty()) {
                    if (this.elytraOn && this.elytraTimer.passedMs(500L)) {
                        final int elytraSlot = InventoryUtil.findItemInventorySlot(Items.ELYTRA, false, XCarry.getInstance().isOn());
                        if (elytraSlot != -1) {
                            if ((elytraSlot < 5 && elytraSlot > 1) || !this.shiftClick.getValue()) {
                                this.taskList.add(new InventoryUtil.Task(elytraSlot));
                                this.taskList.add(new InventoryUtil.Task(6));
                            }
                            else {
                                this.taskList.add(new InventoryUtil.Task(elytraSlot, true));
                            }
                            if (this.updateController.getValue()) {
                                this.taskList.add(new InventoryUtil.Task());
                            }
                            this.elytraTimer.reset();
                        }
                    }
                    else if (!this.elytraOn) {
                        final int slot2 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.CHEST, this.curse.getValue(), XCarry.getInstance().isOn());
                        if (slot2 != -1) {
                            this.getSlotOn(6, slot2);
                        }
                    }
                }
            }
            else if (this.elytraOn && chest.getItem() != Items.ELYTRA && this.elytraTimer.passedMs(500L)) {
                if (this.taskList.isEmpty()) {
                    final int slot2 = InventoryUtil.findItemInventorySlot(Items.ELYTRA, false, XCarry.getInstance().isOn());
                    if (slot2 != -1) {
                        this.taskList.add(new InventoryUtil.Task(slot2));
                        this.taskList.add(new InventoryUtil.Task(6));
                        this.taskList.add(new InventoryUtil.Task(slot2));
                        if (this.updateController.getValue()) {
                            this.taskList.add(new InventoryUtil.Task());
                        }
                    }
                    this.elytraTimer.reset();
                }
            }
            else if (!this.elytraOn && chest.getItem() == Items.ELYTRA && this.elytraTimer.passedMs(500L) && this.taskList.isEmpty()) {
                int slot2 = InventoryUtil.findItemInventorySlot((Item)Items.DIAMOND_CHESTPLATE, false, XCarry.getInstance().isOn());
                if (slot2 == -1) {
                    slot2 = InventoryUtil.findItemInventorySlot((Item)Items.IRON_CHESTPLATE, false, XCarry.getInstance().isOn());
                    if (slot2 == -1) {
                        slot2 = InventoryUtil.findItemInventorySlot((Item)Items.GOLDEN_CHESTPLATE, false, XCarry.getInstance().isOn());
                        if (slot2 == -1) {
                            slot2 = InventoryUtil.findItemInventorySlot((Item)Items.CHAINMAIL_CHESTPLATE, false, XCarry.getInstance().isOn());
                            if (slot2 == -1) {
                                slot2 = InventoryUtil.findItemInventorySlot((Item)Items.LEATHER_CHESTPLATE, false, XCarry.getInstance().isOn());
                            }
                        }
                    }
                }
                if (slot2 != -1) {
                    this.taskList.add(new InventoryUtil.Task(slot2));
                    this.taskList.add(new InventoryUtil.Task(6));
                    this.taskList.add(new InventoryUtil.Task(slot2));
                    if (this.updateController.getValue()) {
                        this.taskList.add(new InventoryUtil.Task());
                    }
                }
                this.elytraTimer.reset();
            }
            final ItemStack legging = AutoArmor.mc.player.inventoryContainer.getSlot(7).getStack();
            if (legging.getItem() == Items.AIR) {
                final int slot3 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.LEGS, this.curse.getValue(), XCarry.getInstance().isOn());
                if (slot3 != -1) {
                    this.getSlotOn(7, slot3);
                }
            }
            final ItemStack feet = AutoArmor.mc.player.inventoryContainer.getSlot(8).getStack();
            if (feet.getItem() == Items.AIR) {
                final int slot4 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.FEET, this.curse.getValue(), XCarry.getInstance().isOn());
                if (slot4 != -1) {
                    this.getSlotOn(8, slot4);
                }
            }
        }
        if (this.timer.passedMs((int)(this.delay.getValue() * Cascade.serverManager.getTpsFactor()))) {
            if (!this.taskList.isEmpty()) {
                for (int i = 0; i < packets.getValue(); ++i) {
                    final InventoryUtil.Task task = this.taskList.poll();
                    if (task != null) {
                        task.run();
                    }
                }
            }
            this.timer.reset();
        }
    }

    private void takeOffSlot(final int slot) {
        if (this.taskList.isEmpty()) {
            int target = -1;
            for (final int i : InventoryUtil.findEmptySlots(XCarry.getInstance().isOn())) {
                if (!this.doneSlots.contains(target)) {
                    target = i;
                    this.doneSlots.add(i);
                }
            }
            if (target != -1) {
                if ((target < 5 && target > 0) || !this.shiftClick.getValue()) {
                    this.taskList.add(new InventoryUtil.Task(slot));
                    this.taskList.add(new InventoryUtil.Task(target));
                }
                else {
                    this.taskList.add(new InventoryUtil.Task(slot, true));
                }
                if (this.updateController.getValue()) {
                    this.taskList.add(new InventoryUtil.Task());
                }
            }
        }
    }

    private void getSlotOn(final int slot, final int target) {
        if (this.taskList.isEmpty()) {
            this.doneSlots.remove((Object)target);
            if ((target < 5 && target > 0) || !this.shiftClick.getValue()) {
                this.taskList.add(new InventoryUtil.Task(target));
                this.taskList.add(new InventoryUtil.Task(slot));
            }
            else {
                this.taskList.add(new InventoryUtil.Task(target, true));
            }
            if (this.updateController.getValue()) {
                this.taskList.add(new InventoryUtil.Task());
            }
        }
    }

    @Override
    public String getDisplayInfo() {
        if (elytraOn) {
            return "Elytra";
        }
        return null;
    }

    boolean isSafe() {
        closest = CombatUtil.getTarget(enemyRange.getValue());
        return closest == null || mc.player.getDistanceSq(closest) >= MathUtil.square(enemyRange.getValue());
    }
}
