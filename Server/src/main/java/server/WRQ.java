package server;

import common.Constants;
import common.ServerUDP;
import common.codes.ErrorCode;
import common.packets.ACKPacket;
import common.packets.DataPacket;
import common.packets.ErrorPacket;
import common.packets.WRQPacket;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class WRQ {

    private static final Logger logger = Logger.getLogger(WRQ.class);

    public static void handleOperation(ServerUDP server, DatagramPacket clientPacket) {
        DatagramSocket socket = server.getSocket();
        WRQPacket wrqPacket = new WRQPacket(clientPacket.getData());

        String filename = null;
        filename = getFilename(wrqPacket);

        if (filename == null) {
            logger.fatal("File name equals null");
            System.exit(1);
        }

        File file = new File(server.getRootDir(), filename);
        logger.info("User want to upload file named: " + filename);

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
                    logger.fatal("Max transmission attempts reached. File transfer failed.");
                    socket.setSoTimeout(Constants.BASE_TIMEOUT);
                    recvFileBuf.close();
                }

                try {
                    socket.receive(clientPacket);
                    DataPacket dataPacket = new DataPacket(clientPacket.getData());
                    blockNum = dataPacket.getBlockNumber();

                    int receivedBlockSize = clientPacket.getLength() - 4; //4 -- опкод + номер блока
                    logger.info("Received " + receivedBlockSize + " bytes");
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
                        logger.error("No data received for next block - retransmitting ACK.");
                        socket.setSoTimeout(socket.getSoTimeout() + 1000);
                    } else {
                        logger.fatal(ex);
                        ex.printStackTrace();
                        System.exit(1);
                    }
                }
            }

            logger.info("Received " + bytesReceived + " bytes, file transfer complete.");
            writingFile(file, recvFileBuf);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getFilename(WRQPacket wrqPacket){
        try {
            return wrqPacket.getFilename();
        } catch (IOException ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        return null;
    }

    public static void fileExistenceChecking(File file, DatagramSocket socket, DatagramPacket clientPacket) {

        if (file.exists()) {
            logger.error("File already exists!");
            ErrorPacket errorPacket = new ErrorPacket(ErrorCode.FILE_ALREADY_EXISTS, "File already exists!");
            DatagramPacket response = new DatagramPacket(errorPacket.getPayload(), errorPacket.getPayload().length, clientPacket.getSocketAddress());

            try {
                socket.send(response);
            } catch (IOException ex) {
                logger.fatal(ex);
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
            logger.fatal(ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void sendAckPacket(DatagramSocket socket, DatagramPacket clientPacket, short blockNum) throws IOException {
        ACKPacket ackPacket = new ACKPacket(blockNum);
        DatagramPacket response = new DatagramPacket(ackPacket.getPayload(), ackPacket.getPayload().length, clientPacket.getSocketAddress());
        socket.send(response);
        logger.info("ACK sent for block " + blockNum);
    }

    public static void writingFile(File file,ByteArrayOutputStream recvFileBuf) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        recvFileBuf.writeTo(fileOutputStream);
        fileOutputStream.close();
        recvFileBuf.close();
    }
}
