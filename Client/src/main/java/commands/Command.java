package commands;

public interface Command {
    String getCommand();
    String getDescription();
    String getUsage();
    void execute(String[] args);

}
