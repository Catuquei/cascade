package cascade.features.modules.misc;

import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class LogCoords extends Module {

    public LogCoords() {
        super("LogCoords", Module.Category.MISC, "");
    }

    Setting<Integer> maxRadius = register(new Setting("MaxRadius", 500, 100, 1000));

    @SubscribeEvent
    public void onPlayerLeave(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        if (!mc.isSingleplayer() && isEnabled() && !fullNullCheck()) {
            if (mc.player.posX > maxRadius.getValue() || mc.player.posZ > maxRadius.getValue()) {
                return;
            }
            int x = (int) mc.player.posX;
            int y = (int) mc.player.posY;
            int z = (int) mc.player.posZ;
            String coords = "Logout coords: " + x + "x  " + y + "y " + z + "z";
            StringSelection data = new StringSelection(coords);
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(data, data);
        }
    }
}