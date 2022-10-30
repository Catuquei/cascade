package cascade.features.modules.combat;

import cascade.Cascade;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.misc.Timer;
import cascade.util.player.BlockUtil;
import cascade.util.player.ItemUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class TrapR extends Module {

    public TrapR() {
        super("Trap", Category.COMBAT, "");
    }

    Timer timer = new Timer();
    Setting<Integer> delay = register(new Setting("Delay", 0, 0, 500));
    Setting<Mode> mode = register(new Setting("Mode", Mode.Top));
    enum Mode {Full, City, Top}
    Setting<Integer> blocksPerTick = register(new Setting("BPT", 10, 0, 20));
    Setting<Double> targetRange = register(new Setting("TargetRange", 4.5d, 0.1d, 6.0d));
    Setting<Boolean> disable = register(new Setting("AutoDisable", false));
    Entity target;
    public BlockPos[] fullOffsets = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(-1, 0, 0), new BlockPos(0, 0, -1), new BlockPos(1, 1, 0), new BlockPos(0, 1, 1), new BlockPos(-1, 1, 0), new BlockPos(0, 1, -1), new BlockPos(1, 2, 0), new BlockPos(0, 2, 0)};
    public BlockPos[] cityOffsets = new BlockPos[]{new BlockPos(1, 1, 0), new BlockPos(1, 1, 1), new BlockPos(0, 1, 1), new BlockPos(-1, 1, 1), new BlockPos(-1, 1, 0), new BlockPos(-1, 1, -1), new BlockPos(0, 1, -1), new BlockPos(-1, 1, -1), new BlockPos(1, 2, 0), new BlockPos(0, 2, 0)};
    BlockPos[] surroundOffsets = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(-1, 0, 0), new BlockPos(0, 0, -1), new BlockPos(0, 0, 0)};

    BlockPos getPlayerPos(Entity player) {
        double decimalPoint = player.posY - Math.floor(player.posY);
        return new BlockPos(player.posX, decimalPoint > 0.8 ? Math.floor(player.posY) + 1.0 : Math.floor(player.posY), player.posZ);
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (timer.passedMs(delay.getValue())) {
            target = mc.world.getLoadedEntityList().stream().filter(Objects::nonNull).filter(entity -> entity instanceof EntityPlayer).filter(this::isAlive).filter(entity -> entity.getEntityId() != mc.player.getEntityId()).filter(entity -> !Cascade.friendManager.isFriend(entity.getName())).filter(entity -> mc.player.getDistance(entity) <= targetRange.getValue()).min(Comparator.comparingDouble(entity -> mc.player.getDistance(entity))).orElse(null);
            if (target == null) {
                return;
            }
            if (mc.world.getBlockState(getPlayerPos(target).add(0, 2, 0)).getMaterial().isSolid() && disable.getValue()) {
                disable();
                return;
            }
            int blocksInTick = 0;
            boolean switched = false;
            int oldSlot = mc.player.inventory.currentItem;
            int slot = getHotbarItemSlot(Item.getItemFromBlock(Blocks.OBSIDIAN));
            if (slot == -1) {
                return;
            }
            BlockPos[] offsets;
            for (BlockPos pos : offsets = mode.getValue().equals(Mode.Full) ? offsetBlocks(fullOffsets, getPlayerPos(target)) : (mode.getValue().equals(Mode.City) ? offsetBlocks(cityOffsets, getPlayerPos(target)) : getObbyToHead(getPlayerPos(target)))) {
                if (!canPlaceBlock(pos)) {
                    continue;
                }
                if (!switched) {
                    ItemUtil.silentSwap(slot);
                    switched = true;
                }
                BlockUtil.placeBlock5(pos, true);
                if (++blocksInTick > blocksPerTick.getValue()) {
                    break;
                }
            }
            ItemUtil.silentSwapRecover(oldSlot);
            timer.reset();
        }
    }

    boolean canPlaceBlock(BlockPos pos) {
        boolean allow = true;
        if (!mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
            allow = false;
        }
        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (entity instanceof EntityArmorStand) {
                continue;
            }
            allow = false;
            break;
        }
        return allow;
    }

    BlockPos[] offsetBlocks(BlockPos[] toOffset, BlockPos offsetPlace) {
        BlockPos[] offsets = new BlockPos[toOffset.length];
        int index = 0;
        for (BlockPos blockPos : toOffset) {
            offsets[index] = offsetPlace.add(blockPos);
            ++index;
        }
        return offsets;
    }

    public BlockPos[] getObbyToHead(BlockPos feet) {
        ArrayList<BlockPos> obbyToHead = new ArrayList<>();
        BlockPos head = feet.add(new BlockPos(0, 1, 0));
        if (getSurroundedBlock(head) != null) {
            obbyToHead.add(getSurroundedBlock(head).add(new BlockPos(0, 1, 0)));
            obbyToHead.add(head.add(new BlockPos(0, 1, 0)));
        } else if (getSurroundedBlock(feet) != null) {
            obbyToHead.add(getSurroundedBlock(feet).add(new BlockPos(0, 1, 0)));
        } else if (getSurroundedBlock(feet.add(0, -1, 0)) != null) {
            obbyToHead.add(getSurroundedBlock(feet.add(new BlockPos(0, -1, 0))).add(new BlockPos(0, 1, 0)));
        }
        BlockPos[] blocks = new BlockPos[obbyToHead.size()];
        return obbyToHead.toArray(blocks);
    }

    BlockPos getSurroundedBlock(BlockPos feet) {
        for (BlockPos offset : surroundOffsets) {
            IBlockState blockState = mc.world.getBlockState(feet.add(offset));
            if (blockState.getMaterial().isReplaceable()) {
                continue;
            }
            return feet.add(offset);
        }
        return null;
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

    boolean isAlive(Entity entity) {
        return entity instanceof EntityLivingBase && !entity.isDead && ((EntityLivingBase)entity).getHealth() > 0.0f;
    }
}