package cascade.util.shader.shaders;

import cascade.util.render.RenderUtil;
import cascade.util.shader.FramebufferShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class RainbowOutlineShader extends FramebufferShader {
        public static RainbowOutlineShader RAINBOW_OUTLINE_SHADER;
        public float time;

        public RainbowOutlineShader() {
            super("rainbowoutline.frag");
        }

        @Override
        public void setupUniforms() {
            this.setupUniform("resolution");
            this.setupUniform("time");
        }

        @Override
        public void updateUniforms() {
            GL20.glUniform2f(this.getUniform("resolution"), (float) new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth(), (float) new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight());
            GL20.glUniform1f(this.getUniform("time"), time);
            time += Float.intBitsToFloat(Float.floatToIntBits(1015.0615f) ^ 0x7F395856) * RenderUtil.deltaTime;
        }

        static {
            RAINBOW_OUTLINE_SHADER = new RainbowOutlineShader();
        }
    }