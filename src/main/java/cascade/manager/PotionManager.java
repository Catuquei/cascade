package cascade.manager;

import cascade.features.modules.hud.HUDManager;
import cascade.features.modules.hud.PotionInfo;
import com.mojang.realmsclient.gui.ChatFormatting;
import cascade.features.Feature;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PotionManager extends Feature {

    private final Map<EntityPlayer, PotionList> potions = new ConcurrentHashMap<>();

    public List<PotionEffect> getOwnPotions() {
        return this.getPlayerPotions(mc.player);
    }

    public List<PotionEffect> getPlayerPotions(EntityPlayer player) {
        PotionList list = this.potions.get(player);
        List<PotionEffect> potions = new ArrayList<>();
        if (list != null) {
            potions = list.getEffects();
        }
        return potions;
    }

    public PotionEffect[] getImportantPotions(EntityPlayer player) {
        PotionEffect[] array = new PotionEffect[3];
        for (PotionEffect effect : this.getPlayerPotions(player)) {
            Potion potion = effect.getPotion();
            switch (I18n.format(potion.getName()).toLowerCase()) {
                case "strength": {
                    array[0] = effect;
                }
                case "weakness": {
                    array[1] = effect;
                }
                case "speed": {
                    array[2] = effect;
                }
            }
        }
        return array;
    }

    public String getPotionString(PotionEffect effect) {
        Potion potion = effect.getPotion();
        return I18n.format(potion.getName()) + " " + (effect.getAmplifier() + 1) + " " + ChatFormatting.WHITE + Potion.getPotionDurationString(effect, 1.0f);
    }

    public String getPotionStringHUD(PotionEffect effect) {
        Potion potion = effect.getPotion();
        String string = potion.getName();
        if (PotionInfo.getInstance().shortNames.getValue()) {
            //todo
        }
        return I18n.format(string) + " " + (PotionInfo.getInstance().showLevel.getValue() ? (effect.getAmplifier() + 1) : "") + (PotionInfo.getInstance().colon.getValue() ? ": " : " ") + HUDManager.getInstance().infoColor.getValue() + Potion.getPotionDurationString(effect, 1.0f);
    }


    public String getColoredPotionString(PotionEffect effect) {
        return getPotionString(effect);
    }

    public String getColoredPotionStringHud(PotionEffect effect) {
        return getPotionStringHUD(effect);
    }

    public static class PotionList {
        private final List<PotionEffect> effects = new ArrayList<>();

        public void addEffect(PotionEffect effect) {
            if (effect != null) {
                this.effects.add(effect);
            }
        }

        public List<PotionEffect> getEffects() {
            return this.effects;
        }
    }
}