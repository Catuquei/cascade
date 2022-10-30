package cascade.features.modules.player;

import cascade.event.events.PacketEvent;
import cascade.event.events.PushEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.misc.MathUtil;
import cascade.util.player.MovementUtil;
import cascade.util.player.TargetUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Freecam extends Module {

    public Freecam() {
        super("Freecam", Category.PLAYER, "");
        setInstance();
    }

    Setting<Float> speed = register(new Setting("Speed", 0.2f, 0.1f, 5.0f));
    Setting<Boolean> view = register(new Setting("3D", false));
    Setting<Boolean> packet = register(new Setting("Packet", true));
    Setting<Boolean> legit = register(new Setting("Legit", false));
    Setting<Boolean> feetTeleport = register(new Setting("TargetFeetTP", false));
    Setting<Boolean> copyYawPitch = register(new Setting("CopyYawPitch", false, v -> feetTeleport.getValue()));
    AxisAlignedBB oldBoundingBox;
    public static EntityOtherPlayerMP entity;
    static Freecam INSTANCE;
    Vec3d position;
    Entity riding;
    float yaw;
    float pitch;

    private void setInstance() {
        INSTANCE = this;
    }

    public static Freecam getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Freecam();
        }
        return INSTANCE;
    }

    public static EntityOtherPlayerMP getFreecamEntity() {
        if (INSTANCE.isEnabled() && entity != null) {
            return entity;
        }
        return null;
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }
        oldBoundingBox = mc.player.getEntityBoundingBox();
        mc.player.setEntityBoundingBox(new AxisAlignedBB(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.posX, mc.player.posY, mc.player.posZ));
        if (mc.player.getRidingEntity() != null) {
            riding = mc.player.getRidingEntity();
            mc.player.dismountRidingEntity();
        }
        (entity = new EntityOtherPlayerMP(mc.world, mc.session.getProfile())).copyLocationAndAnglesFrom(mc.player);
        entity.rotationYaw = mc.player.rotationYaw;
        entity.rotationYawHead = mc.player.rotationYawHead;
        entity.inventory.copyInventory(mc.player.inventory);
        mc.world.addEntityToWorld(726804364, entity);
        position = mc.player.getPositionVector();
        yaw = mc.player.rotationYaw;
        pitch = mc.player.rotationPitch;
        mc.player.noClip = true;
        if (feetTeleport.getValue()) {
            EntityPlayer target = TargetUtil.getTarget(7.5d);
            if (target != null) {
                mc.player.setPosition(target.posX, target.posY - 1.0d, target.posZ);
                if (copyYawPitch.getValue()) {
                    mc.player.rotationYaw = target.rotationYaw;
                    mc.player.rotationPitch = target.rotationPitch;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) {
            return;
        }

        mc.player.setEntityBoundingBox(oldBoundingBox);
        if (riding != null) {
            mc.player.startRiding(riding, true);
        }
        if (entity != null) {
            mc.world.removeEntity(entity);
        }
        if (position != null) {
            mc.player.setPosition(position.x, position.y, position.z);
        }
        mc.player.rotationYaw = yaw;
        mc.player.rotationPitch = pitch;
        mc.player.noClip = false;
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        mc.player.noClip = true;
        mc.player.setVelocity(0.0, 0.0, 0.0);
        mc.player.jumpMovementFactor = speed.getValue();
        double[] dir = MathUtil.directionSpeed(speed.getValue());
        if (MovementUtil.isMoving()) {
            MovementUtil.setMotion(dir[0], mc.player.motionY, dir[1]);
        } else {
            mc.player.motionX = 0.0;
            mc.player.motionZ = 0.0;
        }
        mc.player.setSprinting(false);
        if (view.getValue() && !mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.motionY = speed.getValue() * -MathUtil.degToRad(mc.player.rotationPitch) * mc.player.movementInput.moveForward;
        }
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.motionY += speed.getValue();
        }
        if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.player.motionY -= speed.getValue();
        }
    }

    @Override
    public void onLogout() {
        if (isEnabled()) {
            disable();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send e) {
        if (isDisabled()) {
            return;
        }
        if (legit.getValue() && entity != null && e.getPacket() instanceof CPacketPlayer) {
            ((CPacketPlayer) e.getPacket()).x = entity.posX;
            ((CPacketPlayer) e.getPacket()).y = entity.posY;
            ((CPacketPlayer) e.getPacket()).z = entity.posZ;
            return;
        }
        if (packet.getValue()) {
            if (e.getPacket() instanceof CPacketPlayer) {
                e.setCanceled(true);
            }
        } else if (!(e.getPacket() instanceof CPacketUseEntity) && !(e.getPacket() instanceof CPacketPlayerTryUseItem) && !(e.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) && !(e.getPacket() instanceof CPacketPlayer) && !(e.getPacket() instanceof CPacketVehicleMove) && !(e.getPacket() instanceof CPacketChatMessage) && !(e.getPacket() instanceof CPacketKeepAlive)) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive e) {
        if (isDisabled()) {
            return;
        }
        if (e.getPacket() instanceof SPacketSetPassengers) {
            Entity riding = mc.world.getEntityByID(((SPacketSetPassengers) e.getPacket()).getEntityId());
            if (riding != null && riding == this.riding) {
                this.riding = null;
            }
        }
        if (e.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook p = e.getPacket();
            if (packet.getValue()) {
                if (entity != null) {
                    entity.setPositionAndRotation(p.getX(), p.getY(), p.getZ(), p.getYaw(), p.getPitch());
                }
                position = new Vec3d(p.getX(), p.getY(), p.getZ());
                mc.getConnection().sendPacket(new CPacketConfirmTeleport(p.getTeleportId()));
                e.setCanceled(true); //yoo ? oyo
            } else {
                e.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPush(PushEvent e) {
        if (e.getStage() == 1 && isEnabled()) {
            e.setCanceled(true);
        }
    }
}