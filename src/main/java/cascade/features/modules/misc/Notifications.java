package cascade.features.modules.misc;

import cascade.event.events.PacketEvent;
import cascade.features.command.Command;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.core.TextUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Notifications extends Module {

    public Notifications() {
        super("Notifications", Category.MISC, "notifs brah");
        INSTANCE = this;
    }

    Setting<Page> page = register(new Setting("Page", Page.Pop));
    enum Page {Pop, Ghast, Effect}

    Setting<Boolean> popCounter = register(new Setting("PopCounter", true, v -> page.getValue() == Page.Pop));
    Setting<TextUtil.Color> popMainColor = register(new Setting("PopMainColor", TextUtil.Color.LIGHT_PURPLE, v -> page.getValue() == Page.Pop && popCounter.getValue()));
    Setting<TextUtil.Color> popSecColor = register(new Setting("PopSecColor", TextUtil.Color.DARK_PURPLE, v -> page.getValue() == Page.Pop && popCounter.getValue()));
    HashMap<String, Integer> pops = new HashMap<>();

    Setting<Boolean> ghast = register(new Setting("Ghasts", true, v -> page.getValue() == Page.Ghast));
    Setting<Boolean> sound = register(new Setting("Sound", true, v -> page.getValue() == Page.Ghast && ghast.getValue()));
    Setting<TextUtil.Color> ghastMainColor = register(new Setting("GhastMainColor", TextUtil.Color.LIGHT_PURPLE, v -> page.getValue() == Page.Ghast && ghast.getValue()));
    Setting<TextUtil.Color> ghastSecColor = register(new Setting("GhastSecColor", TextUtil.Color.DARK_PURPLE, v -> page.getValue() == Page.Ghast && ghast.getValue()));
    Set<Entity> ghasts = new HashSet<>();

    Setting<Boolean> effects = register(new Setting("Effects", true, v -> page.getValue() == Page.Effect));
    Setting<Boolean> ignoreSelf = register(new Setting("IgnoreSelf", false, v -> page.getValue() == Page.Effect && effects.getValue()));
    Setting<TextUtil.Color> effectMainColor = register(new Setting("EffectMainColor", TextUtil.Color.LIGHT_PURPLE, v -> page.getValue() == Page.Effect && effects.getValue()));
    Setting<TextUtil.Color> effectSecColor = register(new Setting("EffectSecColor", TextUtil.Color.DARK_PURPLE, v -> page.getValue() == Page.Effect && effects.getValue()));
    private static Notifications INSTANCE;

    public static Notifications getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new Notifications();
        }
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        pops.clear();
        ghasts.clear();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive e) {
        if (isDisabled() || fullNullCheck()) {
            return;
        }
        if (effects.getValue() && e.getPacket() instanceof SPacketEntityEffect) {
            SPacketEntityEffect p = e.getPacket();
            if (Potion.getPotionById(p.getEffectId()) == MobEffects.ABSORPTION || Potion.getPotionById(p.getEffectId()) == MobEffects.REGENERATION || Potion.getPotionById(p.getEffectId()) == MobEffects.RESISTANCE || Potion.getPotionById(p.getEffectId()) == MobEffects.FIRE_RESISTANCE) {
                return;
            }
            for (Entity en : mc.world.loadedEntityList) {
                if (en instanceof EntityPlayer && en.getEntityId() == p.getEntityId()) {
                    boolean isSelf = en.getEntityId() == mc.player.getEntityId();
                    if (isSelf && ignoreSelf.getValue()) {
                        return;
                    }
                    //Command.sendMessage(TextUtil.convertColorName(effectMainColor.getValue()) + (isSelf ? "You" : en.getName()) + " " + TextUtil.convertColorName(effectSecColor.getValue()) + "have ran out of " + TextUtil.convertColorName(effectSecColor.getValue()) + Potion.getPotionById(p.getEffectId()));
                    Command.sendMessage(TextUtil.convertColorName(effectMainColor.getValue()) + (isSelf ? "You" : en.getName()) + " " + TextUtil.convertColorName(effectSecColor.getValue()) + (isSelf ? "have" : "has") + " received " + TextUtil.convertColorName(effectSecColor.getValue()) + ((EntityPlayer) en).getActivePotionEffect(Potion.getPotionById(p.getEffectId())));
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck() || !ghast.getValue()) {
            return;
        }
        //todo make my own brah spacketspawnobject
        for (Entity e : mc.world.getLoadedEntityList()) {
            if (e instanceof EntityGhast && !ghasts.contains(e)) {
                int x = (int) e.posX;
                int y = (int) e.posY;
                int z = (int) e.posZ;
                Command.sendMessage(TextUtil.convertColorName(ghastMainColor.getValue()) + "Ghast" + TextUtil.convertColorName(ghastSecColor.getValue()) + " has spawned at " + TextUtil.convertColorName(ghastMainColor.getValue()) + x + "x " + y + "y " + z + "z");
                ghasts.add(e);
                if (sound.getValue()) {
                    mc.player.playSound(SoundEvents.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);
                }
            }
        }
    }

    public void onDeath(EntityPlayer player) {
        if (pops.containsKey(player.getName())) {
            int i = pops.get(player.getName());
            pops.remove(player.getName());
            if (isEnabled() && popCounter.getValue()) {
                if (player == mc.player) {
                    Command.sendRemovableMessage(TextUtil.convertColorName(popSecColor.getValue()) + "You" + TextUtil.convertColorName(popMainColor.getValue()) + " died after popping " + TextUtil.convertColorName(popSecColor.getValue()) + i + TextUtil.convertColorName(popMainColor.getValue()) + (i == 1 ? (" totem") : (" totems")), -42069);
                } else {
                    Command.sendRemovableMessage(TextUtil.convertColorName(popSecColor.getValue()) + player.getName() + TextUtil.convertColorName(popMainColor.getValue()) + " died after popping " + TextUtil.convertColorName(popSecColor.getValue()) + i + TextUtil.convertColorName(popMainColor.getValue()) + (i == 1 ? (" totem") : (" totems")), -42069);
                }
            }
        }
    }

    public void onTotemPop(EntityPlayer player) {
        if (fullNullCheck()) {
            return;
        }
        int i = 1;
        if (pops.containsKey(player.getName())) {
            i = pops.get(player.getName());
            pops.put(player.getName(), ++i);
        } else {
            pops.put(player.getName(), i);
        }
        if (isEnabled() && popCounter.getValue()) {
            if (player == mc.player) {
                Command.sendRemovableMessage(TextUtil.convertColorName(popSecColor.getValue()) + "You" + TextUtil.convertColorName(popMainColor.getValue()) + " have popped your " + TextUtil.convertColorName(popSecColor.getValue()) + i + suffix(i) + TextUtil.convertColorName(popMainColor.getValue()) + " totem", -42069);
            } else {
                Command.sendRemovableMessage(TextUtil.convertColorName(popSecColor.getValue()) + player.getName() + TextUtil.convertColorName(popMainColor.getValue()) + " has popped their " + TextUtil.convertColorName(popSecColor.getValue()) + i + suffix(i) + TextUtil.convertColorName(popMainColor.getValue()) + " totem", -1337);
            }
        }
    }

    String suffix(int num) {
        if (num == 1) {
            return "st";
        }
        if (num == 2) {
            return "nd";
        }
        if (num == 3) {
            return "rd";
        }
        if (num >= 4 && num < 21) {
            return "th";
        }
        int lastDigit = num % 10;
        if (lastDigit == 1) {
            return "st";
        }
        if (lastDigit == 2) {
            return "nd";
        }
        if (lastDigit == 3) {
            return "rd";
        }
        return "th";
    }
}