package server;

import common.Constants;
import common.ServerUDP;
import common.codes.OpCode;
import common.packets.Packet;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server {
    private ServerUDP server;
    private static final Logger logger = Logger.getLogger(Server.class);

    public void start(String fileRoot) throws SocketException {

        server = new ServerUDP(Constants.SERVER_LISTEN_PORT, 1024, fileRoot);
        DatagramSocket socket = server.getSocket();
        byte[] recvBuf = server.getBuffer();
        logger.info("TFTP server started on port " + Constants.SERVER_LISTEN_PORT);

        while (true) {
            try {
                socket.setSoTimeout(0);
                DatagramPacket clientPacket = createDatagramPacket(recvBuf);
                socket.receive(clientPacket);
                Packet packet = createPacket(clientPacket);

                commandSelection(packet.getOpcode(), clientPacket);

            } catch (IOException ex) {
                logger.fatal(ex);
                ex.printStackTrace();
            }
        }
    }

    public boolean start(String fileRoot, boolean test) {

        try {
            server = new ServerUDP(Constants.SERVER_LISTEN_PORT, 1024, fileRoot);
            logger.info("TFTP server started on port " + Constants.SERVER_LISTEN_PORT);

            if (test) {
                while (true) {
                    return true;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean serverIsOnline() {
        return server != null;
    }

    public DatagramPacket createDatagramPacket (byte[] recvBuf) {
        if(recvBuf == null) return null;
        return new DatagramPacket(recvBuf, recvBuf.length);
    }

    public Packet createPacket (DatagramPacket clientPacket) {
        if(clientPacket == null) return null;
        return new Packet(clientPacket.getData());
    }

    public boolean commandSelection (OpCode string, DatagramPacket clientPacket) throws IOException {
        if(server == null) return false;
        switch (string) {
            case RRQ -> {
                logger.info("User connected and making a read-request");
                RRQ.handleOperation(server, clientPacket);
                break;
            }
            case WRQ -> {
                logger.info("User connected and making a write-request");
                WRQ.handleOperation(server, clientPacket);
                break;
            }
            default -> {
                return false;
            }
        }
        return true;
    }

}
