package cascade.features.modules.visual;

import cascade.Cascade;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.TextComponentString;

import java.awt.*;

public class HandChams extends Module {

    public Setting<RenderMode> mode = register(new Setting("Mode", RenderMode.Wireframe));
    public enum RenderMode {Solid, Wireframe}
    public Setting<Color> c = register(new Setting("Color", new Color(-1)));
    private static HandChams INSTANCE;

    public HandChams() {
        super("HandChams", Category.VISUAL, "Changes the look of ur hand");
        this.setInstance();
    }

    public static HandChams getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new HandChams();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }
}