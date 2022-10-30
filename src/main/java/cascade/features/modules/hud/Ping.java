package cascade.features.modules.hud;

import cascade.Cascade;
import cascade.event.events.Render2DEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.core.TextUtil;

public class Ping extends Module {

    public Ping() {
        super("Ping", Category.HUD, "");
    }

    Setting<Boolean> ms = register(new Setting("Ms", false));
    Setting<Boolean> colon = register(new Setting("Colon", true));
    Setting<Boolean> syncHUD = register(new Setting("SyncHUD", true));
    Setting<Integer> x = register(new Setting("X", 0, 0, 1000, v -> !syncHUD.getValue()));
    Setting<Integer> y = register(new Setting("Y", 0, 0, 1000, v -> !syncHUD.getValue()));

    public void onRender2D(Render2DEvent e) {
        if (fullNullCheck()) {
            return;
        }
        int width = renderer.scaledWidth;
        int height = renderer.scaledHeight;
        int i = HUDManager.getInstance().i;
        //todo unchinese it
        String start = colon.getValue() ? (ms.getValue() ? "Ms: " : "Ping: ") : (ms.getValue() ? "Ms " : "Ping ");
        String ping = start + TextUtil.coloredString(String.valueOf(Cascade.serverManager.getPing()), HUDManager.getInstance().infoColor.getValue());
        i += 10;
        renderer.drawString(ping, syncHUD.getValue() ? (width - renderer.getStringWidth(ping) - 2) : x.getValue() + 0.0f, syncHUD.getValue() ? (HUDManager.getInstance().renderingUp.getValue() ? (height - 2 - i) :  (2 + i++ * 10)) : y.getValue() + 0.0f, HUDManager.getInstance().getColor(), true);
    }
}