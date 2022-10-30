package cascade.features.modules.visual;

import cascade.event.events.PacketEvent;
import cascade.event.events.Render2DEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.misc.Timer;
import cascade.util.render.ColorUtil;
import cascade.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HitMarkers extends Module {

    public HitMarkers() {
        super("HitMarkers", Category.VISUAL, "draws hitmarkers when u hit something");
    }

    Setting<Integer> time = register(new Setting("Time", 50, 0, 1000));
    Setting<Float> size = register(new Setting("Size", 6.0f, 0.0f, 8.0f));
    //Setting<Boolean> sound = register(new Setting("Sound", false)); nah what the fuck
    //todo ignore crystals???
    Timer timer = new Timer();

    @Override
    public void onDisable() {
        timer.reset();
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send e) {
        if (isDisabled() || fullNullCheck()) {
            return;
        }
        if (e.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity p = e.getPacket();
            if (p.getAction() == CPacketUseEntity.Action.ATTACK) {
                timer.reset();
            }
        }
    }

    @Override
    public void onRender2D(Render2DEvent e) {
        if (fullNullCheck()) {
            return;
        }
        if (!timer.passedMs(time.getValue())) {
            ScaledResolution resolution = new ScaledResolution(mc);
            RenderUtil.drawLine(resolution.getScaledWidth() / 2.0f - 4.0f, resolution.getScaledHeight() / 2.0f - 4.0f, resolution.getScaledWidth() / 2.0f - size.getValue(), resolution.getScaledHeight() / 2.0f - size.getValue(), 1.0f, ColorUtil.toRGBA(255, 255, 255, 255));
            RenderUtil.drawLine(resolution.getScaledWidth() / 2.0f + 4.0f, resolution.getScaledHeight() / 2.0f - 4.0f, resolution.getScaledWidth() / 2.0f + size.getValue(), resolution.getScaledHeight() / 2.0f - size.getValue(), 1.0f, ColorUtil.toRGBA(255, 255, 255, 255));
            RenderUtil.drawLine(resolution.getScaledWidth() / 2.0f - 4.0f, resolution.getScaledHeight() / 2.0f + 4.0f, resolution.getScaledWidth() / 2.0f - size.getValue(), resolution.getScaledHeight() / 2.0f + size.getValue(), 1.0f, ColorUtil.toRGBA(255, 255, 255, 255));
            RenderUtil.drawLine(resolution.getScaledWidth() / 2.0f + 4.0f, resolution.getScaledHeight() / 2.0f + 4.0f, resolution.getScaledWidth() / 2.0f + size.getValue(), resolution.getScaledHeight() / 2.0f + size.getValue(), 1.0f, ColorUtil.toRGBA(255, 255, 255, 255));
        }
    }
}