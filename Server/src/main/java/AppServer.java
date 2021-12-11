import org.apache.log4j.Logger;
import server.Server;

import java.net.SocketException;

public class AppServer {
    private static final Logger logger = Logger.getLogger(AppServer.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar server.jar <file_path>");
            return;
        }

        try {
            Server server = new Server();
            server.start(args[0]);
        } catch (SocketException ex) {
            logger.fatal(ex);
            ex.printStackTrace();
        }
    }
}
