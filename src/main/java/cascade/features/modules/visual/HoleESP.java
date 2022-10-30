package cascade.features.modules.visual;

import cascade.Cascade;
import cascade.event.events.Render3DEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.player.BlockUtil;
import cascade.util.render.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;

import java.awt.*;

public class HoleESP extends Module {

    public HoleESP() {
        super("HoleESP", Category.VISUAL, "Shows safe spots");
        INSTANCE = this;
    }

    Setting<Integer> range = register(new Setting("Range", 7, 1, 10));
    Setting<Boolean> box = register(new Setting("Box", true));
    Setting<Color> obbyC = register(new Setting("ObsidianColor", new Color(-1), v -> box.getValue()));
    Setting<Color> bedC = register(new Setting("BedrockColor", new Color(-1), v -> box.getValue()));
    Setting<Boolean> outline = register(new Setting("Outline", true));
    Setting<Color> obbyOutlineC = register(new Setting("ObsidianOutline", new Color(-1), v -> outline.getValue()));
    Setting<Color> bedOutlineC = register(new Setting("BedrockOutline", new Color(-1), v -> outline.getValue()));
    Setting<Boolean> cross = register(new Setting("Cross", false));
    Setting<Color> obbyCrossC = register(new Setting("ObsidianCross", new Color(-1), v -> cross.getValue()));
    Setting<Color> bedCrossC = register(new Setting("BedrockCross", new Color(-1), v -> cross.getValue()));
    Setting<Float> lw = register(new Setting("LineWidth", 0.1f, 0.1f, 6.0f, v -> outline.getValue() || cross.getValue()));
    Setting<Boolean> doubleHoles = register(new Setting("DoubleHoles", true));
    Setting<Boolean> inFov = register(new Setting("InFov", false));
    Setting<Boolean> renderOwn = register(new Setting("RenderOwn", true));
    private static HoleESP INSTANCE;

