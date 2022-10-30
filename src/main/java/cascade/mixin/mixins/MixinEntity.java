package cascade.mixin.mixins;

import cascade.event.events.PushEvent;
import cascade.event.events.StepEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ Entity.class })
public abstract class MixinEntity {

    @Shadow
    public double field_70165_t;
    @Shadow
    public double field_70163_u;
    @Shadow
    public double field_70167_r;
    @Shadow
    public double field_70137_T;
    @Shadow
    public float field_70126_B;
    @Shadow
    public abstract AxisAlignedBB getEntityBoundingBox();
    @Shadow
    public float stepHeight;

    private Float prevHeight;

    @Redirect(method = { "applyEntityCollision" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void addVelocityHook(final Entity entity, final double x, final double y, final double z) {
        PushEvent event = new PushEvent(entity, x, y, z, true);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            entity.motionX += event.x;
            entity.motionY += event.y;
            entity.motionZ += event.z;
            entity.isAirBorne = event.airbone;
        }
    }

    @Inject(method = "move", at = @At(value = "FIELD", target = "net/minecraft/entity/Entity.onGround:Z", ordinal = 1))
    private void onGroundHook(MoverType type, double x, double y, double z, CallbackInfo info) {
        if (EntityPlayerSP.class.isInstance(this)) {
            StepEvent event = new StepEvent(0, this.getEntityBoundingBox(), this.stepHeight);
            MinecraftForge.EVENT_BUS.post(event);
            prevHeight = stepHeight;
            stepHeight = event.getHeight();
        }
    }

    @Inject(method = "move",at = @At(value = "INVOKE", target = "net/minecraft/entity/Entity.setEntityBoundingBox" + "(Lnet/minecraft/util/math/AxisAlignedBB;)V", ordinal = 7, shift = At.Shift.AFTER))
    private void setEntityBoundingBoxHook(MoverType type, double x, double y, double z, CallbackInfo info) {
        if (EntityPlayerSP.class.isInstance(this)) {
            StepEvent event = new StepEvent(1, this.getEntityBoundingBox(), this.prevHeight != null ? this.prevHeight : 0.0F);
            MinecraftForge.EVENT_BUS.post(event);
        }
    }
}