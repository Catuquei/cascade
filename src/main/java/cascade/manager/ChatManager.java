package cascade.manager;

import cascade.event.events.ClientEvent;
import cascade.features.Feature;
import cascade.features.command.Command;
import cascade.features.command.commands.*;
import cascade.features.modules.Module;
import cascade.features.modules.core.ClientManagement;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.LinkedList;

public class ChatManager extends Feature {

    ArrayList<Command> com = new ArrayList();
    String clientMessage = ClientManagement.getInstance().name.getValue();
    String prefix = ".";

    public ChatManager() {
        super("Command");
        com.add(new AddCommand());
        com.add(new BindCommand());
        com.add(new ConfigCommand());
        com.add(new FriendCommand());
        com.add(new ModuleCommand());
        com.add(new RemoveCommand());
    }

    public static String[] removeElement(String[] input, int indexToDelete) {
        LinkedList<String> result = new LinkedList<>();
        for (int i = 0; i < input.length; ++i) {
            if (i == indexToDelete) {
                continue;
            }
            result.add(input[i]);
        }
        return result.toArray(input);
    }

    static String strip(String str, String key) {
        if (str.startsWith(key) && str.endsWith(key)) {
            return str.substring(key.length(), str.length() - key.length());
        }
        return str;
    }

    public void executeCommand(String command) {
        String[] parts = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        String name = parts[0].substring(1);
        String[] args = ChatManager.removeElement(parts, 0);
        for (int i = 0; i < args.length; ++i) {
            if (args[i] == null) {
                continue;
            }
            args[i] = ChatManager.strip(args[i], "\"");
        }
        for (Command c : com) {
            if (!c.getName().equalsIgnoreCase(name)) {
                continue;
            }
            c.execute(parts);
            return;
        }
    }

    public Command getCommandByName(String name) {
        for (Command command : com) {
            if (!command.getName().equals(name)) {
                continue;
            }
            return command;
        }
        return null;
    }

    public ArrayList<Command> getCommands() {
        return com;
    }

    public String getClientMessage() {
        return this.clientMessage + ChatFormatting.BOLD;
    }

    public void setClientMessage(String clientMessage) {
        this.clientMessage = clientMessage;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}