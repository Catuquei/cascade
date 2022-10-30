package cascade.mixin.mixins;


import cascade.Cascade;
import cascade.features.modules.core.FontMod;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ FontRenderer.class })
public abstract class MixinFontRenderer {
    @Shadow
    protected abstract int renderString(final String p0, final float p1, final float p2, final int p3, final boolean p4);

    @Shadow
    protected abstract void renderStringAtPos(final String p0, final boolean p1);

    @Inject(method = {"drawString(Ljava/lang/String;FFIZ)I"}, at = {@At("HEAD")}, cancellable = true)
    public void renderStringHook(final String text, final float x, final float y, final int color, final boolean dropShadow, final CallbackInfoReturnable<Integer> info) {
        if (FontMod.getInstance().isEnabled() && FontMod.getInstance().customAll.getValue() && Cascade.textManager != null) {
            float result = Cascade.textManager.drawFontString(text, x, y, color, dropShadow);
            info.setReturnValue((int) result);
        }
    }
}