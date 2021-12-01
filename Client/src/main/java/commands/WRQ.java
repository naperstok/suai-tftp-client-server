package commands;


import client.Client;
import client.ClientUtils;
import client.Notification;
import common.*;
import common.codes.OpCode;
import common.packets.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class WRQ implements Command {

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

            WRQpacket packet = new WRQpacket();
            packet.writeFilename(filename);
            byte[] payload = packet.getPayload();

            client.send(payload);

            DatagramPacket serverPacket = new DatagramPacket(buf, buf.length);
            socket.receive(serverPacket);
            Packet receivedPacket = new Packet(serverPacket.getData());

            if (receivedPacket.getOpcode() == OpCode.ERROR) {
                ErrorPacket errorPacket = new ErrorPacket(receivedPacket.getPayload());
                System.out.println(errorPacket.getErrorCode().toString() + ": " + errorPacket.getErrorMessage());
                return;
            }

            System.out.println("Initial acknowledgement received from server - commencing file transfer...");

            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] fileBuf = new byte[Constants.BLOCK_SIZE];
            short blockNum = 1;
            int bytesRead = fileInputStream.read(fileBuf);

            while (bytesRead != -1) {
                DataPacket dataPacket = new DataPacket(blockNum, fileBuf, 0, bytesRead);
                int bytesSent = client.send(dataPacket.getPayload()) - 4;
                System.out.println("Block " + blockNum + " - Sent " + bytesSent + " bytes");

                System.out.println("Waiting for server's ACK for block " + blockNum);
                socket.receive(serverPacket);
                ACKpacket ackPacket = new ACKpacket(serverPacket.getData());
                System.out.println("ACK received for block " + ackPacket.getBlockNumber());

                bytesRead = fileInputStream.read(fileBuf);
                blockNum = (short) ((blockNum == Short.MAX_VALUE) ? 0 : blockNum + 1);
            }
            Notification.createNotification(Client.getMenu(), "Success!", true, true, "Success! File downloaded!").setVisible(true);
            client.close();
            fileInputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
