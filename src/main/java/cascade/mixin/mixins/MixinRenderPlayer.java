package cascade.mixin.mixins;

import cascade.features.modules.visual.Nametags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ RenderPlayer.class })
public class MixinRenderPlayer {
    Minecraft mc;

    public MixinRenderPlayer() {
        this.mc = Minecraft.getMinecraft();
    }

    @Inject(method = { "renderEntityName" }, at = { @At("HEAD") }, cancellable = true)
    public void renderEntityNameHook(final AbstractClientPlayer entityIn, final double x, final double y, final double z, final String name, final double distanceSq, final CallbackInfo info) {
        if (Nametags.getInstance().isEnabled()) {
            info.cancel();
        }
    }
    /*
        @Overwrite
    public ResourceLocation getEntityTexture(final AbstractClientPlayer entity) {
        if (OyVey.moduleManager.isModuleEnabled("TexturedChams")) {
            GL11.glColor4f(TexturedChams.red.getValue() / 255.0f, TexturedChams.green.getValue() / 255.0f, TexturedChams.blue.getValue() / 255.0f, TexturedChams.alpha.getValue() / 255.0f);
            return new ResourceLocation("minecraft:steve_skin1.png");
        }
        return entity.getLocationSkin();
    }

     */
}