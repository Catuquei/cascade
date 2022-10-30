package cascade.features.modules.combat;

import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.misc.Timer;
import cascade.util.player.BlockUtil;
import cascade.util.player.InventoryUtil;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;

import java.util.*;

public class HoleFiller extends Module {

    public HoleFiller() {
        super("HoleFiller", Category.COMBAT, "Fills holes around ur target");
    }

    Setting<Integer> bpt = register(new Setting("BPT", 8, 1, 25));
    Setting<Integer> delay = register(new Setting("Delay", 0, 0, 250));
    Setting<Float> range = register(new Setting("Range", 6.0f, 0.1f, 6.0f));
    Setting<Boolean> packet = register(new Setting("Packet", true));
    Setting<Boolean> rotate = register(new Setting("Rotate", false));
    Setting<Integer> toggle = register(new Setting("AutoDisable", 10, 0, 250));
    //Setting<Boolean> requirePlayer = register(new Setting("RequirePlayer", false));
    //Setting<Boolean> attack = register(new Setting("Attack", true));
    Map<BlockPos, Integer> retries = new HashMap<>();
    ArrayList<BlockPos> holes = new ArrayList<>();
    Timer retryTimer = new Timer();
    Timer offTimer = new Timer();
    Timer timer = new Timer();
    int placements = 0;
    int trie;

    @Override
    public void onEnable() {
        offTimer.reset();
        trie = 0;
    }

    @Override
    public void onDisable() {
        retries.clear();
    }

    @Override
    public void onUpdate() {
        if (!fullNullCheck()) {
            mc.addScheduledTask(() -> doHoleFill());
        }
    }

    void doHoleFill() {
        if (check()) {
            return;
        }
        holes = new ArrayList<>();
        Iterable<BlockPos> blocks = BlockPos.getAllInBox(mc.player.getPosition().add(-range.getValue(), -range.getValue(), -range.getValue()), mc.player.getPosition().add(range.getValue(), range.getValue(), range.getValue()));
        for (BlockPos pos : blocks) {
            if (!mc.world.getBlockState(pos).getMaterial().blocksMovement() && !mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial().blocksMovement()) {
                boolean solidNeighbours = (mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.OBSIDIAN) && (mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.OBSIDIAN) && (mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.BEDROCK | HoleFiller.mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.OBSIDIAN) && (HoleFiller.mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.BEDROCK | HoleFiller.mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.OBSIDIAN) && HoleFiller.mc.world.getBlockState(pos.add(0, 0, 0)).getMaterial() == Material.AIR && HoleFiller.mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial() == Material.AIR && HoleFiller.mc.world.getBlockState(pos.add(0, 2, 0)).getMaterial() == Material.AIR;
                if (!solidNeighbours) {
                    continue;
                }
                holes.add(pos);
            }
        }
        if (!holes.isEmpty()) {
            holes.forEach(this::placeBlock);
            if (toggle.getValue() != 0) {
                if (offTimer.passedMs(toggle.getValue())) {
                    disable();
                    return;
                }
            }
        }
    }

    void placeBlock(BlockPos pos) {
        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
            if (entity instanceof EntityLivingBase) {
                return;
            }
        }
        if (placements < bpt.getValue()) {
            int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
            int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
            if (obbySlot == -1 && eChestSot == -1) {
                disable();
                return;
            }
            int originalSlot = mc.player.inventory.currentItem;
            InventoryUtil.packetSwap(obbySlot != -1 ? obbySlot : eChestSot);
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, rotate.getValue(), packet.getValue(), mc.player.isSneaking(), true);
            InventoryUtil.packetSwap(originalSlot);
            timer.reset();
            placements++;
        }
    }


    boolean check() {
        if (fullNullCheck()) {
            return true;
        }
        placements = 0;
        if (retryTimer.passedMs(250)) {
            retries.clear();
            retryTimer.reset();
        }
        return !timer.passedMs((long)(delay.getValue() * 10d));
    }
}