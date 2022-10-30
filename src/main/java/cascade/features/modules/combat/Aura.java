package cascade.features.modules.combat;

import cascade.Cascade;
import cascade.event.events.UpdateWalkingPlayerEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.entity.CombatUtil;
import cascade.util.entity.EntityUtil;
import cascade.util.misc.MathUtil;
import cascade.util.misc.Timer;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Aura extends Module {

    public Aura() {
        super("Aura", Category.COMBAT, "");
        INSTANCE = this;
    }

    public static Entity target;
    Timer timer = new Timer();
    Setting<Float> range = register(new Setting("Range", 6.0f, 0.1f, 6.0f));
    Setting<Float> wallRange = register(new Setting("WallRange", 3.0f, 0.1f, 6.0f));
    Setting<Boolean> swordOnly = register(new Setting("SwordOnly", true));
    Setting<Boolean> delay = register(new Setting("Delay", true));
    Setting<Boolean> rotate = register(new Setting("Rotate", false));
    Setting<Boolean> tps = register(new Setting("TpsSync", true));
    static Aura INSTANCE;

    public static Aura getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Aura();
        }
        return INSTANCE;
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (!CombatUtil.holdingWeapon() && swordOnly.getValue()) {
            return;
        }
        int wait = !delay.getValue() ? 0 : (int) (CombatUtil.getCooldownByWeapon(mc.player) * (tps.getValue() ? Cascade.serverManager.getTpsFactor() : 1.0f));
        if (!timer.passedMs(wait)) {
            return;
        }
        target = getTarget();
        if (target == null) {
            return;
        }
        if (rotate.getValue()) {
            Cascade.rotationManager.lookAtEntity(target);
        }
        mc.getConnection().sendPacket(new CPacketUseEntity(target));
        timer.reset();
    }

    Entity getTarget() {
        Entity target = null;
        double distance = range.getValue();
        double maxHealth = 36.0d;
        for (Entity e : mc.world.playerEntities) {
            if (e instanceof EntityPlayer) {
                if (EntityUtil.isntValid(e, distance)) {
                    continue;
                }
                if (!mc.player.canEntityBeSeen(e) && !EntityUtil.canEntityFeetBeSeen(e) && mc.player.getDistance(e) > wallRange.getValue()) {
                    continue;
                }
                if (target == null) {
                    target = e;
                    distance = mc.player.getDistance(e);
                    maxHealth = EntityUtil.getHealth(e);
                    continue;
                }
                if (mc.player.getDistance(e) < distance) {
                    target = e;
                    distance = mc.player.getDistanceSq(e);
                    maxHealth = EntityUtil.getHealth(e);
                }
                if (EntityUtil.getHealth(e) < maxHealth) {
                    target = e;
                    distance = mc.player.getDistanceSq(e);
                    maxHealth = EntityUtil.getHealth(e);
                }
            }
        }
        return target;
    }
}