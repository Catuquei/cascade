package cascade.features.modules.core;

import cascade.Cascade;
import cascade.event.events.ClientEvent;
import cascade.features.command.Command;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class FontMod extends Module {

    public FontMod() {
        super("FontMod", Category.CORE, "Changes the font");
        INSTANCE = this;
    }

    private static FontMod INSTANCE = new FontMod();
    public Setting<String> fontName = register(new Setting("FontName", "Arial"));
    public Setting<Boolean> antiAlias = register(new Setting("AntiAlias", true));
    public Setting<Boolean> fractionalMetrics = register(new Setting("Metrics", true));
    public Setting<Integer> fontSize = register(new Setting("Size", 18, 12, 30));
    public Setting<Integer> fontStyle = register(new Setting("Style", 0, 0, 3));
    public Setting<Boolean> customAll = register(new Setting("Full", true));
    private boolean reloadFont = false;

    public static FontMod getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FontMod();
        }
        return INSTANCE;
    }

    public static boolean checkFont(String font, boolean message) {
        String[] fonts;
        for (String s : fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if (!message && s.equals(font)) {
                return true;
            }
            if (!message) {
                continue;
            }
            Command.sendMessage(s, true, false);
        }
        return false;
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        Setting setting;
        if (event.getStage() == 2 && (setting = event.getSetting()) != null && setting.getFeature().equals(this)) {
            if (setting.getName().equals("FontName") && !FontMod.checkFont(setting.getPlannedValue().toString(), false)) {
                Command.sendMessage(ChatFormatting.RED + "That font doesnt exist.", true, false);
                event.setCanceled(true);
                return;
            }
            reloadFont = true;
        }
    }

    @Override
    public void onTick() {
        if (reloadFont) {
            Cascade.textManager.init(false);
            reloadFont = false;
        }
    }
}