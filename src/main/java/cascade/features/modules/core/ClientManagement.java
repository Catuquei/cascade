package cascade.features.modules.core;

import cascade.Cascade;
import cascade.event.events.ClientEvent;
import cascade.features.command.Command;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.core.TextUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.Display;

public class ClientManagement extends Module {

    public ClientManagement() {
        super("ClientManagement", Category.CORE, "");
        INSTANCE = this;
    }

    Setting<Page> page = register(new Setting("Page", Page.Chat));
    enum Page {Chat, Client}

    public Setting<String> prefix = register(new Setting("Prefix", ".", v -> page.getValue() == Page.Client));
    public Setting<Boolean> noPacketKick = register(new Setting("NoPacketKick", true, v -> page.getValue() == Page.Client));
    public Setting<Boolean> unfocusedCPU = register(new Setting("UnfocusedCPU", true, v -> page.getValue() == Page.Client));
    public Setting<Integer> cpuFPS = register(new Setting("FPS", 60, 1, 144, v -> page.getValue() == Page.Client && unfocusedCPU.getValue()));
    Setting<Boolean> customTitle = register(new Setting("CustomTitle", false, v -> page.getValue() == Page.Client));
    public Setting<String> title = register(new Setting("Title", "Cascade 1.12.2", v -> page.getValue() == Page.Client && customTitle.getValue()));

    public Setting<Boolean> toggleName = register(new Setting("ToggleName", false, v -> page.getValue() == Page.Chat));
    public Setting<String> name = register(new Setting("Name", "Cascade", v -> page.getValue() == Page.Chat));
    Setting<TextUtil.Color> bracketColor = register(new Setting("BracketColor", TextUtil.Color.BLUE, v -> page.getValue() == Page.Chat));
    Setting<TextUtil.Color> nameColor = register(new Setting("NameColor", TextUtil.Color.BLUE, v -> page.getValue() == Page.Chat));
    Setting<String> lBracket = register(new Setting("LBracket", "[", v -> page.getValue() == Page.Chat));
    Setting<String> rBracket = register(new Setting("RBracket", "]", v -> page.getValue() == Page.Chat));

    private static ClientManagement INSTANCE = new ClientManagement();

    public static ClientManagement getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClientManagement();
        }
        return INSTANCE;
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent e) {
        if (e.getStage() == 2 && e.getSetting().getFeature().equals(this)) {
            if (e.getSetting() == prefix) {
                Cascade.chatManager.setPrefix(prefix.getPlannedValue());
                Command.sendMessage("Prefix set to " + Cascade.chatManager.getPrefix(), true, false);
            }
        }
        if (e.getStage() == 2 && equals(e.getSetting().getFeature())) {
            Cascade.chatManager.setClientMessage(getCommandMessage());
        }
    }

    @Override
    public void onLoad() {
        Cascade.chatManager.setClientMessage(getCommandMessage());
        Cascade.chatManager.setPrefix(prefix.getValue());
    }

    public String getCommandMessage() {
        return TextUtil.coloredString(lBracket.getValue(), bracketColor.getValue()) + TextUtil.coloredString((name.getValue()), nameColor.getValue()) + TextUtil.coloredString(rBracket.getValue(), bracketColor.getValue());
    }

    @Override
    public void onUpdate() {
        if (customTitle.getValue()) {
            Display.setTitle(title.getValueAsString());
        }
    }
}