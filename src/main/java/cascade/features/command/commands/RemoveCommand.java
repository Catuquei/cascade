package cascade.features.command.commands;

import cascade.Cascade;
import cascade.features.command.Command;
import com.mojang.realmsclient.gui.ChatFormatting;

public class RemoveCommand extends Command {

    public RemoveCommand() {
        super("remove", new String[]{"<name>"});
    }

    @Override
    public void execute(String[] commands) {
        Cascade.friendManager.removeFriend(commands[1]);
        sendMessage(ChatFormatting.BOLD + "" + ChatFormatting.RED + commands[1] + " has been removed from friends", true, false);
    }
}
