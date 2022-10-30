package cascade.features.modules.visual;

import cascade.event.events.Render3DEvent;
import cascade.event.events.RenderItemInFirstPersonEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.Display;

import java.awt.*;

public class ItemChams extends Module {

    public ItemChams() {
        super("ItemChams", Category.VISUAL, "");
        INSTANCE = this;
    }

    Setting<Page> page = register(new Setting("Page", Page.Glint));
    public enum Page {Glint, Chams}

    public Setting<Boolean> glint  = register(new Setting("ModifyGlint", false));
    public  Setting<Float> scale        = register(new Setting("GlintScale", 8.0f, 0.1f, 20.0f));
    public Setting<Float> glintMult    = register(new Setting("GlintMultiplier", 1.0f, 0.1f, 10.0f));
    public  Setting<Float> glintRotate  = register(new Setting("GlintRotate", 1.0f, 0.1f, 10.0f));
    public  Setting<Color> glintColor   = register(new Setting("GlintColor", Color.RED));
    public  Setting<Boolean> chams      = register(new Setting("Chams", false));
    public  Setting<Boolean> blur       = register(new Setting("Blur", false));
    public  Setting<Float> radius       = register(new Setting("Radius", 2.0f, 0.1f, 10.0f));
    public  Setting<Float> mix          = register(new Setting("Mix", 1.0f, 0.0f, 1.0f));
    public  Setting<Boolean> useImage   = register(new Setting("UseImage", false));
    public Setting<Boolean> useGif        = register(new Setting("UseGif", false));
    //public  Setting<GifImage> gif          = register(new Setting("Gif", Managers.FILES.getInitialGif(), Managers.FILES.getGifs()));
    //public  Setting<NameableImage> image   = register(new Setting("Image", Managers.FILES.getInitialImage(), Managers.FILES.getImages()));
    public  Setting<Float> imageMix     = register(new Setting("ImageMix", 1.0f, 0.0f, 1.0f));
    public  Setting<Boolean> rotate     = register(new Setting("Rotate", false));
    public  Setting<Color> chamColor    = register(new Setting("Color", Color.RED));

    //GlShader shader = new GlShader("item");

    //FramebufferWrapper wrapper = new FramebufferWrapper();

    boolean forceRender = false;

    private static ItemChams INSTANCE;

    public static ItemChams getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemChams();
        }
        return INSTANCE;
    }
    @SubscribeEvent
    public void invoke(RenderItemInFirstPersonEvent event) {
        if (event.getStage() == 0 && isEnabled()) {
            if (!forceRender && chams.getValue()) {
                event.setCanceled(true);
            }
        }
    }

    private void render(RenderItemInFirstPersonEvent event) {
        mc.getItemRenderer().renderItemSide(event.getEntity(), event.getStack(), event.getTransformType(), event.isLeftHanded());
    }

    @SubscribeEvent
    public void invoke(Render3DEvent event) { //im not sure ab this shiitttt
        if (Display.isActive() || Display.isVisible()) {
            if (chams.getValue()) {
                GlStateManager.pushMatrix();
                GlStateManager.pushAttrib();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.enableAlpha();
                //ItemShader shader = ItemShader.ITEM_SHADER;
                //shader.blur = blur.getValue();
                //shader.mix = mix.getValue();
                //shader.alpha = chamColor.getValue().getAlpha() / 255.0f;
                //shader.imageMix = imageMix.getValue();
                //shader.useImage = useImage.getValue();
                //shader.startDraw(mc.getRenderPartialTicks());
                forceRender = true;
                //((IEntityRenderer) mc.entityRenderer).invokeRenderHand(mc.getRenderPartialTicks(), 2);
                forceRender = false;
                //shader.stopDraw(chamColor.getValue(), radius.getValue(), 1.0f);
                GlStateManager.disableBlend();
                GlStateManager.disableAlpha();
                GlStateManager.disableDepth();
                GlStateManager.popAttrib();
                GlStateManager.popMatrix();
            }
        }
    }
}