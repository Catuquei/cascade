package cascade.features.modules.visual;

import cascade.event.events.Render2DEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.misc.CalcUtil;
import cascade.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ESP extends Module {

    public ESP() {
        super("ESP", Category.VISUAL, "Highlights entities through walls");
    }

    Setting<Boolean> xpBottles = register(new Setting("XpBottles", true));
    Setting<Boolean> xpOrbs = register(new Setting("XpOrbs", false));
    Setting<Boolean> items = register(new Setting("Items", false));
    Setting<Boolean> pearls = register(new Setting("Pearls", true));
    Setting<Float> lineWidth = register(new Setting("LineWidth", 1.0f, 0.1f, 5.0f));
    Setting<Color> c = register(new Setting("Color", new Color(-1)));

    @Override
    public void onRender2D(Render2DEvent e) {
        if (fullNullCheck()) {
            return;
        }
        if (items.getValue()) {
            int i = 0;
            for (Entity entity : ESP.mc.world.loadedEntityList) {
                if (entity instanceof EntityItem && CalcUtil.getDistance(entity) < 300.0) {
                    Vec3d interp = getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
                    AxisAlignedBB bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0 - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05 - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1 - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05 - entity.posZ + interp.z);
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.disableDepth();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    GL11.glEnable(2848);
                    GL11.glHint(3154, 4354);
                    GL11.glLineWidth(1.0f);
                    RenderGlobal.renderFilledBox(bb, c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha());
                    GL11.glDisable(2848);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                    RenderUtil.drawBlockOutline(bb, new Color(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha()), lineWidth.getValue());
                    if (++i >= 50) {
                        break;
                    }
                    continue;
                }
            }
        }
        if (xpOrbs.getValue()) {
            int i = 0;
            for (Entity entity : ESP.mc.world.loadedEntityList) {
                if (entity instanceof EntityXPOrb && CalcUtil.getDistance(entity) < 300.0) {
                    Vec3d interp = getInterpolatedRenderPos(entity, ESP.mc.getRenderPartialTicks());
                    final AxisAlignedBB bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0 - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05 - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1 - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05 - entity.posZ + interp.z);
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.disableDepth();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    GL11.glEnable(2848);
                    GL11.glHint(3154, 4354);
                    GL11.glLineWidth(1.0f);
                    RenderGlobal.renderFilledBox(bb, c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha());
                    GL11.glDisable(2848);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                    RenderUtil.drawBlockOutline(bb, new Color(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha()), lineWidth.getValue());
                    if (++i >= 50) {
                        break;
                    }
                    continue;
                }
            }
        }
        if (pearls.getValue()) {
            int i = 0;
            for (final Entity entity : ESP.mc.world.loadedEntityList) {
                if (entity instanceof EntityEnderPearl && CalcUtil.getDistance(entity) < 300.0) {
                    final Vec3d interp = getInterpolatedRenderPos(entity, ESP.mc.getRenderPartialTicks());
                    final AxisAlignedBB bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0 - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05 - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1 - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05 - entity.posZ + interp.z);
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.disableDepth();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    GL11.glEnable(2848);
                    GL11.glHint(3154, 4354);
                    GL11.glLineWidth(1.0f);
                    RenderGlobal.renderFilledBox(bb, c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha());
                    GL11.glDisable(2848);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                    RenderUtil.drawBlockOutline(bb, new Color(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha()), lineWidth.getValue());
                    if (++i >= 50) {
                        break;
                    }
                    continue;
                }
            }
        }
        if (xpBottles.getValue()) {
            int i = 0;
            for (final Entity entity : ESP.mc.world.loadedEntityList) {
                if (entity instanceof EntityExpBottle && CalcUtil.getDistance(entity) < 300.0) {
                    final Vec3d interp = getInterpolatedRenderPos(entity, ESP.mc.getRenderPartialTicks());
                    final AxisAlignedBB bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0 - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05 - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1 - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05 - entity.posZ + interp.z);
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.disableDepth();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    GL11.glEnable(2848);
                    GL11.glHint(3154, 4354);
                    GL11.glLineWidth(1.0f);
                    RenderGlobal.renderFilledBox(bb, c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha());
                    GL11.glDisable(2848);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                    RenderUtil.drawBlockOutline(bb, new Color(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha()), lineWidth.getValue());
                    if (++i >= 50) {
                        break;
                    }
                    continue;
                }
            }
        }
    }

    Vec3d getInterpolatedAmount(Entity e, double x, double y, double z) {
        return new Vec3d((e.posX - e.lastTickPosX) * x, (e.posY - e.lastTickPosY) * y, (e.posZ - e.lastTickPosZ) * z);
    }

    Vec3d getInterpolatedAmount(Entity e, float pTicks) {
        return getInterpolatedAmount(e, pTicks, pTicks, pTicks);
    }

    Vec3d getInterpolatedPos(Entity e, float pTicks) {
        return new Vec3d(e.lastTickPosX, e.lastTickPosY, e.lastTickPosZ).add(getInterpolatedAmount(e, pTicks));
    }

    Vec3d getInterpolatedRenderPos(Entity e, float partialTicks) {
        return getInterpolatedPos(e, partialTicks).subtract(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ);
    }
}