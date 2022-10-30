package cascade.event.events;

import cascade.event.EventStage;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class RenderItemInFirstPersonEvent extends EventStage {

    private final EntityLivingBase entity;
    private ItemStack stack;
    private ItemCameraTransforms.TransformType transformType;
    private final boolean leftHanded;

    public RenderItemInFirstPersonEvent(EntityLivingBase entitylivingbaseIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform, boolean leftHanded, int stage) {
        super(stage);
        this.entity = entitylivingbaseIn;
        this.stack = heldStack;
        this.transformType = transform;
        this.leftHanded = leftHanded;
    }

    public ItemCameraTransforms.TransformType getTransformType() {
        return transformType;
    }

    public void setTransformType(ItemCameraTransforms.TransformType transformType) {
        this.transformType = transformType;
    }

    public boolean isLeftHanded() {
        return leftHanded;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }
}