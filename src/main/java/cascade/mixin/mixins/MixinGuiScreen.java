package cascade.mixin.mixins;

import cascade.features.modules.visual.Visual;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ GuiScreen.class })
public class MixinGuiScreen extends Gui {
    @Inject(method = { "renderToolTip" }, at = { @At("HEAD") }, cancellable = true)
    public void renderToolTipHook(final ItemStack stack, final int x, final int y, final CallbackInfo info) {
        if (Visual.getInstance().isEnabled() && Visual.getInstance().shulkerPreview.getValue() && stack.getItem() instanceof ItemShulkerBox) {
            Visual.getInstance().renderShulkerToolTip(stack, x, y, null);
            info.cancel();
        }
    }
}