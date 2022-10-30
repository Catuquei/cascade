package cascade.mixin.mixins;

import cascade.Cascade;
import cascade.event.events.ModelRenderEvent;
import cascade.features.modules.visual.Chams;
import cascade.util.Util;
import cascade.util.render.ColorUtil;
import cascade.features.modules.core.ClickGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;

@Mixin({ RenderLivingBase.class })
public abstract class MixinRenderLivingBase<T extends EntityLivingBase> extends Render<T> {
    private static final ResourceLocation glint;

    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");


    public MixinRenderLivingBase(final RenderManager renderManagerIn, final ModelBase modelBaseIn, final float shadowSizeIn) {
        super(renderManagerIn);
    }
    @Shadow
    protected ModelBase mainModel;
    @Shadow
    protected boolean renderMarker;
    float red;
    float green;
    float blue;

    protected MixinRenderLivingBase(final RenderManager renderManager) {
        super(renderManager);
        this.red = 0.0f;
        this.green = 0.0f;
        this.blue = 0.0f;
    }

    @Redirect(method = { "renderModel" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V"))
    private void renderModelHook(final ModelBase modelBase, final Entity entityIn, final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale) {
        boolean cancel = false;
        if (Chams.getINSTANCE().texture.getValue()) {
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            Color visibleColor2 = ColorUtil.getColor(entityIn, Chams.getINSTANCE().textureColor.getValue().getRed(), Chams.getINSTANCE().textureColor.getValue().getGreen(), Chams.getINSTANCE().textureColor.getValue().getBlue(), Chams.getINSTANCE().textureColor.getValue().getAlpha(), true);
            GL11.glColor4f(visibleColor2.getRed() / 255.0f, visibleColor2.getGreen() / 255.0f, visibleColor2.getBlue() / 255.0f, Chams.getINSTANCE().textureColor.getValue().getAlpha() / 255.0f);
            modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
        } else if (!cancel) {
            modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    @Overwrite
    public void doRender(final T entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks) {
        if (!MinecraftForge.EVENT_BUS.post((net.minecraftforge.fml.common.eventhandler.Event)new RenderLivingEvent.Pre((EntityLivingBase)entity, (RenderLivingBase)RenderLivingBase.class.cast(this), partialTicks, x, y, z))) {
            GlStateManager.pushMatrix();
            GlStateManager.disableCull();
            this.mainModel.swingProgress = this.getSwingProgress(entity, partialTicks);
            final boolean shouldSit = entity.isRiding() && entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit();
            this.mainModel.isRiding = shouldSit;
            this.mainModel.isChild = entity.isChild();
            try {
                float f = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
                final float f2 = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
                float f3 = f2 - f;
                if (shouldSit && entity.getRidingEntity() instanceof EntityLivingBase) {
                    final EntityLivingBase entitylivingbase = (EntityLivingBase)entity.getRidingEntity();
                    f = this.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                    f3 = f2 - f;
                    float f4 = MathHelper.wrapDegrees(f3);
                    if (f4 < -85.0f) {
                        f4 = -85.0f;
                    }
                    if (f4 >= 85.0f) {
                        f4 = 85.0f;
                    }
                    f = f2 - f4;
                    if (f4 * f4 > 2500.0f) {
                        f += f4 * 0.2f;
                    }
                    f3 = f2 - f;
                }
                final float f5 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                this.renderLivingAt(entity, x, y, z);
                final float f6 = this.handleRotationFloat(entity, partialTicks);
                this.applyRotations(entity, f6, f, partialTicks);
                final float f7 = this.prepareScale(entity, partialTicks);
                float f8 = 0.0f;
                float f9 = 0.0f;
                if (!entity.isRiding()) {
                    f8 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
                    f9 = entity.limbSwing - entity.limbSwingAmount * (1.0f - partialTicks);
                    if (entity.isChild()) {
                        f9 *= 3.0f;
                    }
                    if (f8 > 1.0f) {
                        f8 = 1.0f;
                    }
                    f3 = f2 - f;
                }
                GlStateManager.enableAlpha();
                this.mainModel.setLivingAnimations(entity, f9, f8, partialTicks);
                this.mainModel.setRotationAngles(f9, f8, f6, f3, f5, f7, entity);
                if (this.renderOutlines) {
                    final boolean flag1 = this.setScoreTeamColor(entity);
                    GlStateManager.enableColorMaterial();
                    GlStateManager.enableOutlineMode(this.getTeamColor(entity));
                    if (!this.renderMarker) {
                        this.renderModel(entity, f9, f8, f6, f3, f5, f7);
                    }
                    if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator()) {
                        this.renderLayers(entity, f9, f8, partialTicks, f6, f3, f5, f7);
                    }
                    GlStateManager.disableOutlineMode();
                    GlStateManager.disableColorMaterial();
                    if (flag1) {
                        this.unsetScoreTeamColor();
                    }
                } else {
                    if (Chams.getINSTANCE().isEnabled() && entity instanceof EntityPlayer && Chams.getINSTANCE().solid.getValue()) {
                        this.red = Chams.getINSTANCE().solidC.getValue().getRed() / 255.0f;
                        this.green = Chams.getINSTANCE().solidC.getValue().getGreen() / 255.0f;
                        this.blue = Chams.getINSTANCE().solidC.getValue().getBlue() / 255.0f;
                        GlStateManager.pushMatrix();
                        if (Chams.getINSTANCE().glint.getValue()) {
                            glPushAttrib(GL_ALL_ATTRIB_BITS);
                            glEnable(GL_BLEND);
                            glDepthMask(false);
                            glEnable(GL_TEXTURE_2D);
                            glDisable(GL_DEPTH_TEST);
                            Util.mc.getTextureManager().bindTexture(RES_ITEM_GLINT);
                            GL11.glTexCoord3d(1.0, 1.0, 1.0);
                            GL11.glEnable(3553);
                            GL11.glBlendFunc(768, 771);
                            GL11.glBlendFunc(770, 32772);
                        }
                        GlStateManager.disableLighting();
                        GL11.glPushAttrib(1048575);
                        GL11.glDisable(3553);
                        GL11.glDisable(2896);
                        GL11.glEnable(2848);
                        GL11.glEnable(3042);
                        GL11.glBlendFunc(770, 771);
                        GL11.glDisable(2929);
                        GL11.glDepthMask(false);
                        if (Cascade.friendManager.isFriend(entity.getName()) || entity == Minecraft.getMinecraft().player) {
                            GL11.glColor4f(0.0f, 191.0f, 255.0f, Chams.getINSTANCE().solidC.getValue().getAlpha() / 255.0f);
                        } else {
                            GL11.glColor4f((ClickGui.getInstance().rainbow.getValue()) ? (ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).getRed() / 255.0f) : red, ((boolean)ClickGui.getInstance().rainbow.getValue()) ? (ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).getGreen() / 255.0f) : this.green, ((boolean)ClickGui.getInstance().rainbow.getValue()) ? (ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).getBlue() / 255.0f) : this.blue, Chams.getINSTANCE().solidC.getValue().getAlpha() / 255.0f);
                        }
                        this.renderModel(entity, f9, f8, f6, f3, f5, f7);
                        GL11.glDisable(2896);
                        GL11.glEnable(2929);
                        GL11.glDepthMask(true);
                        if (Cascade.friendManager.isFriend(entity.getName()) || entity == Minecraft.getMinecraft().player) {
                            GL11.glColor4f(0.0f, 191.0f, 255.0f, Chams.getINSTANCE().solidC.getValue().getAlpha() / 255.0f);
                        } else {
                            GL11.glColor4f((ClickGui.getInstance().rainbow.getValue()) ? (ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).getRed() / 255.0f) : this.red, ((boolean)ClickGui.getInstance().rainbow.getValue()) ? (ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).getGreen() / 255.0f) : this.green, ((boolean)ClickGui.getInstance().rainbow.getValue()) ? (ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).getBlue() / 255.0f) : this.blue, Chams.getINSTANCE().solidC.getValue().getAlpha() / 255.0f);
                        }
                        this.renderModel(entity, f9, f8, f6, f3, f5, f7);
                        GL11.glEnable(2896);
                        GlStateManager.popAttrib();
                        GlStateManager.enableLighting();
                        GlStateManager.popMatrix();
                    }
                    boolean flag1 = this.setDoRenderBrightness(entity, partialTicks);
                    if (!(entity instanceof EntityPlayer) || (Chams.getINSTANCE().isEnabled() && Chams.getINSTANCE().wireframe.getValue()) || Chams.getINSTANCE().isDisabled()) {
                        this.renderModel(entity, f9, f8, f6, f3, f5, f7);
                    }
                    if (flag1) {
                        this.unsetBrightness();
                    }
                    GlStateManager.depthMask(true);
                    if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator()) {
                        this.renderLayers(entity, f9, f8, partialTicks, f6, f3, f5, f7);
                    }
                    if (Chams.getINSTANCE().isEnabled() && entity instanceof EntityPlayer && Chams.getINSTANCE().wireframe.getValue()) {
                        this.red = Chams.getINSTANCE().wireC.getValue().getRed() / 255.0f;
                        this.green = Chams.getINSTANCE().wireC.getValue().getGreen() / 255.0f;
                        this.blue = Chams.getINSTANCE().wireC.getValue().getBlue() / 255.0f;
                        GlStateManager.pushMatrix();
                        if (Chams.getINSTANCE().glint.getValue()) {
                            glPushAttrib(GL_ALL_ATTRIB_BITS);
                            glEnable(GL_BLEND);
                            glDepthMask(false);
                            glEnable(GL_TEXTURE_2D);
                            glDisable(GL_DEPTH_TEST);
                            Util.mc.getTextureManager().bindTexture(RES_ITEM_GLINT);
                            GL11.glTexCoord3d(1.0, 1.0, 1.0);
                            GL11.glEnable(3553);
                            GL11.glBlendFunc(768, 771);
                            GL11.glBlendFunc(770, 32772);
                        }
                        GL11.glPushAttrib(1048575);
                        GL11.glPolygonMode(1032, 6913);
                        GL11.glDisable(3553);
                        GL11.glDisable(2896);
                        GL11.glDisable(2929);
                        GL11.glEnable(2848);
                        GL11.glEnable(3042);
                        GL11.glBlendFunc(770, 771);
                        if (Cascade.friendManager.isFriend(entity.getName()) || entity == Minecraft.getMinecraft().player) {
                            GL11.glColor4f(0.0f, 191.0f, 255.0f, Chams.getINSTANCE().wireC.getValue().getAlpha() / 255.0f);
                        }
                        else {
                            GL11.glColor4f((ClickGui.getInstance().rainbow.getValue()) ? (ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).getRed() / 255.0f) : this.red, (ClickGui.getInstance().rainbow.getValue()) ? (ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).getGreen() / 255.0f) : this.green, (ClickGui.getInstance().rainbow.getValue()) ? (ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).getBlue() / 255.0f) : this.blue, Chams.getINSTANCE().wireC.getValue().getAlpha() / 255.0f);
                        }
                        GL11.glLineWidth(Chams.getINSTANCE().lineWidth.getValue());
                        this.renderModel(entity, f9, f8, f6, f3, f5, f7);
                        GL11.glEnable(2896);
                        GlStateManager.popAttrib();
                        GlStateManager.popMatrix();
                    }
                }
                GlStateManager.disableRescaleNormal();
            }
            catch (Exception var20) {}
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.enableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableCull();
            GlStateManager.popMatrix();
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
            MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post(entity, RenderLivingBase.class.cast(this), partialTicks, x, y, z));
        }
    }

    @Redirect(method = "renderModel", at = @At(value = "INVOKE", target = "net/minecraft/client/model/ModelBase." + "render(Lnet/minecraft/entity/Entity;FFFFFF)V"))
    private void renderHook(ModelBase model, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        RenderLivingBase<?> renderLiving = RenderLivingBase.class.cast(this);
        EntityLivingBase entity = (EntityLivingBase) entityIn;

        ModelRenderEvent event = new ModelRenderEvent.Pre(renderLiving, entity, model, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        MinecraftForge.EVENT_BUS.post(event);

        if (!event.isCanceled()) {
            model.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }

        MinecraftForge.EVENT_BUS.post(new ModelRenderEvent.Post(renderLiving, entity, model, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale));
    }

    @Shadow
    protected abstract boolean isVisible(final EntityLivingBase p0);

    @Shadow
    protected abstract float getSwingProgress(final T p0, final float p1);

    @Shadow
    protected abstract float interpolateRotation(final float p0, final float p1, final float p2);

    @Shadow
    protected abstract float handleRotationFloat(final T p0, final float p1);

    @Shadow
    protected abstract void applyRotations(final T p0, final float p1, final float p2, final float p3);

    @Shadow
    public abstract float prepareScale(final T p0, final float p1);

    @Shadow
    protected abstract void unsetScoreTeamColor();

    @Shadow
    protected abstract boolean setScoreTeamColor(final T p0);

    @Shadow
    protected abstract void renderLivingAt(final T p0, final double p1, final double p2, final double p3);

    @Shadow
    protected abstract void unsetBrightness();

    @Shadow
    protected abstract void renderModel(final T p0, final float p1, final float p2, final float p3, final float p4, final float p5, final float p6);

    @Shadow
    protected abstract void renderLayers(final T p0, final float p1, final float p2, final float p3, final float p4, final float p5, final float p6, final float p7);

    @Shadow
    protected abstract boolean setDoRenderBrightness(final T p0, final float p1);

    static {
        glint = new ResourceLocation("textures/shinechams.png");
    }
}
