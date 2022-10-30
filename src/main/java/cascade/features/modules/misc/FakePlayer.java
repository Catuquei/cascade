package cascade.features.modules.misc;

import com.mojang.authlib.GameProfile;
import cascade.event.events.PacketEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

/**
 * code is dogshit
 */

public class FakePlayer extends Module {

    public FakePlayer() {
        super("FakePlayer", Category.MISC, "Spawns in a fake player");
    }

    public Setting<Boolean> inv = register(new Setting("Inv", false));
    public Setting<Boolean> pop = register(new Setting("Pop", false));
    public Setting<String> plrName = register(new Setting("Name", "Subhuman"));
    private EntityOtherPlayerMP fake_player;

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }

        fake_player = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("ee11ee92-8148-47e8-b416-72908a6a2275"), plrName.getValue()));
        fake_player.copyLocationAndAnglesFrom(mc.player);
        fake_player.rotationYawHead = mc.player.rotationYawHead;
        if (inv.getValue()) {
            fake_player.inventory = mc.player.inventory;
        }
        fake_player.setHealth(36);
        mc.world.addEntityToWorld(-100, fake_player);
    }

    @Override
    public void onLogout() {
        if (isEnabled()) {
            disable();
        }
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) {
            return;
        }
        try {
            mc.world.removeEntity(fake_player);
        } catch (Exception ignored) {}
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (pop.getValue() && isEnabled() && !fullNullCheck()) {
            if (event.getPacket() instanceof SPacketDestroyEntities) {
                SPacketDestroyEntities packet = event.getPacket();
                for (int id : packet.getEntityIDs()) {
                    Entity entity = mc.world.getEntityByID(id);
                    if (entity instanceof EntityEnderCrystal) {
                        if (entity.getDistanceSq(fake_player) < 144) {
                            final float rawDamage = calculateDamage(entity.posX, entity.posY, entity.posZ, fake_player);
                            final float absorption = fake_player.getAbsorptionAmount() - rawDamage;
                            final boolean hasHealthDmg = absorption < 0;
                            final float health = fake_player.getHealth() + absorption;

                            if (hasHealthDmg && health > 0) {
                                fake_player.setHealth(health);
                                fake_player.setAbsorptionAmount(0);
                            } else if (health > 0) {
                                fake_player.setAbsorptionAmount(absorption);
                            } else {
                                fake_player.setHealth(2);
                                fake_player.setAbsorptionAmount(8);
                                fake_player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 5));
                                fake_player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 1));

                                try {
                                    mc.player.connection.handleEntityStatus(new SPacketEntityStatus(fake_player, (byte) 35));
                                } catch (Exception e) {}
                            }

                            fake_player.hurtTime = 5;
                        }
                    }
                }
            }
        }
    }
    public static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        float doubleExplosionSize = 12.0f;
        double distancedsize = entity.getDistance(posX, posY, posZ) / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = 0.0;
        try {
            blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        } catch (Exception exception) {}
        double v = (1.0 - distancedsize) * blockDensity;
        float damage = (int) ((v * v + v) / 2.0 * 7.0 * (double) doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            finald = getBlastReduction((EntityLivingBase) entity, getDamageMultiplied(damage), new Explosion(mc.world, null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float) finald;
    }
    public static float getBlastReduction(EntityLivingBase entity, float damageI, Explosion explosion) {
        float damage = damageI;
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) entity;
            DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(), (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            int k = 0;
            try {
                k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            } catch (Exception exception) {}
            float f = MathHelper.clamp((float) k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.isPotionActive(MobEffects.RESISTANCE)) {
                damage -= damage / 4.0f;
            }
            damage = Math.max(damage, 0.0f);
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    public static float getDamageMultiplied(float damage) {
        int diff = mc.world.getDifficulty().getId();
        return damage * (diff == 0 ? 0.0f : (diff == 2 ? 1.0f : (diff == 1 ? 0.5f : 1.5f)));
    }
}