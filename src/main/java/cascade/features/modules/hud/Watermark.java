package cascade.features.modules.hud;

import cascade.Cascade;
import cascade.event.events.Render2DEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import com.mojang.realmsclient.gui.ChatFormatting;

public class Watermark extends Module {

    public Watermark() {
        super("Watermark", Category.HUD, "");
    }

    Setting<String> text = register(new Setting("Text", "Cascade"));
    Setting<Version> version = register(new Setting("Version", Version.None));
    enum Version {None, Version, Build, Beta}
    Setting<Boolean> gray = register(new Setting("Gray", true, v -> version.getValue() != Version.None));

    public void onRender2D(Render2DEvent e) {
        if (fullNullCheck()) {
            return;
        }
        String vers = null;
        switch (version.getValue()) {
            case None: {
                vers = "";
                break;
            }
            case Version: {
                vers = " v" + Cascade.MODVER;
                break;
            }
            case Build: {
                vers = " b" + Cascade.MODVER;
                break;
            }
            case Beta: {
                vers = " " + Cascade.MODVER + "-beta";
                break;
            }
        }
        String watermark = text.getValue() + (gray.getValue() ? ChatFormatting.DARK_GRAY : "") + vers;
        renderer.drawString(watermark, 2.0f, 1.0f, HUDManager.getInstance().getColor(), true);
    }
}