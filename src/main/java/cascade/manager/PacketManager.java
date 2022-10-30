package cascade.manager;

import cascade.event.events.PacketEvent;
import cascade.features.Feature;
import cascade.features.modules.core.TimingManager;
import cascade.util.misc.Timer;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.server.*;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class PacketManager extends Feature {

    SPacketExplosion pExplosion;
    RayTraceResult raytrace;
    Timer timerExplosion;
    boolean caughtPExplosion;

    SPacketPlayerPosLook pPlayerPosLook;
    Timer timerPlayerPosLook;
    boolean caughtPlayerPosLook;

    SPacketEntityStatus pEntityStatus;
    Timer timerEntityStatus;
    boolean caughtEntityStatus;

    public PacketManager() {
        pExplosion = null;
        raytrace = null;
        timerExplosion = new Timer();
        caughtPExplosion = false;

        pPlayerPosLook = null;
        timerPlayerPosLook = new Timer();
        caughtPlayerPosLook = false;

        pEntityStatus = null;
        timerEntityStatus = new Timer();
        caughtEntityStatus = false;
    }

    public void load() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void unload() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketReceive(PacketEvent.Receive e) {
        if (fullNullCheck() || e.getPacket() == null) {
            return;
        }
        if (e.getPacket() instanceof SPacketPlayerPosLook) {
            pPlayerPosLook = e.getPacket();
            timerPlayerPosLook.reset();
            caughtPlayerPosLook = true;
        }

        if (e.getPacket() instanceof SPacketExplosion) { //str 1.0 = wither/withers projectile, str 6.0 = crystal, str 4.0 = tnt
            pExplosion = e.getPacket();
            raytrace = mc.world.rayTraceBlocks(mc.player.getPositionVector(), new Vec3d(pExplosion.getX(), pExplosion.getY(), pExplosion.getZ()), false, false, false);
            timerExplosion.reset();
            caughtPExplosion = true;
        }

        if (e.getPacket() instanceof SPacketEntityStatus) {
            pEntityStatus = e.getPacket();
            timerEntityStatus.reset();
            caughtEntityStatus = true;
        }
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent e) {
        if (fullNullCheck()) {
            return;
        }
        if (timerExplosion.passedMs((long) TimingManager.getInstance().getExplosion())) {
            raytrace = null;
            pExplosion = null;
            caughtPExplosion = false;
        }
        if (timerPlayerPosLook.passedMs((long) TimingManager.getInstance().getLagBack())) {
            pPlayerPosLook = null;
            caughtPlayerPosLook = false;
        }
        if (timerEntityStatus.passedMs((long) TimingManager.getInstance().getKnockback())) {
            pEntityStatus = null;
            caughtEntityStatus = false;
        }
    }

    /**
     * PlayerPosLook
     */

    public SPacketPlayerPosLook getPacketPPS() {
        return pPlayerPosLook;
    }

    public Timer getTimerPPS() {
        return timerPlayerPosLook;
    }

    public boolean getCaughtPPS() {
        return caughtPlayerPosLook;
    }

    /**
     * Explosion
     */

    public SPacketExplosion getPacketE() {
        return pExplosion;
    }

    public RayTraceResult getRaytrace() {
        return raytrace;
    }

    public Timer getTimerE() {
        return timerExplosion;
    }

    public boolean getCaughtE() {
        return caughtPExplosion;
    }

    public boolean raytraceCheck() { //originally i had +eyehight to the player
        try {
            return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ), new Vec3d(pExplosion.getX(), pExplosion.getY(), pExplosion.getZ()), false, true, false) == null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean isValid() {
        if (mc.player != null && caughtPExplosion && pExplosion.getStrength() == 6.0f && mc.player.getDistance(pExplosion.getX(), pExplosion.getY(), pExplosion.getZ()) <= 12.0d && raytraceCheck()) {
            return true;
        }
        return false;
    }

    /**
     * EntityStatus
     */

    public SPacketEntityStatus getPacketES() {
        return pEntityStatus;
    }

    public Timer getTimerES() {
        return timerEntityStatus;
    }

    public boolean getCaughtES() {
        return caughtEntityStatus;
    }

    public boolean isValidEntityStatus() {
        try {
            return !fullNullCheck() && caughtEntityStatus && pEntityStatus.getEntity(mc.world).getEntityId() == mc.player.getEntityId();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}