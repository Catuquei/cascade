package cascade.features.modules.visual;

import cascade.Cascade;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.TextComponentString;

public class CameraClip extends Module {

    public Setting<Boolean> extend = register(new Setting("Extend", false));
    public Setting<Double> distance = register(new Setting<Object>("Distance", 4.2, 0.0, 50.0, v -> extend.getValue()));
    private static CameraClip INSTANCE = new CameraClip();

    public CameraClip() {
        super("CameraClip", Category.VISUAL, "yall know");
        setInstance();
    }

    public static CameraClip getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CameraClip();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }
}