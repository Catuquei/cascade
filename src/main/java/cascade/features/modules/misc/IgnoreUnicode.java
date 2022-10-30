package cascade.features.modules.misc;

import cascade.event.events.PacketEvent;
import cascade.features.command.Command;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class   IgnoreUnicode extends Module {

    public IgnoreUnicode() {
        super("IgnoreUnicode", Category.MISC, "");
    }

    Setting<Integer> threshold = register(new Setting("Threshold", 420, 10, 550));

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive e) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (e.getPacket() instanceof SPacketChat) {
            String chatMessage = ((SPacketChat) e.getPacket()).getChatComponent().getUnformattedText();
            if (chatMessage.getBytes().length > threshold.getValue()) {
                e.setCanceled(true);
                Command.sendMessage("Ignored unicode message(" + chatMessage.getBytes().length + ")");
            }
        }
    }
}