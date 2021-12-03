package server;

import common.Constants;
import common.ServerUDP;
import common.codes.OpCode;
import common.packets.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server {
    private ServerUDP server;

    public void start(String fileRoot) throws SocketException {

        server = new ServerUDP(Constants.SERVER_LISTEN_PORT, 1024, fileRoot);
        DatagramSocket socket = server.getSocket();
        byte[] recvBuf = server.getBuffer();

        System.out.printf("TFTP server started on port %d\n", Constants.SERVER_LISTEN_PORT);
        LogWriter.writeEvent("TFTP server started on port " + Constants.SERVER_LISTEN_PORT + "\n");

        while (true) {
            try {
                socket.setSoTimeout(0);
                DatagramPacket clientPacket = createDatagramPacket(recvBuf);
                socket.receive(clientPacket);
                Packet packet = createPacket(clientPacket);

                commandSelection(packet.getOpcode(), clientPacket);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean start(String fileRoot, boolean test) throws SocketException {

        try {
            server = new ServerUDP(Constants.SERVER_LISTEN_PORT, 1024, fileRoot);
            DatagramSocket socket = server.getSocket();
            byte[] recvBuf = server.getBuffer();

            System.out.printf("TFTP server started on port %d\n", Constants.SERVER_LISTEN_PORT);
            LogWriter.writeEvent("TFTP server started on port " + Constants.SERVER_LISTEN_PORT + "\n");

            if (!test) {
                while (true) {
                    try {
                        socket.setSoTimeout(0);
                        DatagramPacket clientPacket = createDatagramPacket(recvBuf);
                        socket.receive(clientPacket);
                        Packet packet = createPacket(clientPacket);

                        commandSelection(packet.getOpcode(), clientPacket);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean sendReport(String report) {
        return LogWriter.writeEvent(report);
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
                sendReport("User connected and making a read-request\n");
                RRQ.handleOperation(server, clientPacket);
                break;
            }
            case WRQ -> {
                sendReport("User connected and making a write-request\n");
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
