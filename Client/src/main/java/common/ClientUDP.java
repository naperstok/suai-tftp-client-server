package common;
import java.io.IOException;
import java.net.*;

/**
 * Base class to UDP Client
 */
public class ClientUDP {

    private final DatagramSocket socket;
    private final InetAddress address;
    private final int listenPort;
    private final byte[] buf;

    /**
     * Create an instance of a UDP Client
     * @param address Address of the UDP server
     * @param listenPort Port number of the UDP server
     * @throws UnknownHostException if the host is invalid
     * @throws SocketException if the socket cannot be initialized
     */
    public ClientUDP(String address, int listenPort) throws UnknownHostException, SocketException {
        this.address = InetAddress.getByName(address);
        this.socket = new DatagramSocket();
        this.listenPort = listenPort;
        this.buf = new byte[1024];
    }

    /**
     * Send a byte array payload to the host
     * @param payload A byte array payload
     * @return number of bytes sent
     * @throws IOException if an error occurs
     */
    public int send(byte[] payload) throws IOException {
        DatagramPacket packet = new DatagramPacket(payload, payload.length, this.address, this.listenPort);
        socket.send(packet);
        return packet.getLength();
    }

    /**
     * Close the DatagramSocket
     */
    public void close() {
        this.socket.close();
    }

    /**
     *  Get the DatagramSocket for the UDP Client
     * @return the DatagramSocket
     */
    public DatagramSocket getSocket() {
        return this.socket;
    }

    /**
     * Get the buffer for the UDP Client
     * @return the buffer
     */
    public byte[] getBuffer() {
        return this.buf;
    }

}
