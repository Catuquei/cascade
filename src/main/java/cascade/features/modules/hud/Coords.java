package cascade.features.modules.hud;

import cascade.Cascade;
import cascade.event.events.Render2DEvent;
import cascade.features.modules.Module;
import cascade.features.modules.core.ClickGui;
import cascade.features.setting.Setting;
import cascade.util.misc.MathUtil;
import cascade.util.render.ColorUtil;
import com.mojang.realmsclient.gui.ChatFormatting;

import java.awt.*;

public class Coords extends Module {

    public Coords() {
        super("Coords", Module.Category.HUD, "");
    }

    Setting<Boolean> coords = register(new Setting("Coords", true));
    Setting<Boolean> decimals = register(new Setting("Decimals", false, v -> coords.getValue()));
    Setting<Boolean> bothDimensions = register(new Setting("BothDimensions", true, v -> coords.getValue()));
    Setting<Xyz> xyz = register(new Setting("XYZ", Xyz.None, v -> coords.getValue()));
    enum Xyz {None, Start, Spread}

    Setting<Boolean> direction = register(new Setting("Direction", false));

    public void onRender2D(Render2DEvent e) {
        if (fullNullCheck()) {
            return;
        }
        int height = renderer.scaledHeight;
        int i = HUDManager.getInstance().i;
        i += 10;
        if (direction.getValue()) {
            renderer.drawString(ChatFormatting.WHITE + Cascade.rotationManager.getDirection4D(false), 2.0F, (height - i - 11), HUDManager.getInstance().getColor(), true);
        }
        if (coords.getValue()) {
            double x = MathUtil.round(mc.player.posX, decimals.getValue() ? 1 : 0);
            double y = MathUtil.round(mc.player.posY, decimals.getValue() ? 1 : 0);
            double z = MathUtil.round(mc.player.posZ, decimals.getValue() ? 1 : 0);
            double hX = MathUtil.round(mc.player.posX * (isInHell() ? 8.0d : 0.125d), decimals.getValue() ? 1 : 0);
            double hZ = MathUtil.round(mc.player.posZ * (isInHell() ? 8.0d : 0.125d), decimals.getValue() ? 1 : 0);
            //StringBuilder cordz = new StringBuilder(EntityNames.getName(target)).append(TextColor.GRAY).append(", ");
            String coordinates = ChatFormatting.WHITE + (isInHell() ? (x + ", " + y + ", " + z + " [" + hX + ", " + hZ + "]") : (x + ", " + y + ", " + z + " [" + x + ", " + z + "]"));
            renderer.drawString(coordinates, 2.0F, (height - i), ColorUtil.toRGBA(new Color(255, 255, 255, 255)), true);
        }
    }

    boolean isInHell() {
        return mc.world.getBiome(mc.player.getPosition()).getBiomeName().equals("Hell");
    }
}
