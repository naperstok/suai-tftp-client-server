package server;

import common.Constants;
import common.ServerUDP;
import common.codes.ErrorCode;
import common.packets.ACKPacket;
import common.packets.DataPacket;
import common.packets.ErrorPacket;
import common.packets.WRQPacket;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class WRQ {

    public static void handleOperation(ServerUDP server, DatagramPacket clientPacket) {
        DatagramSocket socket = server.getSocket();
        WRQPacket wrqPacket = new WRQPacket(clientPacket.getData());

        String filename = null;
        filename = getFilename(wrqPacket);

        if (filename == null) {
            sendReport("File name equals null\n");
            System.exit(1);
        }

        File file = new File(server.getRootDir(), filename);
        sendReport("User want to upload file named: " + filename + "\n");

        fileExistenceChecking(file, socket, clientPacket);

        ByteArrayOutputStream recvFileBuf = new ByteArrayOutputStream();
        int bytesReceived = 0;
        short blockNum = 0;
        int tries = 5;

        try {
            socket.setSoTimeout(Constants.BASE_TIMEOUT);
            sendInitialAckPacket(socket, clientPacket, blockNum);

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

                    sendAckPacket(socket, clientPacket, blockNum);

                    if (receivedBlockSize < Constants.BLOCK_SIZE) {
                        break;
                    }

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
            System.out.println("Received " + bytesReceived + " bytes, file transfer complete.");
            writingFile(file, recvFileBuf);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static boolean sendReport(String report) {
        return LogWriter.writeEvent(report);
    }

    public static String getFilename(WRQPacket wrqPacket){
        try {
            return wrqPacket.getFilename();
        } catch (IOException ex) {
            sendReport("Failed to deserialize file name\n");
            System.out.println("Failed to deserialize file name!");
            ex.printStackTrace();
        }
        return null;
    }

    public static void fileExistenceChecking(File file, DatagramSocket socket, DatagramPacket clientPacket) {

        if (file.exists()) {
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
    }

    public static void sendInitialAckPacket(DatagramSocket socket, DatagramPacket clientPacket, short blockNum) {
        ACKPacket initialAck = new ACKPacket(blockNum);
        DatagramPacket response = new DatagramPacket(initialAck.getPayload(), initialAck.getPayload().length, clientPacket.getSocketAddress());

        try {
            socket.send(response);
        } catch (IOException ex) {
            sendReport("Failed to send initial ACK\n");
            System.out.println("Failed to send initial ACK!");
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void sendAckPacket(DatagramSocket socket, DatagramPacket clientPacket, short blockNum) throws IOException {
        ACKPacket ackPacket = new ACKPacket(blockNum);
        DatagramPacket response = new DatagramPacket(ackPacket.getPayload(), ackPacket.getPayload().length, clientPacket.getSocketAddress());
        socket.send(response);
        sendReport("ACK sent for block " + blockNum + "\n");
        System.out.printf("ACK sent for block %d\n", blockNum);
    }

    public static void writingFile(File file,ByteArrayOutputStream recvFileBuf) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        recvFileBuf.writeTo(fileOutputStream);
        fileOutputStream.close();
        recvFileBuf.close();
    }
}
