package cascade.features.modules.visual;

import cascade.features.modules.Module;
import cascade.features.setting.Setting;

public class ViewMod extends Module {

    public ViewMod() {
        super("ViewMod", Category.VISUAL, "View model changer");
        INSTANCE = this;
    }

    public Setting<Float> x = register(new Setting("X", 0.0f, -20.0f, 20.0f));
    public Setting<Float> y = register(new Setting("Y", 0.0f, -20.0f, 20.0f));
    public Setting<Float> z = register(new Setting("Z", 0.0f, -20.0f, 20.0f));
    public Setting<Float> sizeX = register(new Setting("SizeX", 1.0f, 0.0f, 10.0f));
    public Setting<Float> sizeY = register(new Setting("SizeY", 1.0f, 0.0f, 10.0f));
    public Setting<Float> sizeZ = register(new Setting("SizeZ", 1.0f, 0.0f, 10.0f));

    public Setting<Float> itemOpacity = register(new Setting("ItemOpacity", 255.0f, 0.0f, 255.0f));
    static ViewMod INSTANCE = new ViewMod();

    public static ViewMod getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ViewMod();
        }
        return INSTANCE;
    }
}