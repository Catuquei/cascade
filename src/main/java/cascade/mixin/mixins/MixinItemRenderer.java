package cascade.mixin.mixins;

import cascade.event.events.RenderItemInFirstPersonEvent;
import cascade.features.modules.visual.HandChams;
import cascade.features.modules.visual.ViewMod;
import cascade.util.render.ColorUtil;
import cascade.features.modules.core.ClickGui;
import cascade.features.modules.visual.NoRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ ItemRenderer.class })
public abstract class MixinItemRenderer {

    @Inject(method = { "renderFireInFirstPerson" }, at = { @At("HEAD") }, cancellable = true)
    public void renderFireInFirstPersonHook(final CallbackInfo info) {
        if (NoRender.getInstance().isEnabled() && NoRender.getInstance().noOverlay.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = { "renderSuffocationOverlay" }, at = { @At("HEAD") }, cancellable = true)
    public void renderSuffocationOverlay(final CallbackInfo ci) {
        if (NoRender.getInstance().isEnabled() && NoRender.getInstance().noOverlay.getValue()) {
            ci.cancel();
        }
    }

    @Shadow
    @Final
    public Minecraft mc;
    private boolean injection = true;

    @Inject(method = {"renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void renderItemInFirstPersonHook(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, EnumHand hand, float p_187457_5_, ItemStack stack, float p_187457_7_, CallbackInfo info) {
        if (this.injection) {
            info.cancel();
            float xOffset = 0.0f;
            float yOffset = 0.0f;
            this.injection = false;
            if (HandChams.getINSTANCE().isEnabled() && hand == EnumHand.MAIN_HAND && stack.isEmpty()) {
                if (HandChams.getINSTANCE().mode.getValue().equals(HandChams.RenderMode.Wireframe)) {
                    this.renderItemInFirstPerson(player, p_187457_2_, p_187457_3_, hand, p_187457_5_ + xOffset, stack, p_187457_7_ + yOffset);
                }
                GlStateManager.pushMatrix();
                if (HandChams.getINSTANCE().mode.getValue().equals(HandChams.RenderMode.Wireframe)) {
                    GL11.glPushAttrib(1048575);
                } else {
                    GlStateManager.pushAttrib();
                }
                if (HandChams.getINSTANCE().mode.getValue().equals(HandChams.RenderMode.Wireframe)) {
                    GL11.glPolygonMode(1032, 6913);
                }
                GL11.glDisable(3553);
                GL11.glDisable(2896);
                if (HandChams.getINSTANCE().mode.getValue().equals(HandChams.RenderMode.Wireframe)) {
                    GL11.glEnable(2848);
                    GL11.glEnable(3042);
                }
                GL11.glColor4f(ClickGui.getInstance().rainbow.getValue() != false ? (float) ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).getRed() / 255.0f : (float) HandChams.getINSTANCE().c.getValue().getRed() / 255.0f, ClickGui.getInstance().rainbow.getValue() != false ? (float) ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).getGreen() / 255.0f : (float) HandChams.getINSTANCE().c.getValue().getGreen() / 255.0f, ClickGui.getInstance().rainbow.getValue() != false ? (float) ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).getBlue() / 255.0f : (float) HandChams.getINSTANCE().c.getValue().getBlue() / 255.0f, (float) HandChams.getINSTANCE().c.getValue().getAlpha() / 255.0f);
                this.renderItemInFirstPerson(player, p_187457_2_, p_187457_3_, hand, p_187457_5_ + xOffset, stack, p_187457_7_ + yOffset);
                GlStateManager.popAttrib();
                GlStateManager.popMatrix();
            }
            if ((!stack.isEmpty || HandChams.getINSTANCE().isDisabled())) {
                this.renderItemInFirstPerson(player, p_187457_2_, p_187457_3_, hand, p_187457_5_ + xOffset, stack, p_187457_7_ + yOffset);
            } else if (!stack.isEmpty || HandChams.getINSTANCE().isDisabled()) {
                this.renderItemInFirstPerson(player, p_187457_2_, p_187457_3_, hand, p_187457_5_, stack, p_187457_7_);
            }
            this.injection = true;
        }
    }

    @Redirect(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemSide(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V"))
    public void captainHook(ItemRenderer itemRenderer, EntityLivingBase entitylivingbaseIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform, boolean leftHanded) {
        RenderItemInFirstPersonEvent pre = new RenderItemInFirstPersonEvent(entitylivingbaseIn, heldStack, transform, leftHanded, 0);
        MinecraftForge.EVENT_BUS.post(pre);
        if (!pre.isCanceled()) {
            itemRenderer.renderItemSide(entitylivingbaseIn, pre.getStack(), pre.getTransformType(), leftHanded);
        }
        RenderItemInFirstPersonEvent post = new RenderItemInFirstPersonEvent(entitylivingbaseIn, heldStack, transform, leftHanded, 1);
        MinecraftForge.EVENT_BUS.post(post);
    }

    @Shadow
    public abstract void renderItemInFirstPerson(AbstractClientPlayer var1, float var2, float var3, EnumHand var4, float var5, ItemStack var6, float var7);

    @Inject( method = "renderItemSide", at = @At( value = "HEAD" ) )
    public void renderItemSide(EntityLivingBase entityLivingBase, ItemStack stack, ItemCameraTransforms.TransformType transform, boolean leftHanded, CallbackInfo info) {
        if (ViewMod.getInstance().isEnabled() && entityLivingBase == mc.player) {
            GlStateManager.scale(ViewMod.getInstance().sizeX.getValue(), ViewMod.getInstance().sizeY.getValue(), ViewMod.getInstance().sizeZ.getValue());
            if (mc.player.getActiveItemStack() != stack) {
                GlStateManager.translate((ViewMod.getInstance().x.getValue() * 0.1f) * (leftHanded ? -1 : 1), ViewMod.getInstance().y.getValue() * 0.1f, ViewMod.getInstance().z.getValue() * 0.1);
            }
        }
    }
}