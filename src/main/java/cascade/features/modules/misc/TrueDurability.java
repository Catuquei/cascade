package cascade.features.modules.misc;

import cascade.Cascade;
import cascade.features.modules.Module;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.TextComponentString;

public class TrueDurability extends Module {

    public TrueDurability() {
        super("TrueDurability", Category.MISC, "Displays durability of unbreakables");
        INSTANCE = this;
    }

    private static TrueDurability INSTANCE;

    public static TrueDurability getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TrueDurability();
        }
        return INSTANCE;
    }
}
