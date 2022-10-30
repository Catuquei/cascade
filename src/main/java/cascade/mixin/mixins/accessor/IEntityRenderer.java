package cascade.mixin.mixins.accessor;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface IEntityRenderer {

    @Invoker("setupCameraTransform")
    void setupCameraTransformInvoker(float partialTicks, int pass);

    @Accessor(value="rendererUpdateCount")
    int getRendererUpdateCount();

    @Accessor(value="rainXCoords")
    float[] getRainXCoords();

    @Accessor(value="rainYCoords")
    float[] getRainYCoords();

    @Accessor(value="farPlaneDistance")
    float getFarPlaneDistance();

    @Accessor(value="fovModifierHandPrev")
    float getFovModifierHandPrev();

    @Accessor(value="fovModifierHand")
   float getFovModifierHand();

    @Accessor(value="debugView")
    boolean isDebugView();
}