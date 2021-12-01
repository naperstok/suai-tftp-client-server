package server;

import java.io.FileWriter;
import java.io.IOException;

public class LogWriter {

    private static final String FILEPATH = "log.txt";
    private static FileWriter fileWriter;

    static {
        try {
            fileWriter = new FileWriter(FILEPATH);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static boolean writeEvent(String event) {
        try {
            fileWriter.write(event);
            fileWriter.flush();
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
    }

}
