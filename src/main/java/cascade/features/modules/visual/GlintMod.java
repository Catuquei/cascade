package cascade.features.modules.visual;

import cascade.Cascade;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.TextComponentString;

import java.awt.*;

public class GlintMod extends Module {

    public GlintMod() {
        super("GlintMod", Category.VISUAL, "Changes enchantment glint color");
        INSTANCE = this;
    }

    public Setting<Color> c = register(new Setting("Color", new Color(-1)));
    public static GlintMod INSTANCE;

    public static GlintMod getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GlintMod();
        }
        return INSTANCE;
    }
}