    public static HoleESP getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HoleESP();
        }
        return INSTANCE;
    }

    @Override
    public void onRender3D(final Render3DEvent event) {
        if (mc.renderViewEntity != null) {
            Vec3i playerPos = new Vec3i(mc.renderViewEntity.posX, mc.renderViewEntity.posY, mc.renderViewEntity.posZ);
            for (int x = playerPos.getX() - range.getValue(); x < playerPos.getX() + range.getValue(); ++x) {
                for (int z = playerPos.getZ() - range.getValue(); z < playerPos.getZ() + range.getValue(); ++z) {
                    for (int y = playerPos.getY() + range.getValue(); y > playerPos.getY() - range.getValue(); --y) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (mc.world.getBlockState(pos).getBlock() == Blocks.AIR && mc.world.getBlockState(pos.add(0, 1, 0)).getBlock() == Blocks.AIR && mc.world.getBlockState(pos.add(0, 2, 0)).getBlock() == Blocks.AIR && (pos != new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)) || renderOwn.getValue()) {
                            /*if (!BlockUtil.isPosInFov(pos) && inFov.getValue()) {
                                return;
                            }*/
                            if (BlockUtil.isPosInFov(pos) || !inFov.getValue()) {
                                if (doubleHoles.getValue()) {
                                    if (mc.world.getBlockState(pos.north()).getBlock() == Blocks.AIR && mc.world.getBlockState(pos.north().up()).getBlock() == Blocks.AIR && mc.world.getBlockState(pos.north().down()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.north(2)).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.north().east()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.north().west()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK) {
                                        RenderUtil.drawHoleESP(pos, box.getValue(), outline.getValue(), cross.getValue(), new Color(bedC.getValue().getRed(), bedC.getValue().getGreen(), bedC.getValue().getBlue(), bedC.getValue().getAlpha()), new Color(bedOutlineC.getValue().getRed(), bedOutlineC.getValue().getGreen(), bedOutlineC.getValue().getBlue(), bedOutlineC.getValue().getAlpha()), new Color(bedCrossC.getValue().getRed(), bedCrossC.getValue().getGreen(), bedCrossC.getValue().getBlue(), bedCrossC.getValue().getAlpha()), lw.getValue());
                                        RenderUtil.drawHoleESP(pos.north(), box.getValue(), outline.getValue(), cross.getValue(), new Color(bedC.getValue().getRed(), bedC.getValue().getGreen(), bedC.getValue().getBlue(), bedC.getValue().getAlpha()), new Color(bedOutlineC.getValue().getRed(), bedOutlineC.getValue().getGreen(), bedOutlineC.getValue().getBlue(), bedOutlineC.getValue().getAlpha()), new Color(bedCrossC.getValue().getRed(), bedCrossC.getValue().getGreen(), bedCrossC.getValue().getBlue(), bedCrossC.getValue().getAlpha()), lw.getValue());
                                    } else if (mc.world.getBlockState(pos.north()).getBlock() == Blocks.AIR && mc.world.getBlockState(pos.north().up()).getBlock() == Blocks.AIR && (mc.world.getBlockState(pos.north().down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.north().down()).getBlock() == Blocks.BEDROCK) && (mc.world.getBlockState(pos.north(2)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.north(2)).getBlock() == Blocks.BEDROCK) && (mc.world.getBlockState(pos.east()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK) && (mc.world.getBlockState(pos.north().east()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.north().east()).getBlock() == Blocks.BEDROCK) && (mc.world.getBlockState(pos.west()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK) && (mc.world.getBlockState(pos.north().west()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.north().west()).getBlock() == Blocks.BEDROCK) && (mc.world.getBlockState(pos.south()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK) && (mc.world.getBlockState(pos.down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK)) {
                                        RenderUtil.drawHoleESP(pos, box.getValue(), outline.getValue(), cross.getValue(), new Color(obbyC.getValue().getRed(), obbyC.getValue().getGreen(), obbyC.getValue().getBlue(), obbyC.getValue().getAlpha()), new Color(obbyOutlineC.getValue().getRed(), obbyOutlineC.getValue().getGreen(), obbyOutlineC.getValue().getBlue(), obbyOutlineC.getValue().getAlpha()), new Color(obbyCrossC.getValue().getRed(), obbyCrossC.getValue().getGreen(), obbyCrossC.getValue().getBlue(), obbyCrossC.getValue().getAlpha()), lw.getValue());
                                        RenderUtil.drawHoleESP(pos.north(), box.getValue(), outline.getValue(), cross.getValue(), new Color(obbyC.getValue().getRed(), obbyC.getValue().getGreen(), obbyC.getValue().getBlue(), obbyC.getValue().getAlpha()), new Color(obbyOutlineC.getValue().getRed(), obbyOutlineC.getValue().getGreen(), obbyOutlineC.getValue().getBlue(), obbyOutlineC.getValue().getAlpha()), new Color(obbyCrossC.getValue().getRed(), obbyCrossC.getValue().getGreen(), obbyCrossC.getValue().getBlue(), obbyCrossC.getValue().getAlpha()), lw.getValue());
                                    }
                                    if (mc.world.getBlockState(pos.east()).getBlock() == Blocks.AIR && mc.world.getBlockState(pos.east().up()).getBlock() == Blocks.AIR && mc.world.getBlockState(pos.east().down()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.east(2)).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.east(2).down()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.east().north()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.east().south()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK) {
                                        RenderUtil.drawHoleESP(pos, box.getValue(), outline.getValue(), cross.getValue(), new Color(bedC.getValue().getRed(), bedC.getValue().getGreen(), bedC.getValue().getBlue(), bedC.getValue().getAlpha()), new Color(bedOutlineC.getValue().getRed(), bedOutlineC.getValue().getGreen(), bedOutlineC.getValue().getBlue(), bedOutlineC.getValue().getAlpha()), new Color(bedCrossC.getValue().getRed(), bedCrossC.getValue().getGreen(), bedCrossC.getValue().getBlue(), bedCrossC.getValue().getAlpha()), lw.getValue());
                                        RenderUtil.drawHoleESP(pos.east(), box.getValue(), outline.getValue(), cross.getValue(), new Color(bedC.getValue().getRed(), bedC.getValue().getGreen(), bedC.getValue().getBlue(), bedC.getValue().getAlpha()), new Color(bedOutlineC.getValue().getRed(), bedOutlineC.getValue().getGreen(), bedOutlineC.getValue().getBlue(), bedOutlineC.getValue().getAlpha()), new Color(bedCrossC.getValue().getRed(), bedCrossC.getValue().getGreen(), bedCrossC.getValue().getBlue(), bedCrossC.getValue().getAlpha()), lw.getValue());
                                    } else if (mc.world.getBlockState(pos.east()).getBlock() == Blocks.AIR && mc.world.getBlockState(pos.east().up()).getBlock() == Blocks.AIR && (mc.world.getBlockState(pos.east().down()).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(pos.east().down()).getBlock() == Blocks.OBSIDIAN) && (mc.world.getBlockState(pos.east(2)).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(pos.east(2)).getBlock() == Blocks.OBSIDIAN) && (mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(pos.north()).getBlock() == Blocks.OBSIDIAN) && (mc.world.getBlockState(pos.east().north()).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(pos.east().north()).getBlock() == Blocks.OBSIDIAN) && (mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(pos.west()).getBlock() == Blocks.OBSIDIAN) && (mc.world.getBlockState(pos.east().south()).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(pos.east().south()).getBlock() == Blocks.OBSIDIAN) && (mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(pos.south()).getBlock() == Blocks.OBSIDIAN) && (mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(pos.down()).getBlock() == Blocks.OBSIDIAN)) {
                                        RenderUtil.drawHoleESP(pos, box.getValue(), outline.getValue(), cross.getValue(), new Color(obbyC.getValue().getRed(), obbyC.getValue().getGreen(), obbyC.getValue().getBlue(), obbyC.getValue().getAlpha()), new Color(obbyOutlineC.getValue().getRed(), obbyOutlineC.getValue().getGreen(), obbyOutlineC.getValue().getBlue(), obbyOutlineC.getValue().getAlpha()), new Color(obbyCrossC.getValue().getRed(), obbyCrossC.getValue().getGreen(), obbyCrossC.getValue().getBlue(), obbyCrossC.getValue().getAlpha()), lw.getValue());
                                        RenderUtil.drawHoleESP(pos.east(), box.getValue(), outline.getValue(), cross.getValue(), new Color(obbyC.getValue().getRed(), obbyC.getValue().getGreen(), obbyC.getValue().getBlue(), obbyC.getValue().getAlpha()), new Color(obbyOutlineC.getValue().getRed(), obbyOutlineC.getValue().getGreen(), obbyOutlineC.getValue().getBlue(), obbyOutlineC.getValue().getAlpha()), new Color(obbyCrossC.getValue().getRed(), obbyCrossC.getValue().getGreen(), obbyCrossC.getValue().getBlue(), obbyCrossC.getValue().getAlpha()), lw.getValue());
                                    }
                                }
                                if (mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK) {
                                    RenderUtil.drawHoleESP(pos, box.getValue(), outline.getValue(), cross.getValue(), new Color(bedC.getValue().getRed(), bedC.getValue().getGreen(), bedC.getValue().getBlue(), bedC.getValue().getAlpha()), new Color(bedOutlineC.getValue().getRed(), bedOutlineC.getValue().getGreen(), bedOutlineC.getValue().getBlue(), bedOutlineC.getValue().getAlpha()), new Color(bedCrossC.getValue().getRed(), bedCrossC.getValue().getGreen(), bedCrossC.getValue().getBlue(), bedCrossC.getValue().getAlpha()), lw.getValue());
                                } else if (BlockUtil.isBlockUnSafe(mc.world.getBlockState(pos.down()).getBlock()) && BlockUtil.isBlockUnSafe(mc.world.getBlockState(pos.east()).getBlock()) && BlockUtil.isBlockUnSafe(mc.world.getBlockState(pos.west()).getBlock()) && BlockUtil.isBlockUnSafe(mc.world.getBlockState(pos.south()).getBlock())) {
                                    if (BlockUtil.isBlockUnSafe(mc.world.getBlockState(pos.north()).getBlock())) {
                                        RenderUtil.drawHoleESP(pos, box.getValue(), outline.getValue(), cross.getValue(), new Color(obbyC.getValue().getRed(), obbyC.getValue().getGreen(), obbyC.getValue().getBlue(), obbyC.getValue().getAlpha()), new Color(obbyOutlineC.getValue().getRed(), obbyOutlineC.getValue().getGreen(), obbyOutlineC.getValue().getBlue(), obbyOutlineC.getValue().getAlpha()), new Color(obbyCrossC.getValue().getRed(), obbyCrossC.getValue().getGreen(), obbyCrossC.getValue().getBlue(), obbyCrossC.getValue().getAlpha()), lw.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}