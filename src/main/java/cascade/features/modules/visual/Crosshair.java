package cascade.features.modules.visual;

import cascade.Cascade;
import cascade.event.events.Render2DEvent;
import cascade.event.events.UpdateWalkingPlayerEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class Crosshair extends Module {

    private final Setting<Boolean> dot = register(new Setting<>("Dot", false)); //damn dot5??? dotgod elitetier.pl??
    private final Setting<Float> crosshairGap = register(new Setting<>("Gap", 2.0F, 0.0F, 10.0F));
    private final Setting<Float> motionGap = register(new Setting<>("MotionGap", 0.0F, 0.0F, 5.0F));
    private final Setting<Float> crosshairWidth = register(new Setting<>("Width", 1.0F, 0.1F, 5.0F));
    private final Setting<Float> motionWidth = register(new Setting<>("MotionWidth", 0.0F, 0.0F, 2.5F));
    private final Setting<Float> crosshairSize = register(new Setting<>("Size", 2.0F, 0.1F, 40.0F));
    private final Setting<Float> motionSize = register(new Setting<>("MotionSize", 0.0F, 0.0F, 20.0F));
    private final Setting<Color> c = register(new Setting<>("Color", new Color(0x7F08FF)));
    float currentMotion = 0F;
    long lastUpdate = -1L;
    float prevMotion = 0F;

    public Crosshair() {
        super("Crosshair", Category.VISUAL, "Draws a custom crosshair");
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent e) {
        prevMotion = currentMotion;
        double dX = mc.player.posX - mc.player.prevPosX;
        double dZ = mc.player.posZ - mc.player.prevPosZ;
        currentMotion = (float) Math.sqrt(dX * dX + dZ * dZ);
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void onRender2D(Render2DEvent e) {
        ScaledResolution sr = new ScaledResolution(mc);
        float cX = (float) (sr.getScaledWidth_double() / 2F + 0.5F);
        float cY = (float) (sr.getScaledHeight_double() / 2F + 0.5F);
        float gap = crosshairGap.getValue();
        float width = Math.max(crosshairWidth.getValue(), 0.5F);
        float size = crosshairSize.getValue();
        float tickLength = mc.timer.tickLength;
        gap += lerp(prevMotion, currentMotion, Math.min((System.currentTimeMillis() - lastUpdate) / tickLength, 1F)) * motionGap.getValue();
        width += lerp(prevMotion, currentMotion, Math.min((System.currentTimeMillis() - lastUpdate) / tickLength, 1F)) * motionWidth.getValue();
        size += lerp(prevMotion, currentMotion, Math.min((System.currentTimeMillis() - lastUpdate) / tickLength, 1F)) * motionSize.getValue();
        drawRect(cX - gap - size, cY - width / 2.0F, cX - gap, cY + width / 2.0F, c.getValue().getRGB());
        drawRect(cX + gap + size, cY - width / 2.0F, cX + gap, cY + width / 2.0F, c.getValue().getRGB());
        drawRect(cX - width / 2.0F, cY + gap + size, cX + width / 2.0F, cY + gap, c.getValue().getRGB());
        drawRect(cX - width / 2.0F, cY - gap - size, cX + width / 2.0F, cY - gap, c.getValue().getRGB());
        if (dot.getValue()) {
            drawRect(cX - width / 2F, cY - width / 2F, cX + width / 2F, cY + width / 2F, c.getValue().getRGB());
        }
    }

    public static void drawRect(float left, float top, float right, float bottom, int color) {
        if (left < right) {
            float i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            float j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static float lerp(float a, float b, float partial) {
        return (a * (1f - partial)) + (b * partial);
    }
}
