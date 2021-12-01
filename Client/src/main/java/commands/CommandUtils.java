package commands;

import commands.*;

import java.util.HashMap;

public class CommandUtils {

    private static final HashMap<String, Command> commands = new HashMap<>();

    public static void initializeCommands() {
        commands.put("help", new Help());
        commands.put("wrq", new WRQ());
        commands.put("rrq", new RRQ());
        commands.put("exit", new Exit());
    }

    public static HashMap<String, Command> getCommands() {
        return commands;
    }
}