import server.Server;

import java.net.SocketException;

public class AppServer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar server.jar <file_path>");
            return;
        }

        try {
            Server server = new Server();
            server.start(args[0]);
        } catch (SocketException exception) {
            exception.printStackTrace();
        }
    }
}
