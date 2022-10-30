package cascade.util.core;

import cascade.Cascade;
import cascade.features.command.Command;
import cascade.util.Util;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.TextComponentString;

import static cascade.features.Feature.nullCheck;

public class MessageUtil implements Util {

    /*public static void enableMessage(Module module) {
        if (!fullNullCheck()) {
            TextComponentString text = new TextComponentString(Cascade.chatManager.getClientMessage() + " " +
                    ClientManagement.getInstance().message.getValue() +

                    TextUtil.coloredString(String.format("", module + ClientManagement.getInstance().toggle.getValue()) ? "toggled " : "", ClientManagement.getInstance().message.getValue());
            mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(text, 1);
        }
    }

    public static void disableMessage(Module module) {
        if (!fullNullCheck()) {
            TextComponentString text = new TextComponentString(
            Cascade.chatManager.getClientMessage() + " " + ChatFormatting.RED + module + " toggled off.");
            mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(text, 1);
        }
    }*/

    public static void sendMessage(String message, Boolean client, Boolean bold) {
        sendSilentMessage((client ? Cascade.chatManager.getClientMessage() : "") + (bold ? ChatFormatting.BOLD : "") + " " + message);
    }

    public static void sendMessage(String message) {
        sendSilentMessage(Cascade.chatManager.getClientMessage() + " " + message);
    }

    public static void sendRemovableMessage(String message, int id) {
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(message), id);
    }

    public static void sendSilentMessage(String message) {
        if (nullCheck()) {
            return;
        }
        mc.player.sendMessage(new Command.ChatMessage(message));
    }

    public static void getColor() {

    }
}