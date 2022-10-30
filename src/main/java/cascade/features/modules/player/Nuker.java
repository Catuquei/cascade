package cascade.features.modules.player;

import cascade.Cascade;
import cascade.event.events.BlockEvent;
import cascade.event.events.UpdateWalkingPlayerEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.player.BlockUtil;
import cascade.util.player.InventoryUtil;
import cascade.util.misc.MathUtil;
import cascade.util.misc.Timer;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class Nuker extends Module {

    public Nuker() {
        super("Nuker", Category.PLAYER, "Destroys blocks");
    }

    public Setting<Boolean> rotate = register(new Setting("Rotate", false));
    public Setting<Float> distance = register(new Setting("Range", 6.0f, 0.1f, 6.0f));
    public Setting<Integer> blockPerTick = register(new Setting("Blocks/Attack", 50, 1, 100));
    public Setting<Integer> delay = register(new Setting("Delay/Attack", 50, 1, 1000));
    public Setting<Boolean> nuke = register(new Setting("Nuke", false));
    public Setting<Mode> mode = register(new Setting("Mode", Mode.Nuke, v -> nuke.getValue()));
    public enum Mode {Selection, All, Nuke}
    public Setting<Boolean> shulkers = register(new Setting("Shulkers", false));
    public Setting<Boolean> echests = register(new Setting("EChests", false));
    public Setting<Boolean> hoppers = register(new Setting("Hoppers", false));
    public Setting<Boolean> anvils = register(new Setting("Anvils", true));
    public Setting<Boolean> silentSwitch = register(new Setting("SilentSwitch", false));
    int oldSlot;
    boolean isMining;
    Timer timer = new Timer();
    Block selected;

    @Override
    public void onToggle() {
        selected = null;
        oldSlot = -1;
        timer.reset();
    }

    @SubscribeEvent
    public void onClickBlock(BlockEvent event) {
        if (event.getStage() == 3 && mode.getValue() != Mode.All) {
            Block block = mc.world.getBlockState(event.pos).getBlock();
            if (block != null && block != selected) {
                selected = block;
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayerPre(UpdateWalkingPlayerEvent event) {
        if (event.getStage() == 0 && isEnabled()) {
            if (nuke.getValue()) {
                BlockPos pos = null;
                switch (mode.getValue()) {
                    case Selection: {
                    }
                    case Nuke: {
                        pos = getClosestBlockSelection();
                        break;
                    }
                    case All: {
                        pos = getClosestBlockAll();
                        break;
                    }
                }
                if (pos != null) {
                    if (mode.getValue() != Mode.Nuke) {
                        if (this.rotate.getValue()) {
                            final float[] angle = MathUtil.calcAngle(Nuker.mc.player.getPositionEyes(Nuker.mc.getRenderPartialTicks()), new Vec3d((double)(pos.getX() + 0.5f), (double)(pos.getY() + 0.5f), (double)(pos.getZ() + 0.5f)));
                            Cascade.rotationManager.setPlayerRotations(angle[0], angle[1]);
                        }
                        if (canBreak(pos)) {
                            mc.playerController.onPlayerDamageBlock(pos, Nuker.mc.player.getHorizontalFacing());
                            mc.player.swingArm(EnumHand.MAIN_HAND);
                        }
                    } else {
                        for (int i = 0; i < blockPerTick.getValue(); ++i) {
                            pos = getClosestBlockSelection();
                            if (pos != null) {
                                if (this.rotate.getValue()) {
                                    float[] angle2 = MathUtil.calcAngle(Nuker.mc.player.getPositionEyes(Nuker.mc.getRenderPartialTicks()), new Vec3d((double)(pos.getX() + 0.5f), (double)(pos.getY() + 0.5f), (double)(pos.getZ() + 0.5f)));
                                    Cascade.rotationManager.setPlayerRotations(angle2[0], angle2[1]);
                                }
                                if (timer.passedMs(delay.getValue())) {
                                    mc.playerController.onPlayerDamageBlock(pos, Nuker.mc.player.getHorizontalFacing());
                                    mc.player.swingArm(EnumHand.MAIN_HAND);
                                    timer.reset();
                                }
                            }
                        }
                    }
                }
            }
            if (shulkers.getValue()) {
                breakBlocks(BlockUtil.shulkerList);
            }
            if (echests.getValue()) {
                List<Block> blocklist = new ArrayList<>();
                blocklist.add(Blocks.ENDER_CHEST);
                breakBlocks(blocklist);
            }
            if (hoppers.getValue()) {
                List<Block> blocklist = new ArrayList<>();
                blocklist.add(Blocks.HOPPER);
                breakBlocks(blocklist);
            }
            if (anvils.getValue()) {
                List<Block> blocklist = new ArrayList<>();
                blocklist.add(Blocks.ANVIL);
                breakBlocks(blocklist);
            }
        }
    }

    public void breakBlocks(List<Block> blocks) {
        BlockPos pos = getNearestBlock(blocks);
        if (pos != null) {
            if (!isMining) {
                oldSlot = Nuker.mc.player.inventory.currentItem;
                isMining = true;
            }
            if (rotate.getValue()) {
                float[] angle = MathUtil.calcAngle(Nuker.mc.player.getPositionEyes(Nuker.mc.getRenderPartialTicks()), new Vec3d((double)(pos.getX() + 0.5f), (double)(pos.getY() + 0.5f), (double)(pos.getZ() + 0.5f)));
                Cascade.rotationManager.setPlayerRotations(angle[0], angle[1]);
            }
            if (canBreak(pos)) {
                if (silentSwitch.getValue()) {
                    int pickSlot = InventoryUtil.getItemFromHotbar(Items.DIAMOND_PICKAXE);
                    if (pickSlot != -1) {
                        InventoryUtil.packetSwap(pickSlot);
                    }
                }
                mc.playerController.onPlayerDamageBlock(pos, Nuker.mc.player.getHorizontalFacing());
                mc.player.swingArm(EnumHand.MAIN_HAND);
                if (silentSwitch.getValue() && oldSlot != -1) {
                    InventoryUtil.packetSwap(oldSlot);
                    oldSlot = -1;
                    isMining = false;
                }
            }
        }
    }

    private boolean canBreak(BlockPos pos) {
        IBlockState blockState = mc.world.getBlockState(pos);
        Block block = blockState.getBlock();
        return block.getBlockHardness(blockState, mc.world, pos) != -1.0f;
    }

    private BlockPos getNearestBlock(List<Block> blocks) {
        double maxDist = MathUtil.square(distance.getValue());
        BlockPos ret = null;
        for (double x = maxDist; x >= -maxDist; --x) {
            for (double y = maxDist; y >= -maxDist; --y) {
                for (double z = maxDist; z >= -maxDist; --z) {
                    final BlockPos pos = new BlockPos(Nuker.mc.player.posX + x, Nuker.mc.player.posY + y, Nuker.mc.player.posZ + z);
                    final double dist = Nuker.mc.player.getDistanceSq((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
                    if (dist <= maxDist && blocks.contains(Nuker.mc.world.getBlockState(pos).getBlock()) && this.canBreak(pos)) {
                        maxDist = dist;
                        ret = pos;
                    }
                }
            }
        }
        return ret;
    }

    private BlockPos getClosestBlockAll() {
        float maxDist = this.distance.getValue();
        BlockPos ret = null;
        for (float x = maxDist; x >= -maxDist; --x) {
            for (float y = maxDist; y >= -maxDist; --y) {
                for (float z = maxDist; z >= -maxDist; --z) {
                    final BlockPos pos = new BlockPos(Nuker.mc.player.posX + x, Nuker.mc.player.posY + y, Nuker.mc.player.posZ + z);
                    final double dist = Nuker.mc.player.getDistance((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
                    if (dist <= maxDist && Nuker.mc.world.getBlockState(pos).getBlock() != Blocks.AIR && !(Nuker.mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid) && this.canBreak(pos) && pos.getY() >= Nuker.mc.player.posY) {
                        maxDist = (float)dist;
                        ret = pos;
                    }
                }
            }
        }
        return ret;
    }

    private BlockPos getClosestBlockSelection() {
        float maxDist = this.distance.getValue();
        BlockPos ret = null;
        for (float x = maxDist; x >= -maxDist; --x) {
            for (float y = maxDist; y >= -maxDist; --y) {
                for (float z = maxDist; z >= -maxDist; --z) {
                    final BlockPos pos = new BlockPos(Nuker.mc.player.posX + x, Nuker.mc.player.posY + y, Nuker.mc.player.posZ + z);
                    final double dist = Nuker.mc.player.getDistance((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
                    if (dist <= maxDist && Nuker.mc.world.getBlockState(pos).getBlock() != Blocks.AIR && !(Nuker.mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid) && Nuker.mc.world.getBlockState(pos).getBlock() == this.selected && this.canBreak(pos) && pos.getY() >= Nuker.mc.player.posY) {
                        maxDist = (float)dist;
                        ret = pos;
                    }
                }
            }
        }
        return ret;
    }
}