package cascade.features.modules.player;

import cascade.Cascade;
import cascade.features.modules.Module;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.TextComponentString;

public class LiquidInteract extends Module {
    private static LiquidInteract INSTANCE;

    public LiquidInteract() {
        super("LiquidInteract", Category.PLAYER, "lets u interact with ");
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static LiquidInteract getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LiquidInteract();
        }
        return INSTANCE;
    }
}