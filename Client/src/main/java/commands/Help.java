package commands;


import client.ClientUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;

public class Help implements Command {
    private static final Logger logger = Logger.getLogger(Help.class);

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Displays a list of available commands with their descriptions.";
    }

    @Override
    public String getUsage() {
        return "help | help <command_name>";
    }

    @Override
    public void execute(String[] args) {
        HashMap<String, Command> commands = CommandUtils.getCommands();

        if (args == null) {
            for (Command command : commands.values()) {
                System.out.println(command.getCommand() + " - " + command.getDescription());
            }
        } else {
            if (commands.containsKey(args[0])) {
                Command command = commands.get(args[0]);
                System.out.println(ClientUtils.commandUsageFormat(command));
            } else {
                logger.error("Unknown command!");
                System.out.println("Unknown command!");
            }
        }
    }

}
