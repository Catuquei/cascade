package cascade.features.modules.core;

import cascade.Cascade;
import cascade.event.events.ClientEvent;
import cascade.event.events.Render2DEvent;
import cascade.features.gui.CascadeGui;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.render.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;

import static net.minecraftforge.fml.client.config.GuiUtils.drawGradientRect;

public class ClickGui extends Module {


    Setting<Page> page = register(new Setting("Page", Page.General));
    enum Page {General, Categories, Modules,  Booleans, Enums}

    //general
    public Setting<Color> c = register(new Setting("Color", new Color(0x5F00E6), v -> page.getValue() == Page.General));
    public Setting<Color> background = register(new Setting("BackgroundColor", new Color(0x101010), v -> page.getValue() == Page.General));
    public Setting<Boolean> descriptions = register(new Setting("Descriptions", true, v -> page.getValue() == Page.General));
    Setting<Boolean> gradient = register(new Setting("Gradient", false, v -> page.getValue() == Page.General));
    Setting<Color> gradientBottom = register(new Setting("Bottom", new Color(-1), v -> page.getValue() == Page.General && gradient.getValue()));
    Setting<Color> gradientTop = register(new Setting("Top", new Color(0x000000), v -> page.getValue() == Page.General && gradient.getValue()));
    public Setting<Boolean> rainbow = register(new Setting("Rainbow", false, v -> page.getValue() == Page.General));
    public Setting<rainbowMode> rainbowModeHud = register(new Setting("HUD Mode", rainbowMode.Static, v -> page.getValue() == Page.General && rainbow.getValue()));
    public enum rainbowMode {Static, Sideway}
    public Setting<rainbowModeArray> rainbowModeA = register(new Setting("ArrayListMode", rainbowModeArray.Static, v -> page.getValue() == Page.General && rainbow.getValue()));
    public enum rainbowModeArray {Static, Up}
    public Setting<Integer> rainbowHue = register(new Setting("Delay", 240, 0, 600, v -> page.getValue() == Page.General && rainbow.getValue()));
    public Setting<Integer> rainbowBrightness = register(new Setting("Brightness ", 150, 0, 255, v -> page.getValue() == Page.General && rainbow.getValue()));
    public Setting<Integer> rainbowSaturation = register(new Setting("Saturation", 150, 0, 255, v -> page.getValue() == Page.General && rainbow.getValue()));

    //categories
    public Setting<Boolean> syncSize = register(new Setting("SyncSize", false, v -> page.getValue() == Page.Categories));
    public Setting<Color> categoryGradient = register(new Setting("CategoryGradient", new Color(0x181919), v -> page.getValue() == Page.Categories));
    public Setting<Color> categoryOutlineColor = register(new Setting("OutlineColor", new Color(0x7800FF), v -> page.getValue() == Page.Categories));
    public Setting<Color> categoryBoxColor = register(new Setting("BoxColor", new Color(0x7800FF), v -> page.getValue() == Page.Categories));

    //modules
    public Setting<Boolean> moduleOutline = register(new Setting("ModOutline", true, v -> page.getValue() == Page.Modules));
    public Setting<Color> moduleOutlineColor = register(new Setting("ModOutlineColor", new Color(0x181919), v -> page.getValue() == Page.Modules && moduleOutline.getValue()));
    public Setting<Color> moduleEnabledBox = register(new Setting("EnabledBox", new Color(0x7800FF), v -> page.getValue() == Page.Modules));
    public Setting<Color> moduleDisabledBox = register(new Setting("DisabledBox", new Color(0x181919), v -> page.getValue() == Page.Modules));
    public Setting<Color> moduleEnabledText = register(new Setting("EnabledText", new Color(0x7800FF), v -> page.getValue() == Page.Modules));
    public Setting<Color> moduleDisabledText = register(new Setting("DisabledText", new Color(-1), v -> page.getValue() == Page.Modules));
    public Setting<Color> gradientEnabled = register(new Setting("GradientEnabled", new Color(0x181919), v -> page.getValue() == Page.Modules));
    public Setting<Color> gradientDisabled = register(new Setting("GradientDisabled", new Color(0x0181919, true), v -> page.getValue() == Page.Modules));
    public Setting<Integer> hoverAlpha = register(new Setting("HoverAlpha", 255, 0, 255, v -> page.getValue() == Page.Modules));

    //booleans
    public Setting<Color> booleanOutline = register(new Setting("boolOutline", new Color(0x7800FF), v -> page.getValue() == Page.Booleans));
    public Setting<Color> booleanDisabled = register(new Setting("boolDisabledRect", new Color(0x181919), v -> page.getValue() == Page.Booleans));
    public Setting<Color> booleanDisabledOut = register(new Setting("boolDisabledOutl", new Color(0x282828), v -> page.getValue() == Page.Booleans));
    private static ClickGui INSTANCE = new ClickGui();

    //enums
    public Setting<Color> enumOutline = register(new Setting("EnumOutline", new Color(0x181919), v -> page.getValue() == Page.Enums));

    public ClickGui() {
        super("ClickGui", Category.CORE, "Client's Click GUI");
        setInstance();
        setBind(Keyboard.KEY_DOWN);
    }

    public static ClickGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGui();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting().getFeature().equals(this)) {
            Cascade.colorManager.setColor(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha());
        }
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        ScaledResolution resolution = new ScaledResolution(mc);
        if (mc.currentScreen instanceof CascadeGui && gradient.getValue()) {
            RenderUtil.drawGradient(0, 0, resolution.getScaledWidth(), resolution.getScaledHeight(), new Color(0, 0, 0, 0).getRGB(), gradientBottom.getValue().getRGB());
            RenderUtil.drawGradient(0, 0, resolution.getScaledWidth(), resolution.getScaledHeight(), gradientTop.getValue().getRGB(), new Color(0, 0, 0, 0).getRGB());
        }
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(CascadeGui.getClickGui());
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.closeScreen();
        }
    }

    @Override
    public void onLoad() {
        Cascade.colorManager.setColor(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha());
    }

    @Override
    public void onUpdate() {
        if (!fullNullCheck() && !(mc.currentScreen instanceof CascadeGui)) {
            disable();
        }
    }
}