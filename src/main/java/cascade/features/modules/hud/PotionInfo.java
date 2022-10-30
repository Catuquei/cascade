package cascade.features.modules.hud;

import cascade.Cascade;
import cascade.event.events.Render2DEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class PotionInfo extends Module {

    public PotionInfo () {
        super("PotionInfo", Category.HUD, "");
        INSTANCE = this;
    }

    Setting<Page> page = register(new Setting("Page", Page.Potions));
    enum Page {Potions, Settings}
    public Setting<Boolean> showLevel = register(new Setting("ShowLevel", false, v -> page.getValue() == Page.Settings));
    Setting<Boolean> shortnames = register(new Setting("ShortNames", true));
    public Setting<Boolean> colon = register(new Setting("Colon", true, v -> page.getValue() == Page.Settings));
    public Setting<Boolean> shortNames = register(new Setting("ShortNames", true, v -> page.getValue() == Page.Settings));
    Setting<Boolean> syncHUD = register(new Setting("SyncHUD", true, v -> page.getValue() == Page.Settings));
    Setting<Integer> x = register(new Setting("X", 0, 0, 1000, v -> page.getValue() == Page.Settings && !syncHUD.getValue()));
    Setting<Integer> y = register(new Setting("Y", 0, 0, 1000, v -> page.getValue() == Page.Settings && !syncHUD.getValue()));
    static PotionInfo INSTANCE;

    public static PotionInfo getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PotionInfo();
        }
        return INSTANCE;
    }

    public void onRender2D(Render2DEvent e) {
        if (fullNullCheck()) {
            return;
        }
        int width = renderer.scaledWidth;
        int height = renderer.scaledHeight;
        int i = HUDManager.getInstance().i;
        String speed = "";
        String weankess = "";
        String strength = "";
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            speed = (shortNames.getValue() ? "Spd" : "Speed") + (showLevel.getValue() ? mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() : "");
        }
        if (mc.player.isPotionActive(MobEffects.WEAKNESS)) {
            weankess = (shortNames.getValue() ? "Wkns" : "Weakness") + (showLevel.getValue() ? mc.player.getActivePotionEffect(MobEffects.WEAKNESS).getAmplifier() : "");
            i += 10;
        }
        if (mc.player.isPotionActive(MobEffects.STRENGTH)) {
            strength = (shortNames.getValue() ? "Str" : "Strength") + (showLevel.getValue() ? mc.player.getActivePotionEffect(MobEffects.STRENGTH).getAmplifier() : "");
            i += 10;
        }
        List<PotionEffect> effects = new ArrayList(mc.player.getActivePotionEffects());
        for (PotionEffect potionEffect : effects) {
            if (potionEffect.getPotion().getName() == MobEffects.WEAKNESS.toString()) {
                i += 10;
            } else if (potionEffect.getPotion().getName() == MobEffects.SPEED.toString()) {
                i += 10;
            } else if (potionEffect.getPotion().getName() == MobEffects.STRENGTH.toString()) {
                i += 10;
            } else {
                return;
            }
            String string = Cascade.potionManager.getColoredPotionString(potionEffect);
            renderer.drawString(string, (width - renderer.getStringWidth(string) - 2), (2 + i++ * 10), potionEffect.getPotion().getLiquidColor(), true);
        }
        renderer.drawString(speed, syncHUD.getValue() ? (width - renderer.getStringWidth(speed) - 2) : x.getValue() + 0.0f, syncHUD.getValue() ? (HUDManager.getInstance().renderingUp.getValue() ? (height - 2 - i) :  (2 + i++ * 10)) : y.getValue() + 0.0f, syncHUD.getValue() ? HUDManager.getInstance().getColor() : HUDManager.INSTANCE.getColor(), true);
        renderer.drawString(weankess, syncHUD.getValue() ? (width - renderer.getStringWidth(weankess) - 2) : x.getValue() + 0.0f, syncHUD.getValue() ? (HUDManager.getInstance().renderingUp.getValue() ? (height - 2 - i) :  (2 + i++ * 10)) : y.getValue() + 0.0f, syncHUD.getValue() ? HUDManager.getInstance().getColor() : HUDManager.INSTANCE.getColor(), true);
        renderer.drawString(strength, syncHUD.getValue() ? (width - renderer.getStringWidth(strength) - 2) : x.getValue() + 0.0f, syncHUD.getValue() ? (HUDManager.getInstance().renderingUp.getValue() ? (height - 2 - i) :  (2 + i++ * 10)) : y.getValue() + 0.0f, syncHUD.getValue() ? HUDManager.getInstance().getColor() : HUDManager.INSTANCE.getColor(), true);
    }
}