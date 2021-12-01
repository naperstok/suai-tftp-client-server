import common.codes.OpCode;
import org.junit.Assert;
import org.junit.Test;
import server.Server;

import java.net.SocketException;

public class ServerTest {

    @Test
    public void testPort() throws SocketException {
        Server server = new Server();
        Assert.assertFalse(server.serverIsOnline());

        Assert.assertTrue(server.start("..\\server_root", true));
    }

    @Test
    public void testSendReport() {
        Server server = new Server();
        Assert.assertTrue(server.sendReport("test report"));
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
    public void testSwitchCase() throws SocketException {
        Server server = new Server();
        server.start("server_root", true);
        Assert.assertFalse(server.f(OpCode.RRQ, server.createDatagramPacket(new byte[]{123})));
        Assert.assertFalse(server.f(OpCode.ERROR, server.createDatagramPacket(new byte[]{123})));
    }
}
