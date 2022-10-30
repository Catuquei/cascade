package cascade.features.modules.visual;

import cascade.Cascade;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class Chams extends Module {

    public Chams() {
        super("Chams", Category.VISUAL, "Player chams");
        setInstance();
    }

    public Setting<Boolean> solid = register(new Setting("Solid", true));
    public Setting<Color> solidC = register(new Setting("SolidColor", new Color(-1)));

    public Setting<Boolean> wireframe = register(new Setting("Wireframe", true));
    public Setting<Color> wireC = register(new Setting("WireColor", new Color(-1)));
    public Setting<Float> lineWidth = register(new Setting("LineWidth",  1.0f,  0.1f,  3.0f));

    public Setting<Boolean> texture = register(new Setting("Texture", false));
    public Setting<Color> textureColor = register(new Setting("TextureColor", new Color(-1)));
    public Setting<Boolean> glint = register(new Setting("Glint", false));
    //public Setting<Double> scaleX = register(new Setting("ScaleX", 1.0, 0.0, 2.0));
    //public Setting<Double> scaleY = register(new Setting("ScaleY", 1.0, 0.0, 2.0));
    //public Setting<Double> scaleZ = register(new Setting("ScaleZ", 1.0, 0.0, 2.0));
    public Setting<Boolean> model = register(new Setting("Model", false));
    private static Chams INSTANCE;

    public static Chams getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new Chams();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        event.getEntityPlayer().hurtTime = 0;
    }
}