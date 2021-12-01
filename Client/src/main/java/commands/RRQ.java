package commands;

import client.Client;
import client.ClientUtils;
import client.Notification;
import common.ClientUDP;
import common.Constants;
import common.codes.OpCode;
import common.packets.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class RRQ implements Command {

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
            RRQpacket rrqPacket = new RRQpacket();

            rrqPacket.writeFilename(filename);
            byte[] payload = rrqPacket.getPayload();

            socket.setSoTimeout(5000);
            client.send(payload);

            DatagramPacket serverPacket = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(serverPacket);
            } catch (Exception exception) {
                Notification.createNotification(Client.getMenu(), "Error!", true, false, "Mistake! The server is not working!").setVisible(true);
            }

            Packet packet = new Packet(serverPacket.getData());

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
                        System.out.println("Received block " + ackNum + " of size " + receivedBlockSize + " bytes\nSending ACK for block " + ackNum);
                    }

                    ACKpacket ackPacket = new ACKpacket(ackNum);
                    serverPacket.setData(ackPacket.getPayload());
                    socket.send(serverPacket);

                    if (receivedBlockSize < Constants.BLOCK_SIZE) {
                        System.out.println("Block " + ackNum + " has size < 512 bytes - file transfer complete!");
                        break;
                    }

                    serverPacket = new DatagramPacket(buf, buf.length);
                    socket.receive(serverPacket);
                    dataPacket = new DataPacket(serverPacket.getData());
                    retransmit = false;
                } catch (SocketTimeoutException ex) {
                    System.out.println("Time is out... " + tries + " tries left.");
                    timeout += 1000;
                    tries--;
                    socket.setSoTimeout(timeout);
                    retransmit = true;
                }
            }

            System.out.println("Received " + bytesReceived + " bytes");
            File file = new File(Client.CLIENT_ROOT, filename);
            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (!created) {
                    System.out.println("Couldn't create file!");
                    client.close();
                    return;
                }
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileBuf.writeTo(fileOutputStream);
            Notification.createNotification(Client.getMenu(), "Success!", true, true, "Success! File downloaded!").setVisible(true);
            fileOutputStream.close();
            client.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
