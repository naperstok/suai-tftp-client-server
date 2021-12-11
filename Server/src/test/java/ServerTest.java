import common.codes.OpCode;
import org.junit.Assert;
import org.junit.Test;
import server.Server;

import java.io.IOException;
import java.net.SocketException;

public class ServerTest {

    @Test
    public void testPort() throws SocketException {
        Server server = new Server();
        Assert.assertFalse(server.serverIsOnline());

        Assert.assertTrue(server.start("C:\\Users\\User\\Desktop\\Kursovaya\\tftpProject\\Server\\src\\main\\resources", true));
    }

    @Test
    public void testDatagramPacketCreation() {
        Server server = new Server();
        Assert.assertNull(server.createDatagramPacket(null));
        Assert.assertNotNull(server.createDatagramPacket(new byte[]{123}));

    }

    @Test
    public void testPacketCreation(){
        Server server = new Server();
        Assert.assertNull(server.createPacket(null));

    }

    @Test
    public void testSwitchCase() throws IOException {
        Server server = new Server();
        server.start("C:\\Users\\User\\Desktop\\Kursovaya\\tftpProject\\Server\\src\\main\\resources", true);
        Assert.assertFalse(server.commandSelection(OpCode.RRQ, server.createDatagramPacket(new byte[]{123})));
        Assert.assertFalse(server.commandSelection(OpCode.ERROR, server.createDatagramPacket(new byte[]{123})));
    }
}
