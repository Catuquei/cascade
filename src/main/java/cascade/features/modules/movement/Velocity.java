package cascade.features.modules.movement;

import cascade.event.events.PacketEvent;
import cascade.event.events.PushEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Velocity extends Module {

    public Velocity() {
        super("Velocity", Category.MOVEMENT, "Player Tweaks");
        INSTANCE = this;
    }

    public Setting<Boolean> knockBack = register(new Setting("KnockBack", true));
    public Setting<Boolean> noPush = register(new Setting("NoPush", true));
    public Setting<Boolean> bobbers = register(new Setting("Bobbers", true));
    public Setting<Boolean> water = register(new Setting("Water", false));
    public Setting<Boolean> blocks = register(new Setting("Blocks", false));
    public double mX = 0.0d;
    public double mY = 0.0d;
    public double mZ = 0.0d;
    static Velocity INSTANCE;

    public static Velocity getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new Velocity();
        }
        return INSTANCE;
    }

    @Override
    public void onDisable() {
        Blocks.ICE.slipperiness = 0.98f;
        Blocks.PACKED_ICE.slipperiness = 0.98f;
        Blocks.FROSTED_ICE.slipperiness = 0.98f;
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive e) {
        if (isDisabled()) {
            return;
        }
        if (e.getStage() == 0 && mc.player != null) {
            if (knockBack.getValue() && e.getPacket() instanceof SPacketEntityVelocity) {
                SPacketEntityVelocity p = e.getPacket();
                if (p.getEntityID() == mc.player.entityId) {
                    mX = p.getMotionX();
                    mY = p.getMotionY();
                    mZ = p.getMotionZ();
                    e.setCanceled(true);
                }
            }
            if (e.getPacket() instanceof SPacketEntityStatus && bobbers.getValue()) {
                SPacketEntityStatus packet = e.getPacket();
                if (packet.getOpCode() == 31) {
                    Entity entity = packet.getEntity(mc.world);
                    if (entity instanceof EntityFishHook) {
                        EntityFishHook fishHook = (EntityFishHook)entity;
                        if (fishHook.caughtEntity == mc.player) {
                            e.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPush(PushEvent e) {
        if (isDisabled()) {
            return;
        }
        if (e.getStage() == 0 && noPush.getValue() && e.entity == mc.player) {
            e.setCanceled(true);
        } else if (e.getStage() == 1 && blocks.getValue()) {
            e.setCanceled(true);
        } else if (e.getStage() == 2 && water.getValue() && mc.player != null && mc.player == e.entity) {
            e.setCanceled(true);
        }
    }
}