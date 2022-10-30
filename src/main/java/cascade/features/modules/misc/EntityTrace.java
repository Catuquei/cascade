package cascade.features.modules.misc;

import cascade.features.modules.Module;
import cascade.features.setting.Setting;

public class EntityTrace extends Module {

    public EntityTrace() {
        super("EntityTrace", Module.Category.MISC, "Removes entities hitboxes");
        INSTANCE = this;
    }

    private static EntityTrace INSTANCE;
    public Setting<Boolean> pickaxe = register(new Setting("Pickaxe", true));
    public Setting<Boolean> crystal = register(new Setting("Crystal", true));
    public Setting<Boolean> gapple = register(new Setting("Gapple", true));

    public static EntityTrace getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new EntityTrace();
        }
        return INSTANCE;
    }
}