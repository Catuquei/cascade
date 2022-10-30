package cascade.event.events;

import cascade.event.EventStage;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class RenderCrystalEvent extends EventStage {

    public static class RenderCrystalPreEvent extends RenderCrystalEvent {

        private final ModelBase modelBase;
        private final Entity entity;
        private final float limbSwing;
        private final float limbSwingAmount;
        private final float ageInTicks;
        private final float netHeadYaw;
        private final float headPitch;
        private float scaleFactor;

        public RenderCrystalPreEvent(ModelBase modelBase, Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            this.modelBase = modelBase;
            this.entity = entity;
            this.limbSwing = limbSwing;
            this.limbSwingAmount = limbSwingAmount;
            this.ageInTicks = ageInTicks;
            this.netHeadYaw = netHeadYaw;
            this.headPitch = headPitch;
            this.scaleFactor = scaleFactor;
        }

        public ModelBase getModelBase() {
            return this.modelBase;
        }

        public Entity getEntity() {
            return this.entity;
        }

        public float getLimbSwing() {
            return this.limbSwing;
        }

        public float getLimbSwingAmount() {
            return this.limbSwingAmount;
        }

        public float getAgeInTicks() {
            return this.ageInTicks;
        }

        public float getNetHeadYaw() {
            return this.netHeadYaw;
        }

        public float getHeadPitch() {
            return this.headPitch;
        }

        public float getScaleFactor() {
            return this.scaleFactor;
        }
    }

    public static class RenderCrystalPostEvent extends RenderCrystalEvent {

        private final ModelBase modelBase;
        private final ModelBase modelNoBase;
        private final EntityEnderCrystal entityEnderCrystal;
        private final double x;
        private final double y;
        private final double z;
        private final float entityYaw;
        private final float partialTicks;

        public RenderCrystalPostEvent(ModelBase modelBase, ModelBase modelNoBase, EntityEnderCrystal entityEnderCrystal, double x, double y, double z, float entityYaw, float partialTicks) {
            this.modelBase = modelBase;
            this.modelNoBase = modelNoBase;
            this.entityEnderCrystal = entityEnderCrystal;
            this.x = x;
            this.y = y;
            this.z = z;
            this.entityYaw = entityYaw;
            this.partialTicks = partialTicks;
        }

        public ModelBase getModelBase() {
            return this.modelBase;
        }

        public ModelBase getModelNoBase() {
            return this.modelNoBase;
        }

        public EntityEnderCrystal getEntityEnderCrystal() {
            return this.entityEnderCrystal;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getZ() {
            return this.z;
        }

        public float getEntityYaw() {
            return this.entityYaw;
        }

        public float getPartialTicks() {
            return this.partialTicks;
        }
    }
}