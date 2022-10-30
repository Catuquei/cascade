package cascade.mixin.mixins;

import cascade.features.modules.visual.Crosshair;
import cascade.features.modules.visual.NoRender;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ GuiIngame.class })
public class MixinGuiIngame extends Gui {

    @Inject(method = {"renderPortal"}, at = {@At("HEAD")}, cancellable = true)
    protected void renderPortalHook(final float n, final ScaledResolution scaledResolution, final CallbackInfo info) {
        if (NoRender.getInstance().isEnabled() && NoRender.getInstance().noOverlay.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = {"renderPumpkinOverlay"}, at = {@At("HEAD")}, cancellable = true)
    protected void renderPumpkinOverlayHook(final ScaledResolution scaledRes, final CallbackInfo info) {
        if (NoRender.getInstance().isEnabled() && NoRender.getInstance().noOverlay.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "renderAttackIndicator", at = @At("HEAD"), cancellable = true)
    public void onRenderAttackIndicator(float partialTicks, ScaledResolution p_184045_2_, CallbackInfo ci) {
        Crosshair crosshair = new Crosshair();
        if (crosshair.isEnabled())
            ci.cancel();
    }
}