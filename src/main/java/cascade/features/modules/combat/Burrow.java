package cascade.features.modules.combat;

import cascade.Cascade;
import cascade.features.modules.player.Freecam;
import cascade.util.player.*;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.entity.EntityUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class Burrow extends Module {

    public Burrow() {
        super("Burrow", Category.COMBAT, "");
    }

    Setting<Boolean> breakCrystals = register(new Setting("BreakCrystals", false));
    Setting<Boolean> cooldown = register(new Setting("Cooldown", true, v -> breakCrystals.getValue()));
    Setting<Boolean> antiWeakness = register(new Setting("AntiWeakness", false, v -> breakCrystals.getValue()));
    Setting<Boolean> bypass = register(new Setting("Bypass", false));
    Setting<Block> prefer = register(new Setting("Prefer", Block.EChest));
    enum Block {EChest, Obsidian}
    Setting<Double> offset = register(new Setting("Offset", 3.0, -9.0, 9.0));
    double[] offsets = new double[] {0.41999998688698d, 0.7531999805212d, 1.00133597911214d, 1.16610926093821d};
    BlockPos startPos = null;

    @Override
    public void onDisable() {
        startPos = null;
    }

    @Override
    public void onEnable() {
        startPos = mc.player.getPosition();
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }

        int oglSlot = mc.player.inventory.currentItem;
        int ecSlot = InventoryUtil.getItemFromHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST));
        int obbySlot = InventoryUtil.getItemFromHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN));
        if (ecSlot == -1 && obbySlot == -1) {
            disable();
            return;
        }
        if (!canPlaceBlock(startPos)) {
            return;
        }

        if (prefer.getValue() == Block.EChest) {
            if (ecSlot != -1) {
                ItemUtil.silentSwap(ecSlot);
            } else if (obbySlot != -1) {
                ItemUtil.silentSwap(obbySlot);
            }
        }

        if (prefer.getValue() == Block.Obsidian) {
            if (mc.world.getBlockState(startPos.down()).getBlock() == Blocks.ENDER_CHEST) {
                InventoryUtil.packetSwap(ecSlot);
            } else if (obbySlot != -1) {
                ItemUtil.silentSwap(obbySlot);
            } else if (ecSlot != -1) {
                InventoryUtil.packetSwap(ecSlot);
            }
        }

        PlayerUtil.startSneaking();
        for (double pos : offsets) {
            mc.getConnection().sendPacket(new CPacketPlayer.Position(mc.player.posX + (bypass.getValue() ? pos : 0.0d), mc.player.posY + pos, mc.player.posZ + (bypass.getValue() ? pos : 0.0d), true));
        }
        BlockUtil.placeBlock(startPos, true, false);
        ItemUtil.silentSwapRecover(oglSlot);
        doLagBack();
        disable();
    }

    void doLagBack() {
        int y;
        int offset;
        for (offset = (y = 2); y < mc.world.getHeight() - mc.player.posY; ++y) {
            IBlockState scanState1 = mc.world.getBlockState(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ).up(y));
            if (scanState1.getBlock() == Blocks.AIR) {
                IBlockState scanState2 = mc.world.getBlockState(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ).up(y + 1));
                if (scanState2.getBlock() == Blocks.AIR) {
                    offset = y;
                    break;
                }
            }
        }
        mc.getConnection().sendPacket(new CPacketPlayer.Position(mc.player.posX + (bypass.getValue() ? 1.16610926093821d : 0.0d), mc.player.posY + (bypass.getValue() ? 1.16610926093821d : offset), mc.player.posZ + (bypass.getValue() ? 1.16610926093821d : 0.0d), false));
        PlayerUtil.stopSneaking();
    }

    boolean canPlaceBlock(BlockPos pos) {
        if (!mc.world.getBlockState(pos).getMaterial().isReplaceable() ||
                AttackUtil.isInterceptedByOther(pos) ||
                startPos.getY() > 255 ||
                !mc.player.onGround ||
                PlayerUtil.isClipping() ||
                Freecam.getInstance().isEnabled() ||
                mc.player.isInWeb ||
                PlayerUtil.isInLiquid() ||
                //todo who is that bro
                mc.world.checkBlockCollision(mc.player.boundingBox.expand(0, 1, 0))) {
            return false;
        }
        if (AttackUtil.isInterceptedByCrystal(startPos)) {
            if (!breakCrystals.getValue()) {
                return false;
            }
            EntityEnderCrystal crystal = null;
            for (Entity entity : mc.world.loadedEntityList) {
                if (entity == null) {
                    continue;
                }
                if (mc.player.getDistance(entity) > 1.75) {
                    continue;
                }
                if (!(entity instanceof EntityEnderCrystal)) {
                    continue;
                }
                if (entity.isDead) {
                    continue;
                }
                crystal = (EntityEnderCrystal)entity;
            }
            //todo bro idk seems chinese
            if (crystal != null && (!cooldown.getValue() || (cooldown.getValue() && !Cascade.swapManager.hasSwapped()))) {
                /*if (rotate.getValue()) {
                    RotationUtil.faceEntity(crystal);
                }*/
                int previousSlot = mc.player.inventory.currentItem;
                boolean awSwitched = false;
                if (antiWeakness.getValue() && ItemUtil.shouldAntiWeakness()) {
                    int awSlot = getAwSlot();
                    if (awSlot != -1) {
                        ItemUtil.silentSwap(awSlot);
                        awSwitched = true;
                    }
                }
                mc.getConnection().sendPacket(new CPacketUseEntity(crystal));
                mc.getConnection().sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                if (antiWeakness.getValue() && ItemUtil.shouldAntiWeakness() && awSwitched) {
                    ItemUtil.silentSwapRecover(previousSlot);
                }
                return true;
            }
        }
        return false;
    }

    int getAwSlot() {
        int slot;
        slot = getHotbarItemSlot(Items.DIAMOND_SWORD);
        if (slot == -1) {
            slot = getHotbarItemSlot(Items.DIAMOND_PICKAXE);
        }
        if (slot == -1) {
            slot = getHotbarItemSlot(Items.DIAMOND_AXE);
        }

        return slot;
    }

    int getHotbarItemSlot(Item item) {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            if (!mc.player.inventory.getStackInSlot(i).getItem().equals(item)) {
                continue;
            }
            slot = i;
            break;
        }
        return slot;
    }
}