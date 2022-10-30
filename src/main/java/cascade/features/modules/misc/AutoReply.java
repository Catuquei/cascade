package cascade.features.modules.misc;

import cascade.Cascade;
import cascade.event.events.PacketEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.misc.CalcUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;

public class AutoReply extends Module {

    public AutoReply() {
        super("AutoReply", Category.MISC, "autp reply rbh");
    }

    Setting<Boolean> coords = register(new Setting("Coords", true));
    Setting<Boolean> ignoreY = register(new Setting("IgnoreY", true, v -> coords.getValue()));
    Setting<Integer> radius = register(new Setting("RadiusInThousands", 5, 1, 50, v -> coords.getValue()));

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive e) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (e.getPacket() instanceof SPacketChat) {
            SPacketChat p = e.getPacket();
            String msg = p.getChatComponent().getUnformattedText();
            if (msg.contains("says: ") || msg.contains("whispers: ")) {
                String ign = msg.split(" ")[0];
                if (mc.player.getName() == ign) {
                    return;
                }
                if (coords.getValue() && Cascade.friendManager.isFriend((ign)) && CalcUtil.getDistance(0, mc.player.posY, 0) < radius.getValue() * 1000) {
                    String lowerCaseMsg = msg.toLowerCase();
                    if (lowerCaseMsg.contains("cord") || lowerCaseMsg.contains("coord") || lowerCaseMsg.contains("coords") || lowerCaseMsg.contains("cords") || lowerCaseMsg.contains("wya") || lowerCaseMsg.contains("where are you") || lowerCaseMsg.contains("where r u") || lowerCaseMsg.contains("where ru")) {
                        if (lowerCaseMsg.contains("discord") || lowerCaseMsg.contains("record")) {
                            return;
                        }
                        int x = (int) mc.player.posX;
                        int y = (int) mc.player.posY;
                        int z = (int) mc.player.posZ;
                        mc.player.sendChatMessage("/msg " + ign + (" " + x + "x " + (ignoreY.getValue() ? "" : y + "y ") + z + "z"));

                        /*if (visualRange.getValue() != VisualRange.None) {
                            for (Entity en : mc.world.loadedEntityList) {
                                if (!(en instanceof EntityPlayer)) {
                                    continue;
                                }
                                if (players.containsKey(en.getName())) {
                                    players.put(en.getName(), en.getEntityId());
                                }
                            }
                            mc.player.sendChatMessage("/msg " + ign + (x + "x" + (ignoreY.getValue() ? "" : +y + "y") + z + "z"));
                        }*/
                    }
                }
            }
        }
    }
}