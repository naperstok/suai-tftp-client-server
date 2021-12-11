package common;

import java.io.File;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Base class to UDP Server
 */
public class ServerUDP {

    private final DatagramSocket socket;
    private final byte[] buf;
    private final File rootDir;

    /**
     * Create an instance of a UDP Server
     * @param listenPort The port which the UDP Server will listen
     * @param bufSize Size of the UDP Server's buffer
     * @param fileRoot Path to the resource folder
     * @throws SocketException if an error occurs
     */
    public ServerUDP(int listenPort, int bufSize, String fileRoot) throws SocketException {
        this.buf = new byte[bufSize];
        this.socket = new DatagramSocket(listenPort);
        this.rootDir = new File(fileRoot);
    }

    /**
     * Get the DatagramSocket for the UDP Server
     * @return the DatagramSocket
     */
    public DatagramSocket getSocket() {
        return this.socket;
    }

    /**
     * Get the buffer for the UDP Server
     * @return the buffer
     */
    public byte[] getBuffer() {
        return this.buf;
    }

    /**
     * Get the root directory path for resources
     * @return the path
     */
    public File getRootDir() {
        return this.rootDir;
    }

}

