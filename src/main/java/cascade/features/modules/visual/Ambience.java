package cascade.features.modules.visual;

import cascade.Cascade;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.TextComponentString;

import java.awt.*;

public class Ambience extends Module {

    public Ambience() {
        super("Ambience", Category.VISUAL, "Ambience tweaks");
        INSTANCE = this;
    }

    public Setting<Color> c = register(new Setting("Color", new Color(-1)));
    static Ambience INSTANCE;


    public static Ambience getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Ambience();
        }
        return INSTANCE;
    }
}