package cascade.features.modules.hud;

import cascade.features.modules.Module;
import cascade.features.modules.exploit.FastMotion;
import cascade.features.setting.Setting;
import cascade.util.core.TextUtil;
import cascade.util.render.ColorUtil;

import java.awt.*;

public class HUDManager extends Module {

    public HUDManager() {
        super("HUDManager", Category.HUD, "");
        INSTANCE = this;
    }

    public Setting<Boolean> renderingUp = register(new Setting("RenderingUp", false));
    public Setting<TextUtil.Color> infoColor = register(new Setting("InfoColor", TextUtil.Color.GRAY));
    public Setting<Color> c = register(new Setting("Color", new Color(120, 0, 255, 255)));
    public int i = (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat && renderingUp.getValue()) ? 13 : (renderingUp.getValue() ? -2 : 0);
    static HUDManager INSTANCE;

    public static HUDManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HUDManager();
        }
        return INSTANCE;
    }

    public int getColor() {
        return ColorUtil.toRGBA(new Color(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha()));
    }

    public int getRed() {
        return c.getValue().getRed();
    }

    public int getGreen() {
        return c.getValue().getGreen();
    }

    public int getBlue() {
        return c.getValue().getBlue();
    }

    public int getAlpha() {
        return c.getValue().getAlpha();
    }
}