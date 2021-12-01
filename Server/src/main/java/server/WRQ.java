package server;

import common.Constants;
import common.ServerUDP;
import common.codes.ErrorCode;
import common.packets.ACKpacket;
import common.packets.DataPacket;
import common.packets.ErrorPacket;
import common.packets.WRQpacket;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class WRQ {

    public static void handleOperation(ServerUDP server, DatagramPacket clientPacket) {
        DatagramSocket socket = server.getSocket();
        WRQpacket wrqPacket = new WRQpacket(clientPacket.getData());

        String filename = null;
        try {
            filename = wrqPacket.getFilename();
        } catch (IOException ex) {
            sendReport("Failed to deserialize file name\n");
            System.out.println("Failed to deserialize file name!");
            ex.printStackTrace();
        }
        if (filename == null) {
            sendReport("File name equals null\n");
            System.exit(1);
        }

        File file = new File(server.getRootDir(), filename);

        if (file.exists()) {
            sendReport("User want to upload file named: " + filename + "\n");
            sendReport("Error - file already exists!\n");
            ErrorPacket errorPacket = new ErrorPacket(ErrorCode.FILE_ALREADY_EXISTS, "File already exists!");
            System.out.println("Error - file already exists!");
            DatagramPacket response = new DatagramPacket(errorPacket.getPayload(), errorPacket.getPayload().length, clientPacket.getSocketAddress());

            try {
                socket.send(response);
            } catch (IOException ex) {
                sendReport("Failed to send response packet\n");
                System.out.println("Failed to send response packet!");
                ex.printStackTrace();
            }
        }

        ByteArrayOutputStream recvFileBuf = new ByteArrayOutputStream();
        int bytesReceived = 0;
        short blockNum = 0;
        int tries = 5;

        try {
            socket.setSoTimeout(Constants.BASE_TIMEOUT);

            ACKpacket initialAck = new ACKpacket(blockNum);
            DatagramPacket response = new DatagramPacket(initialAck.getPayload(), initialAck.getPayload().length, clientPacket.getSocketAddress());

            try {
                socket.send(response);
            } catch (IOException ex) {
                sendReport("Failed to send initial ACK\n");
                System.out.println("Failed to send initial ACK!");
                ex.printStackTrace();
                System.exit(1);
            }

            while (true) {
                if (tries == 0) {
                    sendReport("Max transmission attempts reached. File transfer failed.\n");
                    System.out.println("Max transmission attempts reached. File transfer failed.");
                    socket.setSoTimeout(Constants.BASE_TIMEOUT);
                    recvFileBuf.close();
                }

                try {
                    socket.receive(clientPacket);
                    DataPacket dataPacket = new DataPacket(clientPacket.getData());
                    blockNum = dataPacket.getBlockNumber();

                    int receivedBlockSize = clientPacket.getLength() - 4; //4 -- опкод + номер блока
                    sendReport("Received " + receivedBlockSize + " bytes\n");
                    System.out.printf("Received %d bytes\n", receivedBlockSize);
                    bytesReceived += receivedBlockSize;
                    recvFileBuf.write(dataPacket.getPayload(), 4, receivedBlockSize);
                    tries = 5;

                    ACKpacket ackPacket = new ACKpacket(blockNum);
                    response = new DatagramPacket(ackPacket.getPayload(), ackPacket.getPayload().length, clientPacket.getSocketAddress());
                    socket.send(response);
                    sendReport("ACK sent for block " + blockNum + "\n");
                    System.out.printf("ACK sent for block %d\n", blockNum);

                    if (receivedBlockSize < Constants.BLOCK_SIZE)
                        break;

                } catch (IOException ex) {
                    if (ex.getCause() instanceof SocketTimeoutException) {
                        tries--;
                        sendReport("No data received for next block -- retransmitting ACK.\n");
                        System.out.println("No data received for next block - retransmitting ACK.");
                        socket.setSoTimeout(socket.getSoTimeout() + 1000);
                    } else {
                        sendReport("Failed to send initial ACK!\n");
                        System.out.println("Failed to send initial ACK!");
                        ex.printStackTrace();
                        System.exit(1);
                    }
                }
            }

            sendReport("Received " + bytesReceived + " bytes, file transfer complete.\n");
            System.out.printf("Received %d bytes, file transfer complete.\n", bytesReceived);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            recvFileBuf.writeTo(fileOutputStream);
            fileOutputStream.close();
            recvFileBuf.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static boolean sendReport(String report) {
        return LogWriter.writeEvent(report);
    }

}
