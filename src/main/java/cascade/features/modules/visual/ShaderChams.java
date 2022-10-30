package cascade.features.modules.visual;

import cascade.Cascade;
import cascade.event.events.Render3DEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.misc.MathUtil;
import cascade.util.shader.FramebufferShader;
import cascade.util.shader.shaders.RainbowOutlineShader;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;

import java.util.Objects;

public class ShaderChams extends Module {

    public Setting<ShaderMode> mode = register(new Setting<>("Mode", ShaderMode.RainbowOutline));

    public enum ShaderMode {RainbowOutline}

    public ShaderChams() {
        super("ShaderChams", Category.VISUAL, "Makes shader on cham");
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (fullNullCheck()) {
            return;
        }
        FramebufferShader framebufferShader = null;
        if (mode.getValue().equals(ShaderMode.RainbowOutline))
            framebufferShader = RainbowOutlineShader.RAINBOW_OUTLINE_SHADER;

        if (framebufferShader == null)
            return;
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        framebufferShader.startDraw(event.getPartialTicks());
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity == mc.player || entity == mc.getRenderViewEntity())
                continue;
            if (!(entity instanceof EntityPlayer))
                continue;
            Vec3d vector = MathUtil.getInterpolatedRenderPos(entity, event.getPartialTicks());
            Objects.requireNonNull(mc.getRenderManager().getEntityRenderObject(entity)).doRender(entity, vector.x, vector.y, vector.z, entity.rotationYaw, event.getPartialTicks());
        }
        framebufferShader.stopDraw();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
    }
}