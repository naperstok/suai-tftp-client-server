package commands;

import client.Client;
import client.ClientUtils;
import client.Notification;
import common.ClientUDP;
import common.Constants;
import common.codes.OpCode;
import common.packets.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class RRQ implements Command {
    private static final Logger logger = Logger.getLogger(RRQ.class);

    @Override
    public String getCommand() {
        return "rrq";
    }

    @Override
    public String getDescription() {
        return "Initiates a read-request";
    }

    @Override
    public String getUsage() {
        return "rrq <filename>";
    }

    @Override
    public void execute(String[] args) {
        if (!ClientUtils.validFileArgs(args)) {
            System.out.println(ClientUtils.commandUsageFormat(this));
            return;
        }

        String filename = args[0];

        String address = Client.getIp();
        ClientUDP client;

        try {
            client = new ClientUDP(address, Constants.SERVER_LISTEN_PORT);
            DatagramSocket socket = client.getSocket();
            byte[] buf = client.getBuffer();

            rrqPacketCreation(filename, socket, client);

            DatagramPacket serverPacket = new DatagramPacket(buf, buf.length);
            Packet packet = datagramPacketChecking(socket, serverPacket);

            if (packet.getOpcode() == OpCode.ERROR) {
                ErrorPacket errorPacket = new ErrorPacket(serverPacket.getData());
                String error = errorPacket.getErrorMessage();
                Notification.createNotification(Client.getMenu(), "Error!", true, false, error).setVisible(true);
                return;
            }

            ByteArrayOutputStream fileBuf = new ByteArrayOutputStream();
            int timeout = Constants.BASE_TIMEOUT;
            socket.setSoTimeout(timeout);

            DataPacket dataPacket = new DataPacket(serverPacket.getData());
            int bytesReceived = fileTransfer(socket, dataPacket, serverPacket, fileBuf, buf, timeout);

            logger.info("Received " + bytesReceived + " bytes");
            File file = new File(Client.CLIENT_ROOT, filename);
            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (!created) {
                    logger.error("Couldn't create file!");
                    client.close();
                    return;
                }
            }
            writingFile(file, fileBuf);
            client.close();
        } catch (IOException ex) {
            logger.fatal(ex);
            ex.printStackTrace();
        }
    }

    public static int  fileTransfer (DatagramSocket socket, DataPacket dataPacket, DatagramPacket serverPacket, ByteArrayOutputStream fileBuf, byte[] buf, int timeout) {
        int tries = 5;
        int bytesReceived = 0;
        boolean retransmit = false;

        while (true) {
            if (tries == 0)
                break;
            try {
                short ackNum = dataPacket.getBlockNumber();
                int receivedBlockSize = serverPacket.getLength() - 4;

                if (!retransmit) {
                    bytesReceived += receivedBlockSize;
                    fileBuf.write(dataPacket.getPayload(), 4, receivedBlockSize);
                    logger.info("Received block " + ackNum + " of size " + receivedBlockSize + " bytes");
                    logger.info("Sending ACK for block " + ackNum);
                }

                sendAckPacket(socket, serverPacket, ackNum);

                if (receivedBlockSize < Constants.BLOCK_SIZE) {
                    logger.info("Block " + ackNum + " has size < 512 bytes - file transfer complete!");
                    break;
                }

                serverPacket = new DatagramPacket(buf, buf.length);
                socket.receive(serverPacket);
                dataPacket = new DataPacket(serverPacket.getData());
                retransmit = false;
            } catch (IOException ex) {
                logger.error("Time is out... " + tries + " tries left.");
                timeout += 1000;
                tries--;
                try {
                    socket.setSoTimeout(timeout);
                } catch (SocketException e) {
                    logger.fatal(e);
                    e.printStackTrace();
                }
                retransmit = true;
            }
        }
        return bytesReceived;
    }

    public static void rrqPacketCreation(String filename, DatagramSocket socket, ClientUDP client) throws IOException {
        RRQPacket rrqPacket = new RRQPacket();
        rrqPacket.writeFilename(filename);
        byte[] payload = rrqPacket.getPayload();

        socket.setSoTimeout(5000);
        client.send(payload);
    }

    public static Packet datagramPacketChecking(DatagramSocket socket, DatagramPacket serverPacket) throws IOException {
        try {
            socket.receive(serverPacket);
        } catch (Exception exception) {
            logger.fatal(exception);
            Notification.createNotification(Client.getMenu(), "Error!", true, false, "Mistake! The server is not working!").setVisible(true);
        }
        return new Packet(serverPacket.getData());
    }


    public static void sendAckPacket(DatagramSocket socket, DatagramPacket serverPacket, short ackNum) throws IOException {
        ACKPacket ackPacket = new ACKPacket(ackNum);
        serverPacket.setData(ackPacket.getPayload());
        socket.send(serverPacket);
    }


    public static void writingFile(File file, ByteArrayOutputStream fileBuf) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileBuf.writeTo(fileOutputStream);
        Notification.createNotification(Client.getMenu(), "Success!", true, true, "Success! File downloaded!").setVisible(true);
        fileOutputStream.close();
    }
}
