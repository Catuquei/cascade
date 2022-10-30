package cascade.features.modules.visual;

import cascade.Cascade;
import cascade.event.events.PerspectiveEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.render.ColorUtil;
import cascade.util.render.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class Visual extends Module {

    public Visual() {
        super("Visual", Category.VISUAL, "Visual tweaks");
        INSTANCE = this;
    }

    Setting<Page> page = register(new Setting("Page", Page.Player));
    enum Page {Player, World}

    Setting<Boolean> instantSwap = register(new Setting("InstantSwap", false, v -> page.getValue() == Page.Player));
    public Setting<Boolean> shulkerPreview = register(new Setting("ShulkerPreview", true, v -> page.getValue() == Page.Player));
    Setting<Boolean> fovChanger = register(new Setting("FovChanger", false, v -> page.getValue() == Page.Player));
    Setting<Boolean> stay = register(new Setting("Stay", false, v -> page.getValue() == Page.Player && fovChanger.getValue()));
    Setting<Integer> fov = register(new Setting("Fov", 137, -180, 180, v -> page.getValue() == Page.Player && fovChanger.getValue()));
    public Setting<Swing> swing = register(new Setting("Swing", Swing.Mainhand, v -> page.getValue() == Page.Player));
    public enum Swing {Mainhand, Offhand, Packet}
    public Setting<Boolean> aspect = register(new Setting("Aspect", false, v -> page.getValue() == Page.Player));
    public Setting<Float> aspectValue = register(new Setting("Value", 0.0f, 0.0f, 3.0f, v -> page.getValue() == Page.Player && aspect.getValue()));

    Setting<Boolean> fullBright = register(new Setting("FullBright", true, v -> page.getValue() == Page.World));
    Setting<Boolean> skyChanger = register(new Setting("SkyChanger", false, v -> page.getValue() == Page.World));
    Setting<Color> c = register(new Setting("SkyColor", new Color(-1), v -> page.getValue() == Page.World));
    static ResourceLocation SHULKER_GUI_TEXTURE = new ResourceLocation("textures/gui/container/shulker_box.png");
    static Visual INSTANCE = new Visual();
    float originalBrightness;

    public static Visual getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Visual();
        }
        return INSTANCE;
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (instantSwap.getValue()) {
            if (mc.entityRenderer.itemRenderer.prevEquippedProgressMainHand >= 0.9) {
                mc.entityRenderer.itemRenderer.equippedProgressMainHand = 1.0f;
                mc.entityRenderer.itemRenderer.itemStackMainHand = mc.player.getHeldItemMainhand();
            }
        }
        if (fovChanger.getValue() && !stay.getValue()) {
            mc.gameSettings.setOptionFloatValue(GameSettings.Options.FOV, fov.getValue());
        }
        if (fullBright.getValue() && mc.gameSettings.gammaSetting != 42069.0f) {
            mc.gameSettings.gammaSetting = 42069.0f;
        }
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }
        originalBrightness = mc.gameSettings.gammaSetting;
        if (fullBright.getValue()) {
            mc.gameSettings.gammaSetting = 42069.0f;
        }
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) {
            return;
        }
        mc.gameSettings.gammaSetting = originalBrightness;
    }

    @SubscribeEvent
    public void onFogColor(EntityViewRenderEvent.FogColors e) {
        if (skyChanger.getValue() && isEnabled() && e != null) {
            e.setRed(c.getValue().getRed() / 255.0f);
            e.setGreen(c.getValue().getGreen() / 255.0f);
            e.setBlue(c.getValue().getBlue() / 255.0f);
        }
    }

    @SubscribeEvent
    public void onFovChange(EntityViewRenderEvent.FOVModifier e) {
        if (fovChanger.getValue() && stay.getValue() && isEnabled() && e != null) {
            e.setFOV(fov.getValue());
        }
    }

    @SubscribeEvent
    public void onPerspectiveEvent(PerspectiveEvent e) {
        if (aspect.getValue() && isEnabled() && e != null) {
            e.setAspect(aspectValue.getValue());
        }
    }
    public void renderShulkerToolTip(ItemStack stack, int x, int y, String name) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("BlockEntityTag", 10)) {
            NBTTagCompound blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag");
            if (blockEntityTag.hasKey("Items", 9)) {
                GlStateManager.enableTexture2D();
                GlStateManager.disableLighting();
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                mc.getTextureManager().bindTexture(SHULKER_GUI_TEXTURE);
                RenderUtil.drawTexturedRect(x, y, 0, 0, 176, 16, 500);
                RenderUtil.drawTexturedRect(x, y + 16, 0, 16, 176, 54, 500);
                RenderUtil.drawTexturedRect(x, y + 16 + 54, 0, 160, 176, 8, 500);
                GlStateManager.disableDepth();
                Color color = new Color(-1);
                renderer.drawStringWithShadow((name == null) ? stack.getDisplayName() : name, (float)(x + 8), (float)(y + 6), ColorUtil.toRGBA(color));
                GlStateManager.enableDepth();
                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.enableRescaleNormal();
                GlStateManager.enableColorMaterial();
                GlStateManager.enableLighting();
                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(blockEntityTag, nonnulllist);
                for (int i = 0; i < nonnulllist.size(); ++i) {
                    int iX = x + i % 9 * 18 + 8;
                    int iY = y + i / 9 * 18 + 18;
                    ItemStack itemStack = nonnulllist.get(i);
                    mc.getRenderItem().zLevel = 501.0f;
                    RenderUtil.itemRender.renderItemAndEffectIntoGUI(itemStack, iX, iY);
                    RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, iX, iY, null);
                    mc.getRenderItem().zLevel = 0.0f;
                }
                GlStateManager.disableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }
}