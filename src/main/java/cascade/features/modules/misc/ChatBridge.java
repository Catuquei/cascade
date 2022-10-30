package cascade.features.modules.misc;

import cascade.event.events.PacketEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatBridge extends Module {

    public ChatBridge() {
        super("ChatBridge", Category.MISC, "discord chat bridge");
        INSTANCE = this;
    }

    Setting<Time> time = register(new Setting("Time", Time.Local));
    enum Time {Local}
    Setting<Boolean> ignoreDeaths = register(new Setting("IgnoreDeaths", true));
    Setting<Boolean> ignoreLogs = register(new Setting("IgnoreLogs", true));
    private static ChatBridge INSTANCE;

    public static ChatBridge getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new ChatBridge();
        }
        return INSTANCE;
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive e) throws Exception {
        if (isDisabled() || fullNullCheck()) {
            return;
        }
        if (e.getPacket() instanceof SPacketChat) {
            SPacketChat p = e.getPacket();
            String msg = p.getChatComponent().getUnformattedText();
            if (ignoreDeaths.getValue()) {
                //return;
            }
            if (ignoreLogs.getValue() && (msg.contains(" joined the game") || msg.contains( "left the game"))) {
                //return;
            }
            if (msg.contains("says: ") || msg.contains("whispers: ") || msg.contains("whisper")) {
                //return;
            }
            if (mc.currentServerData != null) {
                sendMessageWeb("https://discord.com/api/webhooks/964900554016964668/GDehsPvm4m4VZBscVmvBSP1am3u8oqoTqGs7_tKzi6CHfMicIQ2EK7GDU4q903x8upRg",
                             "" + mc.player.getName(),
                           "**[" + mc.currentServerData.serverIP + "]" + getTimeString() + "** " + "`" + msg + "`");
            }
        }
    }

    int sendMessageWeb(String url, String name, String message) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        String POST_PARAMS = "{ \"username\": \"" + name + "\", \"content\": \"" + message + "\" }";
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(POST_PARAMS.getBytes());
        os.flush();
        os.close();
        Thread.sleep(1l);
        return con.getResponseCode();
    }

    String getTimeString() {
        String date = new SimpleDateFormat("k:mm").format(new Date());
        return "<" + date + ">";
    }
}