package commands;


import client.Client;
import client.ClientUtils;
import client.Notification;
import common.*;
import common.codes.OpCode;
import common.packets.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class WRQ implements Command {
    private static final Logger logger = Logger.getLogger(WRQ.class);

    @Override
    public String getCommand() {
        return "wrq";
    }

    @Override
    public String getDescription() {
        return "Initiates a write request";
    }

    @Override
    public String getUsage() {
        return "wrq <filename>";
    }

    @Override
    public void execute(String[] args) {
        if (!ClientUtils.validFileArgs(args)) {
            System.out.println(ClientUtils.commandUsageFormat(this));
            return;
        }

        String filename = args[0];

        File file = new File(Client.CLIENT_ROOT, filename);
        if (!(file.exists() || file.isDirectory())) {
            Notification.createNotification(Client.getMenu(), "Error!", true, false, "File doesn't exist!").setVisible(true);
            return;
        }

        String address = Client.getIp();
        ClientUDP client;
        int baseTimeout = Constants.BASE_TIMEOUT;

        try {
            client = new ClientUDP(address, Constants.SERVER_LISTEN_PORT);
            DatagramSocket socket = client.getSocket();
            socket.setSoTimeout(baseTimeout);
            byte[] buf = client.getBuffer();

            wrqPacketCreation(filename, client);

            DatagramPacket serverPacket = new DatagramPacket(buf, buf.length);
            socket.receive(serverPacket);
            Packet receivedPacket = new Packet(serverPacket.getData());

            if (receivedPacket.getOpcode() == OpCode.ERROR) {
                ErrorPacket errorPacket = new ErrorPacket(receivedPacket.getPayload());
                System.out.println(errorPacket.getErrorCode().toString() + ": " + errorPacket.getErrorMessage());
                return;
            }

            logger.info("Initial acknowledgement received from server - commencing file transfer...");
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] fileBuf = new byte[Constants.BLOCK_SIZE];
            int bytesRead = fileInputStream.read(fileBuf);

            fileTransfer(socket, client, serverPacket, fileInputStream, bytesRead, fileBuf);

            Notification.createNotification(Client.getMenu(), "Success!", true, true, "Success! File downloaded!").setVisible(true);
            client.close();
            fileInputStream.close();
        } catch (IOException ex) {
            logger.fatal(ex);
            ex.printStackTrace();
        }
    }

    public static void wrqPacketCreation(String filename, ClientUDP client) throws IOException {
        WRQPacket packet = new WRQPacket();
        packet.writeFilename(filename);
        byte[] payload = packet.getPayload();

        client.send(payload);
    }

    public static void fileTransfer(DatagramSocket socket, ClientUDP client, DatagramPacket serverPacket, FileInputStream fileInputStream, int bytesRead, byte[] fileBuf) throws IOException {
        short blockNum = 1;
        while (bytesRead != -1) {
            DataPacket dataPacket = new DataPacket(blockNum, fileBuf, 0, bytesRead);
            int bytesSent = client.send(dataPacket.getPayload()) - 4;
            logger.info("Block " + blockNum + " - Sent " + bytesSent + " bytes");
            logger.info("Waiting for server's ACK for block " + blockNum);
            socket.receive(serverPacket);
            ACKPacket ackPacket = new ACKPacket(serverPacket.getData());
            logger.info("ACK received for block " + ackPacket.getBlockNumber());

            bytesRead = fileInputStream.read(fileBuf);
            blockNum = (short) ((blockNum == Short.MAX_VALUE) ? 0 : blockNum + 1);
        }
    }

}
