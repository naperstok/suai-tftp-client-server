package common;

import java.io.File;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ServerUDP {

    private final DatagramSocket socket;
    private final byte[] buf;
    private final File rootDir;

    public ServerUDP(int listenPort, int bufSize, String fileRoot) throws SocketException {
        this.buf = new byte[bufSize];
        this.socket = new DatagramSocket(listenPort);
        this.rootDir = new File(fileRoot);
    }

    public DatagramSocket getSocket() {
        return this.socket;
    }

    public byte[] getBuffer() {
        return this.buf;
    }

    public File getRootDir() {
        return this.rootDir;
    }

}

