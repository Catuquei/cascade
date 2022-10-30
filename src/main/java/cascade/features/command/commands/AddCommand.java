package cascade.features.command.commands;

import cascade.Cascade;
import cascade.features.command.Command;
import com.mojang.realmsclient.gui.ChatFormatting;

public class AddCommand extends Command {

    public AddCommand() {
        super("add", new String[]{"<name>"});
    }

    @Override
    public void execute(String[] commands) {
        Cascade.friendManager.addFriend(commands[1]);
        sendMessage(ChatFormatting.BOLD + "" + ChatFormatting.GREEN + commands[1] + " has been added to friends", true, false);
    }
}