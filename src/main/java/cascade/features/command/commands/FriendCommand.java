package cascade.features.command.commands;

import cascade.Cascade;
import cascade.features.command.Command;
import cascade.manager.FriendManager;
import com.mojang.realmsclient.gui.ChatFormatting;

public class FriendCommand extends Command {

    public FriendCommand() {
        super("friend", new String[]{"<add/del/name/clear>", "<name>"});
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            if (Cascade.friendManager.getFriends().isEmpty()) {
                FriendCommand.sendMessage("Friend list empty D:.", true, false);
            } else {
                String f = "Friends: ";
                for (FriendManager.Friend friend : Cascade.friendManager.getFriends()) {
                    try {
                        f = f + friend.getUsername() + ", ";
                    } catch (Exception exception) {
                    }
                }
                FriendCommand.sendMessage(f, true, false);
            }
            return;
        }
        if (commands.length == 2) {
            switch (commands[0]) {
                case "reset": {
                    Cascade.friendManager.onLoad();
                    FriendCommand.sendMessage("Friends got reset.", true, false);
                    return;
                }
            }
            FriendCommand.sendMessage(commands[0] + (Cascade.friendManager.isFriend(commands[0]) ? " is friended." : " isn't friended."), true, false);
            return;
        }
        if (commands.length >= 2) {
            switch (commands[0]) {
                case "add": {
                    Cascade.friendManager.addFriend(commands[1]);
                    sendMessage(ChatFormatting.GREEN + commands[1] + " has been friended", true, false);
                    return;
                }
                case "del": {
                    Cascade.friendManager.removeFriend(commands[1]);
                    sendMessage(ChatFormatting.RED + commands[1] + " has been unfriended", true, false);
                    return;
                }
            }
            sendMessage("Unknown Command, try friend add/del (name)", true, false);
        }
    }
}

