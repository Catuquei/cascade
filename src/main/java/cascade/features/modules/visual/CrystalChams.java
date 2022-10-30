package cascade.features.modules.visual;

import cascade.event.events.CrystalTextureEvent;
import cascade.event.events.RenderCrystalEvent;
import cascade.features.modules.Module;
import cascade.features.modules.combat.CascadeAura;
import cascade.features.setting.Setting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;


public class CrystalChams extends Module {
    public Setting<Integer> rotations = register(new Setting("Rotations", 30, 0, 200));
    public Setting<Boolean> glint = register(new Setting("Glint", false));
    public Setting<Double> scaleX = register(new Setting("ScaleX", 1.0, 0.0, 2.0));
    public Setting<Double> scaleY = register(new Setting("ScaleY", 1.0, 0.0, 2.0));
    public Setting<Double> scaleZ = register(new Setting("ScaleZ", 1.0, 0.0, 2.0));
    public Setting<Float> lineWidth = register(new Setting("Line Width", 1.0f, 0.1f, 3.0f));
    public Setting<Color> c = register(new Setting("Color", new Color(-1)));
    static final ResourceLocation RES_ITEM_GLINT;


    static {
        RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    }

    public CrystalChams() {
        super("CrystalChams", Category.VISUAL, "");
    }

    @SubscribeEvent
    public void renderCrystalTexture(CrystalTextureEvent event) {
        if (isEnabled()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderCrystalPre(RenderCrystalEvent.RenderCrystalPreEvent event) {
        if (isEnabled()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderCrystalPost(RenderCrystalEvent.RenderCrystalPostEvent event) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        glPushMatrix();
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        float rotation = event.getEntityEnderCrystal().innerRotation + event.getPartialTicks();
        float rotationMoved = MathHelper.sin(rotation * 0.2F) / 2 + 0.5F;
        rotationMoved += Math.pow(rotationMoved, 2);
        glTranslated(event.getX(), event.getY(), event.getZ());
        glScaled(scaleX.getValue(), scaleY.getValue(), scaleZ.getValue());
        glEnable(GL_BLEND);
        glDepthMask(false);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        if (glint.getValue()) {
            mc.getTextureManager().bindTexture(RES_ITEM_GLINT);
            GL11.glTexCoord3d(1.0, 1.0, 1.0);
            GL11.glEnable(3553);
            GL11.glBlendFunc(768, 771);
            GL11.glBlendFunc(770, 32772);
        }
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glLineWidth(lineWidth.getValue());
        float r = c.getValue().getRed() / 255f;
        float g = c.getValue().getGreen() / 255f;
        float b = c.getValue().getBlue() / 255f;
        float a = c.getValue().getAlpha() / 255f;
        if (CascadeAura.getInstance().crystalChams.getValue() && (CascadeAura.getInstance().placeSet.contains(event.getEntityEnderCrystal().getPosition()) || CascadeAura.getInstance().breakSet.contains(event.getEntityEnderCrystal().getEntityId()))) {
            float rC = CascadeAura.getInstance().chamsColor.getValue().getRed() / 255f;
            float gC = CascadeAura.getInstance().chamsColor.getValue().getGreen() / 255f;
            float bC = CascadeAura.getInstance().chamsColor.getValue().getBlue() / 255f;
            float aC = CascadeAura.getInstance().chamsColor.getValue().getAlpha() / 255f;
            GL11.glColor4f(rC, gC, bC, aC);
        } else {
            GL11.glColor4f(r, g, b, a);
        }
        glPolygonMode(GL_FRONT_AND_BACK, GL_POLYGON_MODE);
        event.getModelNoBase().render(event.getEntityEnderCrystal(), 0, rotation * (rotations.getValue() / 10f), rotationMoved * 0.2F, 0, 0, 0.0625F);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        event.getModelNoBase().render(event.getEntityEnderCrystal(), 0, rotation * (rotations.getValue() / 10f), rotationMoved * 0.2F, 0, 0, 0.0625F);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_TEXTURE_2D);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glScaled(1.0, 1.0, 1.0);
        glPopAttrib();
        glPopMatrix();
    }
}