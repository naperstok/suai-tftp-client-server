package common;
import java.io.IOException;
import java.net.*;

public class ClientUDP {

    private final DatagramSocket socket;
    private final InetAddress address;
    private final int listenPort;
    private final byte[] buf;

    public ClientUDP(String address, int listenPort) throws UnknownHostException, SocketException {
        this.address = InetAddress.getByName(address);
        this.socket = new DatagramSocket();
        this.listenPort = listenPort;
        this.buf = new byte[1024];
    }

    public int send(byte[] payload) throws IOException {
        DatagramPacket packet = new DatagramPacket(payload, payload.length, this.address, this.listenPort);
        socket.send(packet);
        return packet.getLength();
    }

    public void close() {
        this.socket.close();
    }

    public DatagramSocket getSocket() {
        return this.socket;
    }

    public byte[] getBuffer() {
        return this.buf;
    }

}
