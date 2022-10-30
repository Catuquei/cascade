package cascade.features.modules.misc;

import cascade.event.events.PacketEvent;
import cascade.event.events.Render3DEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.render.RenderUtil;
import cascade.util.misc.Timer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class ChorusPredict extends Module {

    public ChorusPredict() {
        super("ChorusPredict", Category.MISC, "Predicts chorus pos");
        INSTANCE = this;
    }

    Setting<Integer> removeDelay = this.register(new Setting("RemoveDelay", 4000, 0, 4000));
    //Setting<Boolean> text = register(new Setting("Text", false));
    //Setting<Boolean> noNetherRoof = register(new Setting("NoNetherRoof", true));
    Setting<Boolean> box = register(new Setting("Box", true));
    Setting<Boolean> tracer = register(new Setting("Tracer", false));
    Setting<Color> c = register(new Setting("Color", new Color(-1), v -> box.getValue()));
    Setting<Boolean> outline = register(new Setting("Outline", true));
    Setting<Float> outlineWidth = register(new Setting("Width", 1.5f, 0.1f, 3.0f, v -> outline.getValue()));
    Timer renderTimer = new Timer();
    static ChorusPredict INSTANCE;
    BlockPos pos;

    public static ChorusPredict getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new ChorusPredict();
        }
        return INSTANCE;
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof SPacketSoundEffect && isEnabled()) {
            SPacketSoundEffect packet = e.getPacket();
            if (packet.getSound() == SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT || packet.getSound() == SoundEvents.ENTITY_ENDERMEN_TELEPORT) {
                renderTimer.reset();
                pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
            }
        }
    }

    @Override
    public void onRender3D(Render3DEvent e) {
        if (pos != null) {
            if (renderTimer.passedMs(removeDelay.getValue())) {
                renderTimer.reset();
                pos = null;
                return;
            }
            if (box.getValue()) {
                RenderUtil.drawBoxESP(pos, new Color(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha()), outlineWidth.getValue(), outline.getValue(), box.getValue(), c.getValue().getAlpha());
            }
            /*if (text.getValue()) {
                RenderUtil.drawText(pos, "");
            }*/
            if (tracer.getValue()) {
                RenderUtil.drawLineFromPosToPos(mc.player.posX, mc.player.posY, mc.player.posZ, pos.getX(), pos.getY(), pos.getZ(), 0, c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha());
            }
        }
    }

    @Override
    public void onDisable() {
        renderTimer.reset();
        pos = null;
    }
}