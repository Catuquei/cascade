package cascade.features.modules.visual;

import cascade.Cascade;
import cascade.event.events.Render3DEvent;
import cascade.util.render.ColorUtil;
import cascade.util.render.RenderUtil;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;

import java.awt.*;

public class BlockHighlight extends Module {

    public BlockHighlight() {
        super("BlockHighlight", Category.VISUAL, "ye");
    }

    public Setting<Mode> mode = register(new Setting("Mode", Mode.Outline));
    public enum Mode {Fill, Outline, Both}
    public Setting<Color> c = register(new Setting("Color", new Color(-1)));
    public Setting<Float> width = register(new Setting("Width", 1.0f, 0.1f, 5.0f));
    public Setting<Boolean> rainbow = register(new Setting("Rainbow", false));
    public Setting<Integer> rainbowhue = register(new Setting("RainbowHue", 255, 0, 255, v -> rainbow.getValue()));

    @Override
    public void onRender3D(Render3DEvent event) {
        RayTraceResult ray = BlockHighlight.mc.objectMouseOver;
        if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos blockPos = ray.getBlockPos();
            RenderUtil.drawBoxESP(blockPos, rainbow.getValue() != false ? ColorUtil.rainbow(rainbowhue.getValue()) : new Color(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha()), false, rainbow.getValue() != false ? ColorUtil.rainbow(rainbowhue.getValue()) : new Color(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha()), width.getValue(), (mode.getValue() == Mode.Outline || mode.getValue() == Mode.Both), (mode.getValue() == Mode.Fill || mode.getValue() == Mode.Both), c.getValue().getAlpha(), false);
        }
    }
}