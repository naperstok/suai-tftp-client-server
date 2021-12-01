package client;

import commands.Command;
import commands.CommandUtils;

import java.util.HashMap;

public class CommandParser {
    private final HashMap<String, Command> commands;

    public CommandParser() {
        CommandUtils.initializeCommands();
        this.commands = CommandUtils.getCommands();
    }

    public void parse(String input) {
        String[] content = input.split(" ");
        String cmd = content[0];
        String[] args = null;
        if (content.length > 1) {
            args = new String[content.length - 1];
            System.arraycopy(content, 1, args, 0, content.length - 1);
        }

        if (commands.containsKey(cmd.toLowerCase())) {
            commands.get(cmd.toLowerCase()).execute(args);
        } else {
            System.out.println("Unknown command!");
        }
    }

}
